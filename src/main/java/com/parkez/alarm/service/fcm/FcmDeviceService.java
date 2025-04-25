package com.parkez.alarm.service.fcm;

import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FcmDeviceService {

    private final FcmDeviceRepository fcmDeviceRepository;

    public FcmDevice registerDevice(Long userId, String token) {
        Optional<FcmDevice> existingDeviceOpt = fcmDeviceRepository.findByTokenAndStatusTrue(token);

        existingDeviceOpt.ifPresent(existingDevice -> {
            existingDevice.updateStatus(false);
            fcmDeviceRepository.save(existingDevice);
        });

        FcmDevice newDevice = FcmDevice.of(userId, token);
        return fcmDeviceRepository.save(newDevice);
    }
}
