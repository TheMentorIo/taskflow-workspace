package com.learn.taskflow.identity.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.request.LoginRequest;
import com.learn.taskflow.identity.request.RefreshTokenRequest;
import com.learn.taskflow.identity.request.RegisterRequest;
import com.learn.taskflow.identity.request.VerifyOtpRequest;
import com.learn.taskflow.identity.response.MessageResponse;
import com.learn.taskflow.identity.response.RegistrationResponse;
import com.learn.taskflow.identity.response.TokenResponse;
import com.learn.taskflow.identity.response.UserResponse;
import com.learn.taskflow.identity.service.ApiAuthenticationService;
import com.learn.taskflow.identity.service.SessionService;
import com.learn.taskflow.identity.service.UserRegistrationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final ApiAuthenticationService authenticationService;
    private final UserRegistrationService registrationService;
    private final SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {
        User user = registrationService.register(request, httpRequest.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(user.getId(), user.getEmail(), "Registration successful"));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return authenticationService.login(request, httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent"), httpRequest.getHeader("Sec-CH-UA"));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        return authenticationService.refresh(request.refreshToken(), httpRequest.getRemoteAddr());
    }

    @PostMapping("/logout")
    public MessageResponse logout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String sessionId = authentication.getDetails() instanceof UUID id ? id.toString() : null;
        if (sessionId == null) {
            throw new IllegalArgumentException("Session context is missing");
        }
        sessionService.revoke(UUID.fromString(sessionId), user.getId());
        return new MessageResponse("Logged out");
    }

    @PostMapping("/logout-all")
    public MessageResponse logoutAll(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        sessionService.revokeAll(user.getId());
        return new MessageResponse("All sessions revoked");
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return authenticationService.currentUser((User) authentication.getPrincipal());
    }

    @PostMapping("/challenges/{challengeId}/verify")
    public TokenResponse verifyOtp(@PathVariable UUID challengeId,
            @Valid @RequestBody VerifyOtpRequest request, HttpServletRequest httpRequest) {
        return authenticationService.verifyOtp(challengeId, request,
                httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"),
                httpRequest.getHeader("Sec-CH-UA"));
    }
}
