package org.example.library.infra.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisClient {

    private final StringRedisTemplate redisTemplate;

    public RedisClient(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void set(String key, String val, long expire, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, val, expire, timeUnit);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
