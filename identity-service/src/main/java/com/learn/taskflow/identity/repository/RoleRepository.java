package com.learn.taskflow.identity.repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.Role;
import com.learn.taskflow.identity.enums.RoleName;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);

    List<Role> findAllByNameNot(RoleName name);
}