package com.xxx.filter;

import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT过滤器
 */
@Slf4j
@Component
public class JwtFilter implements GlobalFilter, Ordered {

    @Value("${auth.jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<String, Object> map = new HashMap<>();
        map.put("sub", "auth");
        map.put("now", System.currentTimeMillis());
        map.put("exp", System.currentTimeMillis() + 60000);
        String jwt = JWTUtil.createToken(map, jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("jwt是：{}",jwt);
        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header("Authorization", jwt)
                .build();
        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();
        return chain.filter(newExchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
