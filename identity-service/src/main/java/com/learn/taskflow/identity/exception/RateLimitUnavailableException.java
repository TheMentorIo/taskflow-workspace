package com.learn.taskflow.identity.exception;

public class RateLimitUnavailableException extends RuntimeException {

    public RateLimitUnavailableException(Throwable cause) {
        super("Rate limiting service is unavailable", cause);
    }
}