package com.learn.taskflow.identity.response;

import java.util.UUID;

public record RegistrationResponse(UUID userId, String email, String message) {
}
