package com.learn.taskflow.identity.security.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.OtpChallenge;
import com.learn.taskflow.identity.entity.SecurityChallenge;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.OtpDeliveryChannel;
import com.learn.taskflow.identity.enums.SecurityChallengeStatus;
import com.learn.taskflow.identity.repository.OtpChallengeRepository;
import com.learn.taskflow.identity.repository.SecurityChallengeRepository;
import com.learn.taskflow.identity.service.AccountStatePolicy;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class OtpAuthenticationService {

    private final OtpChallengeRepository otpChallengeRepository;
    private final SecurityChallengeRepository securityChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountStatePolicy accountStatePolicy;


    public OtpChallenge loadByChallengeId(UUID challengeId) {
        return otpChallengeRepository
                .findById(challengeId)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Invalid credentials"));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = BadCredentialsException.class)
    public User verify(UUID challengeId, String otp) {
        OtpChallenge otpChallenge = loadByChallengeId(challengeId);
        SecurityChallenge challenge = otpChallenge.getSecurityChallenge();

        if (challenge == null) {
            throw new BadCredentialsException("Invalid credentials");
        }
        accountStatePolicy.requireActive(challenge.getUser());

        if (challenge.getStatus() != SecurityChallengeStatus.PENDING
                || challenge.getExpiresAt().isBefore(LocalDateTime.now())
                || challenge.getAttemptCount() >= challenge.getMaxAttempts()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (otp == null || !passwordEncoder.matches(otp, otpChallenge.getCodeHash())) {
            challenge.setAttemptCount(challenge.getAttemptCount() + 1);
            if (challenge.getAttemptCount() >= challenge.getMaxAttempts()) {
                challenge.setStatus(SecurityChallengeStatus.FAILED);
            }
            throw new BadCredentialsException("Invalid credentials");
        }

        challenge.setStatus(SecurityChallengeStatus.VERIFIED);
        challenge.setVerifiedAt(LocalDateTime.now());
        return challenge.getUser();
    }
    @Transactional
    public OtpChallenge create(UUID challengeId) {
        SecurityChallenge challenge = securityChallengeRepository.findById(challengeId)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid challenge"));

        if (challenge.getUser() == null
                || challenge.getStatus() != SecurityChallengeStatus.PENDING
                || challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("Invalid challenge");
        }

        String otp = generateOtp();

        String codeHash = passwordEncoder.encode(otp);
        
        
        OtpChallenge otpChallenge = OtpChallenge.builder()
            .id(challengeId)
            .securityChallenge(challenge)
            .codeHash(codeHash)
            .deliveryChannel(OtpDeliveryChannel.EMAIL)
            .destination(challenge.getUser().getEmail())
            .build();
        
        otpChallengeRepository.save(otpChallenge);

        return otpChallenge;
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        return String.format("%06d", random.nextInt(1_000_000));
    }
}