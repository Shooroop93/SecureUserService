package com.secureuser.service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSave_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        boolean result = redisService.save("key1", "value1", 10);
        assertTrue(result);
        verify(valueOperations).setIfAbsent("key1", "value1", Duration.ofMinutes(10));
    }

    @Test
    void testSave_Failure() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        boolean result = redisService.save("key2", "value2", 10);

        assertFalse(result);
        verify(valueOperations).setIfAbsent("key2", "value2", Duration.ofMinutes(10));
    }

    @Test
    void testSave_Exception() {
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("Redis error"));
        boolean result = redisService.save("key3", "value3", 10);
        assertFalse(result);
    }

    @Test
    void testGet_ExistingKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("key1")).thenReturn("value1");
        Optional<String> result = redisService.get("key1");
        assertTrue(result.isPresent());
        assertEquals("value1", result.get());
    }

    @Test
    void testGet_NonExistingKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("key2")).thenReturn(null);
        Optional<String> result = redisService.get("key2");
        assertFalse(result.isPresent());
    }

    @Test
    void testDelete() {
        redisService.delete("key1");
        verify(redisTemplate).delete("key1");
    }
}