package com.learn.taskflow.identity.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.Device;
import com.learn.taskflow.identity.entity.Session;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.repository.UserRepository;
import com.learn.taskflow.identity.request.DeviceRequest;
import com.learn.taskflow.identity.request.LoginRequest;
import com.learn.taskflow.identity.request.VerifyOtpRequest;
import com.learn.taskflow.identity.response.TokenResponse;
import com.learn.taskflow.identity.response.UserResponse;
import com.learn.taskflow.identity.security.service.JwtAuthenticationService;
import com.learn.taskflow.identity.security.service.UserAuthorityResolverService;
import com.learn.taskflow.identity.security.token.OtpAuthenticationToken;
import com.learn.taskflow.identity.security.token.PasswordEmailAuthenticationToken;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final DeviceService deviceService;
    private final SessionService sessionService;
    private final UserAuthorityResolverService authorityResolver;
    private final JwtAuthenticationService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationAttemptPolicy authenticationAttemptPolicy;
    private final AccountStatePolicy accountStatePolicy;
    private final DistributedRateLimiter distributedRateLimiter;

    @Value("${security.rate-limit.refresh-per-ip:60}")
    private long refreshPerIp;

    @Value("${security.session.access-duration-seconds:900}")
    private long accessDurationSeconds;

    @Value("${security.session.refresh-duration-seconds:2592000}")
    private long refreshDurationSeconds;

    @Transactional
    public TokenResponse login(LoginRequest request, String ip, String userAgent, String browser) {
        authenticationAttemptPolicy.checkAllowed(request.email(), ip);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new PasswordEmailAuthenticationToken(request.email().trim(), request.password()));
        } catch (AuthenticationException exception) {
            authenticationAttemptPolicy.recordFailure(request.email(), ip, userAgent,
                com.learn.taskflow.identity.enums.AuthenticationFailureReason.INVALID_CREDENTIALS);
            throw exception;
        }
        User user = (User) authentication.getPrincipal();
        authenticationAttemptPolicy.recordSuccess(user, request.email(), ip, userAgent);
        Device device = optionalDevice(user, request.device());
        Session session = sessionService.create(user, device, ip, userAgent, browser);
        return tokens(user, session);
    }

    @Transactional
    public TokenResponse verifyOtp(UUID challengeId, VerifyOtpRequest request,
            String ip, String userAgent, String browser) {
        authenticationAttemptPolicy.checkAllowed(challengeId.toString(), ip);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new OtpAuthenticationToken(challengeId, request.otp()));
        } catch (AuthenticationException exception) {
            authenticationAttemptPolicy.recordFailure(challengeId.toString(), ip, userAgent,
                com.learn.taskflow.identity.enums.AuthenticationFailureReason.INVALID_CREDENTIALS,
                challengeId);
            throw exception;
        }
        User user = (User) authentication.getPrincipal();
        authenticationAttemptPolicy.recordSuccess(user, challengeId.toString(), ip, userAgent, challengeId);
        Device device = optionalDevice(user, request.device());
        Session session = sessionService.create(user, device, ip, userAgent, browser);
        return tokens(user, session);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken, String ipAddress) {
        distributedRateLimiter.check("refresh", ipAddress, null, refreshPerIp, 0);
        com.learn.taskflow.identity.entity.RefreshToken current = refreshTokenService.consume(refreshToken);
        User user = userRepository.findByIdAndDeletedAtIsNull(current.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        accountStatePolicy.requireActive(user);
        Session session = sessionService.requireActive(current.getSession().getId(), user.getId());
        RefreshTokenService.IssuedRefreshToken rotated = refreshTokenService.rotate(current,
                Instant.now().plusSeconds(refreshDurationSeconds).atZone(java.time.ZoneOffset.UTC).toLocalDateTime());
        return tokens(user, session, rotated.value());
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(User user) {
        return toResponse(user);
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getAccountStatus().name(), user.isEmailVerified(), authorityResolver.resolve(user).stream()
                        .map(authority -> authority.getAuthority()).collect(java.util.stream.Collectors.toUnmodifiableSet()));
    }

    private TokenResponse tokens(User user, Session session) {
        long accessExpiry = Instant.now().plusSeconds(accessDurationSeconds).toEpochMilli();
        RefreshTokenService.IssuedRefreshToken issued = refreshTokenService.issue(user, session,
                Instant.now().plusSeconds(refreshDurationSeconds).atZone(java.time.ZoneOffset.UTC).toLocalDateTime());
        return tokens(user, session, issued.value(), accessExpiry);
    }

    private TokenResponse tokens(User user, Session session, String refreshToken) {
        long accessExpiry = Instant.now().plusSeconds(accessDurationSeconds).toEpochMilli();
        return tokens(user, session, refreshToken, accessExpiry);
    }

    private TokenResponse tokens(User user, Session session, String refreshToken, long accessExpiry) {
        return new TokenResponse("Bearer",
                jwtService.generateAccessToken(user.getId(), session.getId(), accessExpiry),
                refreshToken,
                accessDurationSeconds, session.getId(), toResponse(user));
    }

    private Device optionalDevice(User user, DeviceRequest device) {
        return device == null ? null : deviceService.findOrCreate(user, device);
    }
}
