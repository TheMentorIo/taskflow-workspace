package com.learn.taskflow.identity.service;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.learn.taskflow.identity.entity.Device;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.enums.DeviceType;
import com.learn.taskflow.identity.repository.DeviceRepository;
import com.learn.taskflow.identity.request.DeviceRequest;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository repository;

    @Test
    void upsertReReadsTheWinnerAfterConcurrentInsert() {
        User user = User.builder().id(UUID.randomUUID()).build();
        DeviceRequest request = new DeviceRequest("client-device", DeviceType.MOBILE, "Android", "Phone");
        Device winner = Device.builder().id(UUID.randomUUID()).user(user)
                .deviceIdentifier(request.deviceIdentifier())
                .deviceType(request.deviceType())
                .operatingSystem(request.operatingSystem())
                .build();

        when(repository.findByUserIdAndDeviceIdentifier(user.getId(), request.deviceIdentifier()))
                .thenReturn(Optional.empty(), Optional.of(winner));
        when(repository.findById(any(UUID.class))).thenReturn(Optional.empty());

        Device result = new DeviceService(repository).findOrCreate(user, request);

        assertThat(result).isSameAs(winner);
        verify(repository).insertIfAbsent(any(UUID.class), eq(user.getId()), eq("client-device"),
                eq("MOBILE"), eq("Android"), eq("Phone"));
    }
}
