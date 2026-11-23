package com.learn.taskflow.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.PasswordChallenge;

public interface PasswordChallengeRepository extends JpaRepository<PasswordChallenge, UUID> {

    Optional<PasswordChallenge> findBySecurityChallenge_Id(UUID securityChallengeId);

    boolean existsBySecurityChallenge_Id(UUID securityChallengeId);
}
