package com.parkez.alarm.service.fcm;

import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmDeviceService {

    private final FcmDeviceRepository deviceRepository;

    public FcmDevice registerDevice(Long userId, String token) {
        Optional<FcmDevice> existingDeviceOpt = deviceRepository.findByTokenAndStatusTrue(token);

        existingDeviceOpt.ifPresent(existingDevice -> {
            existingDevice.updateStatus(false);
            deviceRepository.save(existingDevice);
        });

        FcmDevice newDevice = FcmDevice.of(userId, token);
        return deviceRepository.save(newDevice);
    }
}
