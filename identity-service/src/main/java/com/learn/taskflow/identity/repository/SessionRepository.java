package com.learn.taskflow.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learn.taskflow.identity.entity.Device;
import com.learn.taskflow.identity.entity.Session;
import com.learn.taskflow.identity.enums.SessionStatus;

public interface SessionRepository extends JpaRepository<Session, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Session session
        SET session.status = :revokedStatus,
            session.revokedAt = :revokedAt
        WHERE session.status = :activeStatus
          AND session.user.id = :userId
        """)
    int revokeActiveByUserId(
        @Param("userId") UUID userId,
        @Param("activeStatus") SessionStatus activeStatus,
        @Param("revokedStatus") SessionStatus revokedStatus,
        @Param("revokedAt") LocalDateTime revokedAt
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Session session
        SET session.status = :revokedStatus,
            session.revokedAt = :revokedAt
        WHERE session.status = :activeStatus
          AND session.device.id = :deviceId
        """)
    int revokeActiveByDeviceId(
        @Param("deviceId") UUID deviceId,
        @Param("activeStatus") SessionStatus activeStatus,
        @Param("revokedStatus") SessionStatus revokedStatus,
        @Param("revokedAt") LocalDateTime revokedAt
    );

    Set<Session> findByDeviceId(UUID deviceId);

    @EntityGraph(attributePaths = {"user", "device", "device.user"})
    @Query("""
        SELECT session
        FROM Session session
        JOIN FETCH session.user user
        LEFT JOIN FETCH session.device device
        WHERE session.id = :id
        """)
    Optional<Session> findWithDeviceAndUserById(UUID id);

    Set<Session> findByDeviceAndStatus(Device device, SessionStatus status);

    Set<Session> findByDeviceIdAndStatus(UUID deviceId, SessionStatus status);

    Set<Session> findByDevice_User_Id(UUID userId);

    Set<Session> findByDevice_User_IdAndStatus(UUID userId, SessionStatus status);

    List<Session> findByStatusAndExpiresAtBefore(
        SessionStatus status,
        LocalDateTime expiresAt
    );
}