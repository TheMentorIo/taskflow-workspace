package com.learn.taskflow.identity.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.WorkflowType;
import com.learn.taskflow.identity.enums.WorkflowCode;

public interface WorkflowTypeRepository
        extends JpaRepository<WorkflowType, UUID> {

    Optional<WorkflowType> findByNameContainingIgnoreCase(String name);

    Optional<WorkflowType> findByCode(WorkflowCode workflowCode);

    List<WorkflowType> findAllByOrderByCodeAsc();
}