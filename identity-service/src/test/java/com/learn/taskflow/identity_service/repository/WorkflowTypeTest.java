package com.learn.taskflow.identity_service.repository;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ContextConfiguration;
import com.learn.taskflow.identity.IdentityServiceApplication;
import com.learn.taskflow.identity.repository.WorkflowTypeRepository;
import com.learn.taskflow.identity.entity.WorkflowType;
import com.learn.taskflow.identity.enums.WorkflowCode;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ContextConfiguration(classes = IdentityServiceApplication.class)
public class WorkflowTypeTest {
    @Autowired
    private WorkflowTypeRepository workflowTypeRepository;

   @Test
    void shouldFindWorkflowTypeByCode() {
        WorkflowType workflowType =
                workflowTypeRepository
                        .findByCode(WorkflowCode.values()[0])
                        .orElseThrow();

        assertThat(workflowType.getCode())
                .isEqualTo(WorkflowCode.PASSWORD_RECOVERY);

        assertThat(workflowType.getName())
                .isEqualTo("Password recovery");
    }

    @Test
    void shouldLoadAllWorkflowTypes() {
        List<WorkflowType> workflows = workflowTypeRepository.findAll();

        assertThat(workflows).hasSize(6);

        assertThat(workflows)
                .extracting(WorkflowType::getCode)
                .containsExactlyInAnyOrder(
                        WorkflowCode.PASSWORD_RECOVERY,
                        WorkflowCode.EMAIL_VERIFICATION,
                        WorkflowCode.MFA,
                        WorkflowCode.EMAIL_CHANGE,
                        WorkflowCode.PHONE_VERIFICATION,
                        WorkflowCode.TRANSACTION_CONFIRMATION
                );
    }

    @Test
    void shouldHaveCorrectWorkflowNames() {
        assertThat(workflowTypeRepository
                .findByCode(WorkflowCode.PASSWORD_RECOVERY)
                .orElseThrow()
                .getName())
                .isEqualTo("Password recovery");

        assertThat(workflowTypeRepository
                .findByCode(WorkflowCode.EMAIL_VERIFICATION)
                .orElseThrow()
                .getName())
                .isEqualTo("Email verification");

        assertThat(workflowTypeRepository
                .findByCode(WorkflowCode.MFA)
                .orElseThrow()
                .getName())
                .isEqualTo("Multi-factor authentication");
    }

    @Test
    void shouldReturnEmptyWhenNameDoesNotExist() {
        Optional<WorkflowType> result =
                workflowTypeRepository
                        .findByNameContainingIgnoreCase("does-not-exist");

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindWorkflowTypeIgnoringCase() {
        Optional<WorkflowType> result =
                workflowTypeRepository
                        .findByNameContainingIgnoreCase("PASSWORD");

        assertThat(result).isPresent();
        assertThat(result.get().getName())
                .isEqualTo("Password recovery");
    }
}
