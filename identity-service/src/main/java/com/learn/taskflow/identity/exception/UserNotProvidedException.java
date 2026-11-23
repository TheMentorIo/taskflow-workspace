package com.learn.taskflow.identity.exception;

public class UserNotProvidedException extends RuntimeException {
    public UserNotProvidedException() {
        super("User must be provided");
    }
}