package com.secureuser.service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean save(String key, String value, long ttlMinutes) {
        try {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofMinutes(ttlMinutes));
            return Boolean.TRUE.equals(success);
        } catch (Exception e) {
            log.error("Error saving data in Redis for key: {}", key, e);
            return false;
        }
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}