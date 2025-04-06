package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingZoneQueryService {

    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingZone findById(Long parkingZoneId) {
        return parkingZoneRepository.findById(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ReservationErrorCode.NOT_FOUND_PARKING_ZONE)
        );
    }

    public boolean existsById(Long parkingZoneId) {
        return parkingZoneRepository.existsById(parkingZoneId);
    }
}
