package com.parkez.parkingzone.service;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingZoneQueryService {

    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingZone findById(@NotNull Long parkingZoneId) {
        return parkingZoneRepository.findById(parkingZoneId).orElseThrow(
                () -> new IllegalArgumentException("주차장 없음")
        );
    }
}
