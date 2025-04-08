package com.parkez.parkingzone.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
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

    public ParkingZone createParkingZone(ParkingZoneCreateRequest request, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(request.getName())
                .imageUrl(request.getImageUrl())
                .build();
        return parkingZoneRepository.save(parkingZone);
    }

    public void updateParkingZone(Long parkingZoneId, ParkingZoneUpdateRequest request) {
        ParkingZone parkingZone = parkingZoneRepository.findByIdAndDeletedAtIsNull(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
        );

//        if (!parkingZone.getParkingLot().getOwner().getId().equals(authUser.getId())){
//            new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
//        }

        parkingZone.updateParkingZone(request.getName());
    }

    public void updateParkingZoneStatus(Long parkingZoneId, ParkingZoneUpdateStatusRequest request) {
        ParkingZone parkingZone = parkingZoneRepository.findByIdAndDeletedAtIsNull(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
        );

//        if (!parkingZone.getParkingLot().getOwner().getId().equals(authUser.getId())){
//            new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
//        }

        parkingZone.updateParkingZoneStatus(request.getStatus());
    }

    public void updateParkingZoneImage(Long parkingZoneId, ParkingZoneUpdateImageRequest request) {
        ParkingZone parkingZone = parkingZoneRepository.findByIdAndDeletedAtIsNull(parkingZoneId).orElseThrow(
                () -> new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND)
        );

//        if (!parkingZone.getParkingLot().getOwner().getId().equals(authUser.getId())){
//            new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
//        }

        parkingZone.updateParkingZoneImage(request.getImageUrl());
    }

    public void deleteParkingZone(Long parkingZoneId) {
        if (!parkingZoneRepository.existsByIdAndDeletedAtIsNull(parkingZoneId)){
            throw new ParkingEasyException(ParkingZoneErrorCode.PARKING_ZONE_NOT_FOUND);
        }

//        if (!parkingZone.getParkingLot().getOwner().getId().equals(authUser.getId())){
//            new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_UPDATE);
//        }

        parkingZoneRepository.softDeleteById(parkingZoneId, LocalDateTime.now());
    }
}
