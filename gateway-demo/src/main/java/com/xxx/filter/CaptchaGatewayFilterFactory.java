package com.xxx.filter;

import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
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
    private Mono<Void> validateCaptcha(org.springframework.web.server.ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain, String blockKey, String countKey) {
        ServerHttpRequest request = exchange.getRequest();
        String userCode = request.getHeaders().getFirst(CAPTCHA_CODE_HEADER);
        String captchaId = request.getHeaders().getFirst(CAPTCHA_ID_HEADER);

        if (StringUtils.isAnyBlank(userCode, captchaId)) {
            return sendErrorResponse(exchange, "CAPTCHA_MISSING", "Captcha code or id is missing.");
        }

        String captchaKey = CAPTCHA_CODE_KEY_PREFIX + captchaId;

        return redisTemplate.opsForValue().get(captchaKey)
                .flatMap(storedCode -> {
                    if (userCode.equalsIgnoreCase(storedCode)) {
                        // 校验成功，删除相关键并放行
                        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                                .header(CAPTCHA_VERIFIED_HEADER, "true").build();
                        // 使用 blockKey 替代 ipBlockKey
                        return redisTemplate.delete(blockKey, countKey, captchaKey)
                                .then(chain.filter(exchange.mutate().request(modifiedRequest).build()));
                    } else {
                        // 答案错误
                        return sendErrorResponse(exchange, "CAPTCHA_INCORRECT", "Incorrect captcha code.");
                    }
                })
                .switchIfEmpty(sendErrorResponse(exchange, "CAPTCHA_EXPIRED", "Captcha has expired, please refresh."));
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

    private Mono<Void> sendErrorResponse(org.springframework.web.server.ServerWebExchange exchange, String errorCode, String message) {
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
        if (ip != null && ip.length() > 0 && !"unknown".equalsIgnoreCase(ip)) {
            if (ip.contains(",")) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = headers.getFirst("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
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
