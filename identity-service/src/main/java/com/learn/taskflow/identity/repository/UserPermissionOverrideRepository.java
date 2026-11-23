package com.learn.taskflow.identity.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.UserPermissionOverride;
import com.learn.taskflow.identity.entity.UserPermissionOverrideId;

public interface UserPermissionOverrideRepository
        extends JpaRepository<UserPermissionOverride, UserPermissionOverrideId> {

        List<UserPermissionOverride> findAllByUser_Id(UUID userId);

        List<UserPermissionOverride> findAllByAuthority_Id(UUID authorityId);

        List<UserPermissionOverride> findAllByUser_IdAndAuthority_Id(
                UUID userId,
                UUID authorityId
        );
}