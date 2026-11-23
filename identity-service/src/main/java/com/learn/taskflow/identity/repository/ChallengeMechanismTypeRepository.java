package com.learn.taskflow.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.ChallengeMechanismType;
import com.learn.taskflow.identity.enums.ChallengeMechanismCode;

public interface ChallengeMechanismTypeRepository
        extends JpaRepository<ChallengeMechanismType, UUID> {

    Optional<ChallengeMechanismType> findByCode(ChallengeMechanismCode code);

    boolean existsByCode(ChallengeMechanismCode code);

    Optional<ChallengeMechanismType> findByNameIgnoreCase(String name);

    List<ChallengeMechanismType> findAllByOrderByCodeAsc();
}
