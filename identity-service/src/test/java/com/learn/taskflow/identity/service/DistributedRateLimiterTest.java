package com.learn.taskflow.identity.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import com.learn.taskflow.identity.exception.RateLimitExceededException;
import com.learn.taskflow.identity.exception.RateLimitUnavailableException;

@ExtendWith(MockitoExtension.class)
class DistributedRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Test
    void rejectsWhenRedisScriptReportsLimitExceeded() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenReturn(1L);

        assertThatThrownBy(() -> new DistributedRateLimiter(redisTemplate)
                .check("login", "127.0.0.1", "person@example.com", 30, 5))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void reportsRedisAvailabilitySeparately() {
        when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString(), anyString(), anyString()))
                .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("Redis unavailable"));

        assertThatThrownBy(() -> new DistributedRateLimiter(redisTemplate)
                .check("login", "127.0.0.1", "person@example.com", 30, 5))
                .isInstanceOf(RateLimitUnavailableException.class);
    }
}
