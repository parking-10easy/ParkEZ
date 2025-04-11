package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingZoneReader {

    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingZone findById(Long parkingZoneId) {
        return parkingZoneRepository.findById(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ReservationErrorCode.NOT_FOUND_PARKING_ZONE)
        );
    }

    public boolean existsById(Long parkingZoneId) {
        return parkingZoneRepository.existsById(parkingZoneId);
    }

    public ParkingZone findByIdWithPessimisticLock(Long parkingZoneId) {
        return parkingZoneRepository.findByIdWithPessimisticLock(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ReservationErrorCode.NOT_FOUND_PARKING_ZONE)
        );
    }
}
