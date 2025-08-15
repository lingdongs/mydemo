package com.xxx.controller;

import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@RestController
public class CaptchaController {

    private static final String CAPTCHA_CODE_KEY_PREFIX = "captcha:code:";

    @Autowired
    private Producer producer;

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @GetMapping("/captcha")
    public Mono<ResponseEntity<byte[]>> getCaptcha() {
        // 由于 Kaptcha 是阻塞库，我们需要在专门的线程池中执行其操作
        return Mono.fromCallable(() -> {
                    String text = producer.createText();
                    BufferedImage image = producer.createImage(text);
                    return new CaptchaData(text, image);
                }).subscribeOn(Schedulers.boundedElastic())
                .flatMap(captchaData -> {
                    String captchaId = UUID.randomUUID().toString();
                    String captchaKey = CAPTCHA_CODE_KEY_PREFIX + captchaId;

                    // 将图片转换为字节数组
                    byte[] imageBytes;
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        ImageIO.write(captchaData.getImage(), "jpg", baos);
                        imageBytes = baos.toByteArray();
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Failed to write captcha image", e));
                    }

                    // 设置响应头
                    HttpHeaders headers = new HttpHeaders();
                    headers.add("X-Captcha-Id", captchaId);
                    headers.add(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate, max-age=0");
                    headers.add(HttpHeaders.PRAGMA, "no-cache");
                    headers.add(HttpHeaders.EXPIRES, "0");

                    // 将验证码存入 Redis 并构建响应
                    return redisTemplate.opsForValue()
                            .set(captchaKey, captchaData.getText(), Duration.ofMinutes(2))
                            .then(Mono.just(ResponseEntity.ok()
                                    .headers(headers)
                                    .contentType(MediaType.IMAGE_JPEG)
                                    .body(imageBytes)));
                });
    }

    // 内部类用于封装验证码数据
    private static class CaptchaData {
        private final String text;
        private final BufferedImage image;

        public CaptchaData(String text, BufferedImage image) {
            this.text = text;
            this.image = image;
        }

        public String getText() {
            return text;
        }

        public BufferedImage getImage() {
            return image;
        }
    }
}
