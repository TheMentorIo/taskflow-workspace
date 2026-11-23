package com.learn.taskflow.identity.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.UUID;
import jakarta.persistence.Id;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table(
    name = "workflow_mechanism_policies",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_workflow_mechanism_policy",
            columnNames = {
                "workflow_type_id",
                "mechanism_type_id"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkflowMechanismPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "workflow_type_id",
        nullable = false
    )
    private WorkflowType workflowType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "mechanism_type_id",
        nullable = false
    )
    private ChallengeMechanismType mechanismType;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    @Builder.Default
    private short priority = 0;

   @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private JsonNode config = JsonNodeFactory.instance.objectNode();

    @OneToMany(mappedBy = "workflowMechanismPolicy", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<SecurityChallenge> securityChallenges = new HashSet<>();
}