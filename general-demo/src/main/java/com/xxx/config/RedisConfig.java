package com.xxx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // key采用String的序列化方式
        template.setKeySerializer(RedisSerializer.string());
        // hash的key也采用String的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        // value序列化方式采用jackson
        template.setValueSerializer(RedisSerializer.json());
        // hash的value序列化方式采用jackson
        template.setHashValueSerializer(RedisSerializer.json());
        return template;
    }

}
