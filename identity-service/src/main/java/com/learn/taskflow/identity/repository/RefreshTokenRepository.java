package com.learn.taskflow.identity.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learn.taskflow.identity.entity.RefreshToken;

import jakarta.persistence.LockModeType;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT token FROM RefreshToken token WHERE token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    @Modifying
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = CURRENT_TIMESTAMP
        WHERE token.familyId = :familyId
          AND token.revokedAt IS NULL
        """)
    int revokeFamily(@Param("familyId") UUID familyId);

    @Modifying
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = CURRENT_TIMESTAMP
        WHERE token.session.id = :sessionId
          AND token.revokedAt IS NULL
        """)
    int revokeBySessionId(@Param("sessionId") UUID sessionId);

    @Modifying
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = CURRENT_TIMESTAMP
        WHERE token.user.id = :userId
          AND token.revokedAt IS NULL
        """)
    int revokeByUserId(@Param("userId") UUID userId);
}
