package com.learn.taskflow.identity.service;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.Role;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.AccountStatus;
import com.learn.taskflow.identity.enums.RoleName;
import com.learn.taskflow.identity.repository.RoleRepository;
import com.learn.taskflow.identity.repository.UserRepository;
import com.learn.taskflow.identity.request.RegisterRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final DeviceService deviceService;
    private final DistributedRateLimiter distributedRateLimiter;

    @Value("${security.rate-limit.register-per-ip:10}")
    private long registerPerIp;

    @Value("${identity.registration.require-email-verification:false}")
    private boolean requireEmailVerification;

    @Transactional
    public User register(RegisterRequest request, String ipAddress) {
        distributedRateLimiter.check("register", ipAddress, null, registerPerIp, 0);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new IllegalStateException("Email is already registered");
        }
        Role role = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new IllegalStateException("Default role is not configured"));
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .role(role)
                .accountStatus(AccountStatus.ACTIVE)
                .emailVerified(!requireEmailVerification)
                .build());
        if (request.device() != null) {
            deviceService.findOrCreate(user, request.device());
        }
        return user;
    }
}
