package com.learn.taskflow.identity.response;

import java.util.UUID;

public record TokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UUID sessionId,
        UserResponse user) {
}
