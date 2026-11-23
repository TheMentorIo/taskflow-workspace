package com.learn.taskflow.identity.security.service;

import java.util.Base64;
import java.util.UUID;

import io.jsonwebtoken.Jwts;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class JwtAuthenticationServiceTest {

    private final JwtAuthenticationService service = new JwtAuthenticationService(
            Base64.getEncoder().encodeToString(Jwts.SIG.HS256.key().build().getEncoded()));

    @Test
    void accessAndRefreshTokenTypesRemainDistinct() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        long expiry = System.currentTimeMillis() + 60_000;

        var accessClaims = service.parseToken(service.generateAccessToken(userId, sessionId, expiry));
        var refreshClaims = service.parseToken(service.generateRefreshToken(userId, sessionId, expiry));

        assertThat(service.isAccessToken(accessClaims)).isTrue();
        assertThat(service.isRefreshToken(accessClaims)).isFalse();
        assertThat(service.isRefreshToken(refreshClaims)).isTrue();
        assertThat(service.isAccessToken(refreshClaims)).isFalse();
    }
}
