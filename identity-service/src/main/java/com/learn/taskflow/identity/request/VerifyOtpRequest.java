package com.learn.taskflow.identity.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyOtpRequest(
        @NotBlank @Pattern(regexp = "\\d{6}") @Size(min = 6, max = 6) String otp,
        @jakarta.validation.Valid DeviceRequest device) {
}
