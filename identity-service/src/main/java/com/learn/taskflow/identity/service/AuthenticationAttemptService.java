package com.learn.taskflow.identity.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.AuthenticationAttempt;
import com.learn.taskflow.identity.entity.SecurityChallenge;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AuthenticationAttemptStatus;
import com.learn.taskflow.identity.enums.AuthenticationFailureReason;
import com.learn.taskflow.identity.repository.AuthenticationAttemptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationAttemptService {

    private final AuthenticationAttemptRepository repository;

    @Transactional
    public void recordSuccess(User user, String identifier, String ip, String userAgent,
            SecurityChallenge challenge) {
        save(user, identifier, ip, userAgent, AuthenticationAttemptStatus.SUCCESS, null, challenge);
    }

    @Transactional
    public void recordFailure(User user, String identifier, String ip, String userAgent,
            AuthenticationFailureReason reason, SecurityChallenge challenge) {
        save(user, identifier, ip, userAgent, AuthenticationAttemptStatus.FAILURE, reason, challenge);
    }

    private void save(User user, String identifier, String ip, String userAgent,
            AuthenticationAttemptStatus status, AuthenticationFailureReason reason,
            SecurityChallenge challenge) {
        repository.save(AuthenticationAttempt.builder()
                .user(user)
                .attemptedIdentifier(identifier)
                .status(status)
                .failureReason(reason)
                .securityChallenge(challenge)
                .ipAddress(ip == null ? "unknown" : ip)
                .userAgent(userAgent)
                .attemptedAt(LocalDateTime.now())
                .build());
    }
}
