package com.xxx.filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.captcha.generator.MathGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class CaptchaGatewayFilterFactory extends AbstractGatewayFilterFactory<CaptchaGatewayFilterFactory.Config> {

    private static final String CAPTCHA_CODE_KEY_PREFIX = "captcha:code:";
    private static final String CAPTCHA_REQUEST_COUNT_KEY_PREFIX = "captcha:req_count:";
    // 将 IP 锁定键重命名为更通用的身份锁定键
    private static final String CAPTCHA_BLOCK_KEY_PREFIX = "captcha:block:";
    private static final String CAPTCHA_CODE_HEADER = "X-Captcha-Code";
    private static final String CAPTCHA_ID_HEADER = "X-Captcha-Id";
    private static final String CAPTCHA_VERIFIED_HEADER = "X-Captcha-Verified";
    private static final String CAPTCHA_VALIDATION_MONO_KEY = "captchaValidationMono";

    private enum CaptchaValidationResult {
        SUCCESS,
        INCORRECT,
        EXPIRED
    }


    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public CaptchaGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            // 默认标识符为 IP 地址
            String identifier = getIpAddress(request);

            try {
                // 尝试获取登录用户ID，如果存在，则使用 loginId 作为标识符
                SaReactorSyncHolder.setContext(exchange);
                String loginId = (String) StpUtil.getLoginIdDefaultNull();
                if (StringUtils.isNotBlank(loginId)) {
                    identifier = loginId;
                }
            }
            finally {
                SaReactorSyncHolder.clearContext();
            }

            String blockKey = CAPTCHA_BLOCK_KEY_PREFIX + identifier;
            String countKey = CAPTCHA_REQUEST_COUNT_KEY_PREFIX + path + ":" + identifier;
            final String finalIdentifier = identifier; // for use in lambda

            return redisTemplate.hasKey(blockKey)
                    .flatMap(isBlocked -> {
                        if (isBlocked) {
                            // 1. 身份被锁定，必须校验验证码
                            return validateCaptcha(exchange, chain, blockKey, countKey);
                        } else {
                            // 2. 身份未被锁定，执行速率限制检查
                            return checkRateLimit(exchange, chain, config, blockKey, countKey, finalIdentifier);
                        }
                    });
        };
    }

    /**
     * 校验验证码
     */
    private Mono<Void> validateCaptcha(ServerWebExchange exchange, GatewayFilterChain chain, String blockKey, String countKey) {
        // 从交换属性中检索缓存的 Mono，以防止对同一请求重复执行整个逻辑链
        Mono<Void> cachedMono = exchange.getAttribute(CAPTCHA_VALIDATION_MONO_KEY);
        if (cachedMono != null) {
            return cachedMono;
        }

        ServerHttpRequest request = exchange.getRequest();
        String userCode = request.getHeaders().getFirst(CAPTCHA_CODE_HEADER);
        String captchaId = request.getHeaders().getFirst(CAPTCHA_ID_HEADER);

        if (StringUtils.isAnyBlank(userCode, captchaId)) {
            return sendErrorResponse(exchange, "CAPTCHA_MISSING", "Captcha code or id is missing.");
        }

        String captchaKey = CAPTCHA_CODE_KEY_PREFIX + captchaId;

        // 将整个验证和处理逻辑构建成一个 Mono
        Mono<Void> validationMono = redisTemplate.opsForValue().get(captchaKey)
                .map(storedCode -> new MathGenerator().verify(storedCode, userCode)
                        ? CaptchaValidationResult.SUCCESS
                        : CaptchaValidationResult.INCORRECT)
                .defaultIfEmpty(CaptchaValidationResult.EXPIRED)
                .flatMap(result -> {
                    switch (result) {
                        case SUCCESS:
                            // 验证成功：删除 Redis 键并继续过滤器链
                            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                    .header(CAPTCHA_VERIFIED_HEADER, "true").build();
                            return redisTemplate.delete(blockKey, countKey, captchaKey)
                                    .then(chain.filter(exchange.mutate().request(modifiedRequest).build()));
                        case INCORRECT:
                            // 验证失败：验证码不正确
                            return sendErrorResponse(exchange, "CAPTCHA_INCORRECT", "Incorrect captcha code.");
                        case EXPIRED:
                        default:
                            // 验证失败：验证码过期或不存在
                            return sendErrorResponse(exchange, "CAPTCHA_EXPIRED", "Captcha has expired, please refresh.");
                    }
                });

        // 缓存最终的 Mono<Void> 并将其存储在交换属性中，以供同一请求中的后续调用使用
        Mono<Void> finalResult = validationMono.cache();
        exchange.getAttributes().put(CAPTCHA_VALIDATION_MONO_KEY, finalResult);

        return finalResult;
    }

    /**
     * 检查速率限制
     */
    private Mono<Void> checkRateLimit(org.springframework.web.server.ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain, Config config, String blockKey, String countKey, String identifier) {
        return redisTemplate.opsForValue().increment(countKey)
                .flatMap(count -> {
                    if (count == 1) {
                        // 首次访问，设置过期时间
                        return redisTemplate.expire(countKey, Duration.ofSeconds(config.getTimeWindowInSeconds()))
                                .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > config.getThreshold()) {
                        // 达到阈值，锁定身份并返回需要验证码的错误
                        log.warn("Identifier [{}] has exceeded the request threshold for path [{}]. Triggering captcha.", identifier, exchange.getRequest().getURI().getPath());
                        return redisTemplate.opsForValue().set(blockKey, "1", Duration.ofMinutes(5))
                                .then(sendErrorResponse(exchange, "CAPTCHA_REQUIRED", "Please solve the CAPTCHA to continue."));
                    } else {
                        // 未达到阈值，正常放行
                        return chain.filter(exchange);
                    }
                });
    }

    private Mono<Void> sendErrorResponse(ServerWebExchange exchange, String errorCode, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);

        try {
            byte[] responseBytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(responseBytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error writing captcha error response", e);
            return response.setComplete();
        }
    }


    private String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ip = headers.getFirst("X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (remoteAddress != null) {
                ip = remoteAddress.getAddress().getHostAddress();
            }
        }
        return ip;
    }

    @Data
    public static class Config {
        private int threshold; // 阈值
        private long timeWindowInSeconds; // 时间窗口，单位秒
    }
}
