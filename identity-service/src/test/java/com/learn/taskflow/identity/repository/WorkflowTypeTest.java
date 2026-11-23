package com.learn.taskflow.identity.repository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;

import com.learn.taskflow.identity.entity.WorkflowType;
import com.learn.taskflow.identity.enums.WorkflowCode;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
public class WorkflowTypeTest {
    @Autowired
    private WorkflowTypeRepository workflowTypeRepository;

   @Test
    void shouldFindWorkflowTypeByCode() {
        Optional<WorkflowType> workflowType =
                workflowTypeRepository
                        .findByCode(WorkflowCode.values()[0]);
        assertThat(workflowType).isPresent();

        assertThat(workflowType.get().getCode())
                .isEqualTo(WorkflowCode.PASSWORD_RECOVERY);

        assertThat(workflowType.get().getName())
                .isEqualTo("Password recovery");
    }

//     @Test
//     void shouldLoadAllWorkflowTypes() {
//         List<WorkflowType> workflows = workflowTypeRepository.findAll();

//         assertThat(workflows).hasSize(6);

//         assertThat(workflows)
//                 .extracting(WorkflowType::getCode)
//                 .containsExactlyInAnyOrder(
//                         WorkflowCode.PASSWORD_RECOVERY,
//                         WorkflowCode.EMAIL_VERIFICATION,
//                         WorkflowCode.MFA,
//                         WorkflowCode.EMAIL_CHANGE,
//                         WorkflowCode.PHONE_VERIFICATION,
//                         WorkflowCode.TRANSACTION_CONFIRMATION
//                 );
//     }

//     @Test
//     void shouldHaveCorrectWorkflowNames() {
//         assertThat(workflowTypeRepository
//                 .findByCode(WorkflowCode.PASSWORD_RECOVERY)
//                 .orElseThrow()
//                 .getName())
//                 .isEqualTo("Password recovery");

//         assertThat(workflowTypeRepository
//                 .findByCode(WorkflowCode.EMAIL_VERIFICATION)
//                 .orElseThrow()
//                 .getName())
//                 .isEqualTo("Email verification");

//         assertThat(workflowTypeRepository
//                 .findByCode(WorkflowCode.MFA)
//                 .orElseThrow()
//                 .getName())
//                 .isEqualTo("Multi-factor authentication");
//     }

//     @Test
//     void shouldReturnEmptyWhenNameDoesNotExist() {
//         Optional<WorkflowType> result =
//                 workflowTypeRepository
//                         .findByNameContainingIgnoreCase("does-not-exist");

//         assertThat(result).isEmpty();
//     }

//     @Test
//     void shouldFindWorkflowTypeIgnoringCase() {
//         Optional<WorkflowType> result =
//                 workflowTypeRepository
//                         .findByNameContainingIgnoreCase("PASSWORD");

//         assertThat(result).isPresent();
//         assertThat(result.get().getName())
//                 .isEqualTo("Password recovery");
//     }
}
