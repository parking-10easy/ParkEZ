package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingZoneWriter {

    private final ParkingZoneRepository parkingZoneRepository;
    private final ParkingZoneReader parkingZoneReader;

    public ParkingZone createParkingZone(String name, String imageUrl, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(name)
                .imageUrl(imageUrl)
                .build();
        return parkingZoneRepository.save(parkingZone);
    }

    public void updateParkingZone(AuthUser authUser, Long parkingZoneId, String name) {
        ParkingZone parkingZone = parkingZoneReader.getParkingZone(parkingZoneId);

        if (!parkingZone.isOwnedParkingLot(authUser.getId())) {
            throw new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
        }

        parkingZone.updateParkingZone(name);
    }

    public void updateParkingZoneStatus(AuthUser authUser, Long parkingZoneId, ParkingZoneStatus status) {
        ParkingZone parkingZone = parkingZoneReader.getParkingZone(parkingZoneId);

        if (!parkingZone.isOwnedParkingLot(authUser.getId())) {
            throw new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
        }

        parkingZone.updateParkingZoneStatus(status);
    }

    public void updateParkingZoneImage(AuthUser authUser, Long parkingZoneId, String imageUrl) {
        ParkingZone parkingZone = parkingZoneReader.getParkingZone(parkingZoneId);

        if (!parkingZone.isOwnedParkingLot(authUser.getId())) {
            throw new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
        }

        parkingZone.updateParkingZoneImage(imageUrl);
    }

    public void deleteParkingZone(AuthUser authUser, Long parkingZoneId, LocalDateTime deletedAt) {
        ParkingZone parkingZone = parkingZoneReader.getParkingZone(parkingZoneId);

        if (!parkingZone.isOwnedParkingLot(authUser.getId())) {
            throw new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_DELETE);
        }

        parkingZoneRepository.softDeleteById(parkingZoneId, deletedAt);
    }
}
