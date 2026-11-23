package com.learn.taskflow.identity.service;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AuthenticationAttemptStatus;
import com.learn.taskflow.identity.enums.AuthenticationFailureReason;
import com.learn.taskflow.identity.repository.AuthenticationAttemptRepository;
import com.learn.taskflow.identity.repository.SecurityChallengeRepository;
import com.learn.taskflow.identity.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationAttemptPolicy {

    private final AuthenticationAttemptRepository attemptRepository;
    private final AuthenticationAttemptService attemptService;
    private final UserRepository userRepository;
    private final SecurityChallengeRepository challengeRepository;
    private final DistributedRateLimiter distributedRateLimiter;

    @Value("${security.auth.max-failures:5}")
    private long maxFailures;

    @Value("${security.auth.failure-window-seconds:900}")
    private long failureWindowSeconds;

    @Value("${security.auth.lockout-seconds:900}")
    private long lockoutSeconds;

    @Value("${security.rate-limit.login-per-ip:30}")
    private long loginPerIp;

    @Value("${security.rate-limit.login-per-identifier-ip:5}")
    private long loginPerIdentifierIp;

    @Value("${security.rate-limit.otp-per-ip:30}")
    private long otpPerIp;

    @Value("${security.rate-limit.otp-per-identifier-ip:5}")
    private long otpPerIdentifierIp;

    public void checkAllowed(String identifier, String ipAddress) {
        boolean otp = identifier != null && identifier.matches("[0-9a-fA-F-]{36}");
        distributedRateLimiter.check(otp ? "otp" : "login", ipAddress, identifier,
            otp ? otpPerIp : loginPerIp, otp ? otpPerIdentifierIp : loginPerIdentifierIp);
        String canonicalIdentifier = canonicalize(identifier);
        String normalizedIp = ipAddress == null ? "unknown" : ipAddress;
        long failures = attemptRepository.countByAttemptedIdentifierAndIpAddressAndStatusAndAttemptedAtAfter(
                canonicalIdentifier, normalizedIp, AuthenticationAttemptStatus.FAILURE,
                LocalDateTime.now().minusSeconds(failureWindowSeconds));
        if (failures >= maxFailures) {
            throw new LockedException("Too many authentication attempts");
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(String identifier, String ipAddress, String userAgent,
            AuthenticationFailureReason reason) {
        recordFailure(identifier, ipAddress, userAgent, reason, null);
        }

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void recordFailure(String identifier, String ipAddress, String userAgent,
            AuthenticationFailureReason reason, java.util.UUID challengeId) {
        String canonicalIdentifier = canonicalize(identifier);
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(canonicalIdentifier).orElse(null);
        attemptService.recordFailure(user, canonicalIdentifier, ipAddress, userAgent, reason,
            challengeId == null ? null : challengeRepository.findById(challengeId).orElse(null));
        if (user != null && countRecentFailures(canonicalIdentifier, ipAddress) >= maxFailures) {
            user.setAccountStatus(com.learn.taskflow.identity.enums.AccountStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusSeconds(lockoutSeconds));
            userRepository.save(user);
        }
    }

    public void recordSuccess(User user, String identifier, String ipAddress, String userAgent) {
        recordSuccess(user, identifier, ipAddress, userAgent, null);
    }

    public void recordSuccess(User user, String identifier, String ipAddress, String userAgent,
            java.util.UUID challengeId) {
        attemptService.recordSuccess(user, canonicalize(identifier), ipAddress, userAgent,
                challengeId == null ? null : challengeRepository.findById(challengeId).orElse(null));
    }

    private long countRecentFailures(String identifier, String ipAddress) {
        return attemptRepository.countByAttemptedIdentifierAndIpAddressAndStatusAndAttemptedAtAfter(
                canonicalize(identifier), ipAddress == null ? "unknown" : ipAddress,
                com.learn.taskflow.identity.enums.AuthenticationAttemptStatus.FAILURE,
                LocalDateTime.now().minusSeconds(failureWindowSeconds));
    }

    private String canonicalize(String identifier) {
        return identifier == null ? "unknown" : identifier.trim().toLowerCase(Locale.ROOT);
    }
}
