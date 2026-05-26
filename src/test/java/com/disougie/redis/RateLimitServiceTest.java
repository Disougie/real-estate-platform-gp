package com.disougie.redis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
public class RateLimitServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitService rateLimitService;

    private final String clientId = "test-client";
    private final String key = "user:" + clientId;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should return true and initialize counter if key does not exist")
    void isAllowed_ShouldReturnTrueForNewClient() {
        // Given
        when(valueOperations.get(key)).thenReturn(null);

        // When
        boolean result = rateLimitService.isAllowed(clientId);

        // Then
        assertTrue(result);
        verify(valueOperations).set(eq(key), eq(1), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Should return true and increment counter if requests < 30")
    void isAllowed_ShouldReturnTrueAndIncrementIfUnderLimit() {
        // Given
        when(valueOperations.get(key)).thenReturn(15);

        // When
        boolean result = rateLimitService.isAllowed(clientId);

        // Then
        assertTrue(result);
        verify(valueOperations).increment(key);
    }

    @Test
    @DisplayName("Should return false if requests reach limit of 30")
    void isAllowed_ShouldReturnFalseIfAtLimit() {
        // Given
        when(valueOperations.get(key)).thenReturn(30);

        // When
        boolean result = rateLimitService.isAllowed(clientId);

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false if requests exceed limit")
    void isAllowed_ShouldReturnFalseIfOverLimit() {
        // Given
        when(valueOperations.get(key)).thenReturn(35);

        // When
        boolean result = rateLimitService.isAllowed(clientId);

        // Then
        assertFalse(result);
    }
}
