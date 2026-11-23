package com.learn.taskflow.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.learn.taskflow.identity.exception.RateLimitExceededException;
import com.learn.taskflow.identity.exception.RateLimitUnavailableException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DistributedRateLimiter {

    private static final String INCREMENT_SCRIPT = """
            for index, key in ipairs(KEYS) do
                local current = redis.call('INCR', key)
                if current == 1 then
                    redis.call('EXPIRE', key, ARGV[1])
                end
                if current > tonumber(ARGV[index + 1]) then
                    return 1
                end
            end
            return 0
            """;

    private final StringRedisTemplate redisTemplate;

    @Value("${security.rate-limit.window-seconds:900}")
    private long windowSeconds;

    public void check(String endpoint, String ipAddress, String identifier, long endpointLimit,
            long identifierLimit) {
        List<String> keys = new java.util.ArrayList<>();
        keys.add(key(endpoint, "ip", ipAddress));
        if (identifier != null && identifierLimit > 0) {
            keys.add(key(endpoint, "identifier", identifier + ":" + ipAddress));
        }
        try {
            Long limited = redisTemplate.execute(
                    new DefaultRedisScript<>(INCREMENT_SCRIPT, Long.class), keys,
                    Long.toString(windowSeconds), Long.toString(endpointLimit), Long.toString(identifierLimit));
            if (limited != null && limited == 1L) {
                throw new RateLimitExceededException();
            }
        } catch (DataAccessException exception) {
            throw new RateLimitUnavailableException(exception);
        }
    }

    private String key(String endpoint, String dimension, String value) {
        return "identity:rate:" + endpoint + ":" + dimension + ":" + hash(value == null ? "unknown" : value);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.trim().toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
