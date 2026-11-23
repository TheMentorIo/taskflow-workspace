package com.learn.taskflow.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.learn.taskflow.identity.entity.OtpChallenge;
import com.learn.taskflow.identity.enums.OtpDeliveryChannel;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT challenge FROM OtpChallenge challenge JOIN FETCH challenge.securityChallenge securityChallenge WHERE challenge.id = :id")
    Optional<OtpChallenge> findById(@Param("id") UUID id);

    Optional<OtpChallenge> findBySecurityChallenge_Id(UUID securityChallengeId);

    boolean existsBySecurityChallenge_Id(UUID securityChallengeId);

    long countByDeliveryChannel(OtpDeliveryChannel deliveryChannel);
}
