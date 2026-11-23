package com.learn.taskflow.identity.request;

import com.learn.taskflow.identity.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DeviceRequest(
        @NotBlank @Size(max = 255) String deviceIdentifier,
        @NotNull DeviceType deviceType,
        @NotBlank @Size(max = 255) String operatingSystem,
        @Size(max = 255) String deviceName) {
}
