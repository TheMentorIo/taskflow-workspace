package com.learn.taskflow.identity.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.learn.taskflow.identity.enums.AuthenticationAttemptStatus;
import com.learn.taskflow.identity.enums.AuthenticationFailureReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "authentication_attempts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "attempted_identifier", nullable = false, length = 255)
    private String attemptedIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthenticationAttemptStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason", length = 64)
    private AuthenticationFailureReason failureReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "security_challenge_id")
    private SecurityChallenge securityChallenge;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;
    @Column(name = "attempt_count")
    @Builder.Default
    private int attemptCount = 0;
    @CreationTimestamp
    @Column(name = "attempted_at", nullable = false, updatable = false)
    private LocalDateTime attemptedAt;
}