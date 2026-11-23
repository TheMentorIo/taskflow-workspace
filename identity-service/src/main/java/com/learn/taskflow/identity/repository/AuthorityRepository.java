package com.learn.taskflow.identity.repository;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.Authority;
import com.learn.taskflow.identity.enums.AuthorityName;

public interface AuthorityRepository extends JpaRepository<Authority, UUID> {

    Optional<Authority> findByName(AuthorityName name);

    boolean existsByName(AuthorityName name);

    List<Authority> findAllByNameIn(Iterable<AuthorityName> names);
}