package com.learn.taskflow.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learn.taskflow.identity.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

    @Modifying
    @Query(value = """
        INSERT INTO devices (id, user_id, device_identifier, device_type, operating_system, device_name, created_at)
        VALUES (:id, :userId, :identifier, :deviceType, :operatingSystem, :deviceName, CURRENT_TIMESTAMP)
        ON CONFLICT (user_id, device_identifier) WHERE deleted_at IS NULL DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(
        @Param("id") UUID id,
        @Param("userId") UUID userId,
        @Param("identifier") String identifier,
        @Param("deviceType") String deviceType,
        @Param("operatingSystem") String operatingSystem,
        @Param("deviceName") String deviceName
    );

    Optional<Device> findByUserIdAndDeviceIdentifier(
        UUID userId,
        String deviceIdentifier
    );

    Optional<Device> findByIdAndUserId(UUID id, UUID userId);

    List<Device> findAllByUserId(UUID userId);

    boolean existsByUserIdAndDeviceIdentifier(UUID userId, String deviceIdentifier);

    long countByUserId(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Device device
        SET device.deletedAt = :deletedAt
        WHERE device.id = :deviceId
          AND device.deletedAt IS NULL
        """)
    int softDeleteById(
        @Param("deviceId") UUID deviceId,
        @Param("deletedAt") LocalDateTime deletedAt
    );
}