package com.learn.taskflow.identity.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.learn.taskflow.identity.entity.WorkflowMechanismPolicy;
import com.learn.taskflow.identity.enums.ChallengeMechanismCode;

public interface WorkflowMechanismPolicyRepository
        extends JpaRepository<WorkflowMechanismPolicy, UUID> {

    List<WorkflowMechanismPolicy> findAllByWorkflowType_IdOrderByPriorityAsc(UUID workflowTypeId);

    List<WorkflowMechanismPolicy> findAllByWorkflowType_IdAndEnabledTrueOrderByPriorityAsc(
        UUID workflowTypeId
    );

    Optional<WorkflowMechanismPolicy> findByWorkflowType_IdAndMechanismType_Code(
        UUID workflowTypeId,
        ChallengeMechanismCode mechanismCode
    );

    boolean existsByWorkflowType_IdAndMechanismType_Code(
        UUID workflowTypeId,
        ChallengeMechanismCode mechanismCode
    );
}
