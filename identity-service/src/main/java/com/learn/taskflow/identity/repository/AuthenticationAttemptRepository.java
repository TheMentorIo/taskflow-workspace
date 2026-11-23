package com.learn.taskflow.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.AuthenticationAttempt;
import com.learn.taskflow.identity.enums.AuthenticationAttemptStatus;

public interface AuthenticationAttemptRepository
        extends JpaRepository<AuthenticationAttempt, UUID> {

    List<AuthenticationAttempt> findAllByUser_IdOrderByAttemptedAtDesc(UUID userId);

    List<AuthenticationAttempt> findAllBySecurityChallenge_IdOrderByAttemptedAtDesc(
        UUID securityChallengeId
    );

    List<AuthenticationAttempt> findAllByAttemptedIdentifierOrderByAttemptedAtDesc(
        String attemptedIdentifier
    );

    List<AuthenticationAttempt> findAllByAttemptedIdentifierAndAttemptedAtAfter(
        String attemptedIdentifier,
        LocalDateTime attemptedAt
    );

    long countByAttemptedIdentifierAndIpAddressAndStatusAndAttemptedAtAfter(
        String attemptedIdentifier,
        String ipAddress,
        AuthenticationAttemptStatus status,
        LocalDateTime attemptedAt
    );

    long countByUser_IdAndStatus(UUID userId, AuthenticationAttemptStatus status);
}
