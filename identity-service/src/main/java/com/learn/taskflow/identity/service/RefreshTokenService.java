package com.learn.taskflow.identity.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.RefreshToken;
import com.learn.taskflow.identity.entity.Session;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public IssuedRefreshToken issue(User user, Session session, LocalDateTime expiresAt) {
        return issue(user, session, expiresAt, UUID.randomUUID());
    }

    @Transactional
    public IssuedRefreshToken rotate(RefreshToken current, LocalDateTime expiresAt) {
        LocalDateTime now = LocalDateTime.now();
        current.setUsedAt(now);
        String rawToken = generateToken();
        repository.save(current);
        repository.save(RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .familyId(current.getFamilyId())
                .user(current.getUser())
                .session(current.getSession())
                .expiresAt(expiresAt)
                .createdAt(now)
                .build());
        return new IssuedRefreshToken(rawToken, current.getSession());
    }

    @Transactional
    public RefreshToken consume(String rawToken) {
        RefreshToken token = repository.findByTokenHashForUpdate(hash(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        LocalDateTime now = LocalDateTime.now();
        if (token.getRevokedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        if (token.getUsedAt() != null) {
            repository.revokeFamily(token.getFamilyId());
            throw new IllegalArgumentException("Refresh token replay detected");
        }
        return token;
    }

    @Transactional
    public int revokeForSession(UUID sessionId) {
        return repository.revokeBySessionId(sessionId);
    }

    @Transactional
    public int revokeForUser(UUID userId) {
        return repository.revokeByUserId(userId);
    }

    private IssuedRefreshToken issue(User user, Session session, LocalDateTime expiresAt, UUID familyId) {
        String rawToken = generateToken();
        repository.save(RefreshToken.builder()
                .tokenHash(hash(rawToken))
                .familyId(familyId)
                .user(user)
                .session(session)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build());
        return new IssuedRefreshToken(rawToken, session);
    }

    private String generateToken() {
        byte[] value = new byte[48];
        secureRandom.nextBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record IssuedRefreshToken(String value, Session session) {
    }
}
