package com.learn.taskflow.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.learn.taskflow.identity.entity.SecurityChallenge;
import com.learn.taskflow.identity.enums.SecurityChallengeStatus;

import jakarta.persistence.LockModeType;

public interface SecurityChallengeRepository
        extends JpaRepository<SecurityChallenge, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT challenge FROM SecurityChallenge challenge WHERE challenge.id = :id")
    java.util.Optional<SecurityChallenge> findById(@org.springframework.data.repository.query.Param("id") UUID id);

    List<SecurityChallenge> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<SecurityChallenge> findAllByUser_IdAndStatusOrderByCreatedAtDesc(
        UUID userId,
        SecurityChallengeStatus status
    );

    List<SecurityChallenge> findAllByStatusAndExpiresAtBefore(
        SecurityChallengeStatus status,
        LocalDateTime expiresAt
    );

    long countByUser_IdAndStatus(UUID userId, SecurityChallengeStatus status);
}
