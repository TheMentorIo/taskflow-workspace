package com.learn.taskflow.identity.response;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String accountStatus,
        boolean emailVerified,
        Set<String> authorities) {
}
