package com.parkez.alarm.domain.repository;

import com.parkez.alarm.domain.entity.FcmDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FcmDeviceRepository extends JpaRepository<FcmDevice, Long> {
    // Optional<FcmDevice> findByUserIdAndToken(Long userId, String token);
    Optional<FcmDevice> findFirstByUserId(Long userId);
    Optional<FcmDevice> findByTokenAndStatusTrue(String token);
}
