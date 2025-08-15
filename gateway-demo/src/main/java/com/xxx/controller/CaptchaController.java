package com.xxx.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@RestController
public class CaptchaController {

    private static final String CAPTCHA_CODE_KEY_PREFIX = "captcha:code:";

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @GetMapping("/captcha")
    public Mono<ResponseEntity<byte[]>> getCaptcha() {
        return Mono.fromCallable(() -> {
                    // 使用 Hutool 生成算术验证码
                    ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 100);
                    captcha.setGenerator(new MathGenerator(1));
                    captcha.createCode();
                    return captcha;
                })
                .flatMap(captcha -> {
                    String captchaId = UUID.randomUUID().toString();
                    String captchaKey = CAPTCHA_CODE_KEY_PREFIX + captchaId;
                    String captchaCode = captcha.getCode();
                    byte[] imageBytes = captcha.getImageBytes();

                    // 设置响应头
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Captcha-Id", captchaId);
                    headers.add(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
                    headers.add(HttpHeaders.PRAGMA, "no-cache");
                    headers.add(HttpHeaders.EXPIRES, "0");

                    // 将验证码存入 Redis 并构建响应
                    return redisTemplate.opsForValue()
                            .set(captchaKey, captchaCode, Duration.ofMinutes(2))
                            .then(Mono.just(ResponseEntity.ok()
                                    .headers(headers)
                                    .contentType(MediaType.IMAGE_JPEG)
                                    .body(imageBytes)));
                });
    }
}
