package com.xxx.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.validation.ReportAsSingleViolation;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 写入缓存
     *
     * @param key    键
     * @param value  值
     * @param expire 过期时间(s)
     */
    public void set(String key, Object value, long expire) {
        Assert.isTrue(expire > 0, "过期时间必须大于0");
        redisTemplate.opsForValue().set(key, value, expire, TimeUnit.SECONDS);
    }

    /**
     * 读取缓存
     *
     * @param key 键
     * @return 值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置过期时间
     *
     * @param key    键
     * @param expire 过期时间(s)
     * @return 是否成功
     */
    public boolean expire(String key, long expire) {
        Boolean result = redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否成功
     */
    public boolean delete(String key) {
        Boolean result = redisTemplate.delete(key);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 递增或递减
     *
     * @param key   键
     * @param delta 要增加几(可为负数)
     * @return
     */
    public long increment(String key, long delta) {
        Long result = redisTemplate.opsForValue().increment(key, delta);
        if (result == null) {
            throw new RuntimeException("redis操作失败");
        }
        return result;
    }

    /**
     * 如果不存在则设置
     *
     * @param key    键
     * @param value  值
     * @param expire 过期时间(s)
     * @return 是否成功
     */
    public boolean setIfAbsent(String key, Object value, long expire) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, value, expire, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }
}
