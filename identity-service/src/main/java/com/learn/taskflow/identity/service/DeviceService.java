package com.learn.taskflow.identity.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.learn.taskflow.identity.entity.Device;
import com.learn.taskflow.identity.entity.User;
import com.learn.taskflow.identity.repository.DeviceRepository;
import com.learn.taskflow.identity.request.DeviceRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    @Transactional
    public Device findOrCreate(User user, DeviceRequest request) {
        Device device = deviceRepository.findByUserIdAndDeviceIdentifier(user.getId(), request.deviceIdentifier())
            .orElseGet(() -> Device.builder()
                .id(UUID.randomUUID())
                .user(user)
                .deviceIdentifier(request.deviceIdentifier())
                .deviceType(request.deviceType())
                .operatingSystem(request.operatingSystem())
                .deviceName(request.deviceName())
                .createdAt(LocalDateTime.now())
                .build());
        device.setDeviceType(request.deviceType());
        device.setOperatingSystem(request.operatingSystem());
        device.setDeviceName(request.deviceName());
        if (deviceRepository.findById(device.getId()).isEmpty()) {
            deviceRepository.insertIfAbsent(device.getId(), user.getId(), device.getDeviceIdentifier(),
                device.getDeviceType().name(), device.getOperatingSystem(), device.getDeviceName());
            return deviceRepository.findByUserIdAndDeviceIdentifier(user.getId(), request.deviceIdentifier())
                .orElseThrow(() -> new IllegalStateException("Device could not be created"));
        }
        return deviceRepository.save(device);
    }

    @Transactional(readOnly = true)
    public Device requireOwned(UUID deviceId, UUID userId) {
        return deviceRepository.findByIdAndUserId(deviceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Device not found"));
    }
}
