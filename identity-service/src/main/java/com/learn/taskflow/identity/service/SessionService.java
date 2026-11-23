package com.learn.taskflow.identity.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.Device;
import com.learn.taskflow.identity.entity.Session;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.SessionStatus;
import com.learn.taskflow.identity.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final RefreshTokenService refreshTokenService;

    @Value("${security.session.idle-timeout-seconds:2592000}")
    private long idleTimeoutSeconds;

    @Value("${security.session.absolute-timeout-seconds:2592000}")
    private long absoluteTimeoutSeconds;

    @Transactional
    public Session create(User user, Device device, String ipAddress, String userAgent, String browser) {
        LocalDateTime now = LocalDateTime.now();
        return sessionRepository.save(Session.builder()
            .user(user)
                .device(device)
                .ipAddress(ipAddress == null ? "unknown" : ipAddress)
                .userAgent(userAgent)
                .browser(browser)
                .status(SessionStatus.ACTIVE)
                .createdAt(now)
                .lastActiveAt(now)
                .expiresAt(now.plusSeconds(idleTimeoutSeconds))
                .absoluteExpiresAt(now.plusSeconds(absoluteTimeoutSeconds))
                .build());
    }

    @Transactional
    public Session requireActive(UUID sessionId, UUID userId) {
        Session session = sessionRepository.findWithDeviceAndUserById(sessionId)
            .filter(candidate -> candidate.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        LocalDateTime now = LocalDateTime.now();
        if (session.getStatus() != SessionStatus.ACTIVE
            || !session.getExpiresAt().isAfter(now)
            || !session.getAbsoluteExpiresAt().isAfter(now)) {
            if (session.getStatus() == SessionStatus.ACTIVE) {
                session.setStatus(SessionStatus.EXPIRED);
                sessionRepository.save(session);
            }
            throw new IllegalArgumentException("Session is no longer active");
        }
        session.setLastActiveAt(now);
        session.setExpiresAt(now.plusSeconds(idleTimeoutSeconds));
        return session;
    }

    @Transactional
    public void revoke(UUID sessionId, UUID userId) {
        Session session = requireOwned(sessionId, userId);
        if (session.getStatus() == SessionStatus.ACTIVE) {
            session.setStatus(SessionStatus.REVOKED);
            session.setRevokedAt(LocalDateTime.now());
            sessionRepository.save(session);
        }
        refreshTokenService.revokeForSession(sessionId);
    }

    @Transactional
    public int revokeAll(UUID userId) {
        int revoked = sessionRepository.revokeActiveByUserId(
                userId, SessionStatus.ACTIVE, SessionStatus.REVOKED, LocalDateTime.now());
        refreshTokenService.revokeForUser(userId);
        return revoked;
    }

    @Transactional(readOnly = true)
    public Session requireOwned(UUID sessionId, UUID userId) {
        return sessionRepository.findWithDeviceAndUserById(sessionId)
            .filter(session -> session.getUser().getId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));
    }
}
