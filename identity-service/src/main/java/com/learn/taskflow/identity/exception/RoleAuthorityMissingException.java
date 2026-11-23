package com.learn.taskflow.identity.exception;

import java.util.UUID;

public class RoleAuthorityMissingException extends RuntimeException {
    public RoleAuthorityMissingException(UUID userId) {
        super("User role must have at least one authority: " + userId);
    }
}