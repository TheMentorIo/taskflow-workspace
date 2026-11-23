package com.learn.taskflow.identity.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AccountStatus;
import com.learn.taskflow.identity.enums.RoleName;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = {"role", "role.authorities", "permissionOverrides", "permissionOverrides.authority"})
    Optional<User> findWithAuthorizationById(UUID id);

    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    Optional<User> findByIdAndDeletedAtIsNull(UUID id);

    Optional<User> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByIdAndDeletedAtIsNull(UUID id);

    List<User> findAllByAccountStatusAndDeletedAtIsNull(AccountStatus accountStatus);

    List<User> findAllByRole_NameAndDeletedAtIsNull(RoleName roleName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE User user
        SET user.accountStatus = :disabledStatus,
            user.deletedAt = :deletedAt,
            user.updatedAt = :updatedAt
        WHERE user.id = :userId
          AND user.deletedAt IS NULL
        """)
    int softDeleteById(
        @Param("userId") UUID userId,
        @Param("disabledStatus") AccountStatus disabledStatus,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );
}