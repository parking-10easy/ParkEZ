package com.parkez.parkingzone.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateNameRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.exception.ParkingZoneErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingZoneService {

    private final ParkingZoneWriter parkingZoneWriter;
    private final ParkingZoneReader parkingZoneReader;
    private final ParkingLotReader parkingLotReader;

    @Value("${parking-zone.default-image-url}")
    private String defaultImageUrl;

    public ParkingZoneResponse createParkingZone(AuthUser authUser, ParkingZoneCreateRequest request) {
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(authUser.getId(), request.getParkingLotId());
        validateNotPublicData(parkingLot);
        return ParkingZoneResponse.from(parkingZoneWriter.createParkingZone(request.getName(), defaultImageUrl, parkingLot));
    }

    public Page<ParkingZoneResponse> getParkingZones(PageRequest pageRequest, Long parkingLotId) {
        parkingLotReader.validateExistence(parkingLotId);
        return parkingZoneReader.getParkingZones(pageRequest.getPage(), pageRequest.getSize(), parkingLotId);
    }

    public ParkingZoneResponse getParkingZone(Long parkingZoneId) {
        return ParkingZoneResponse.from(parkingZoneReader.getActiveByParkingZoneId(parkingZoneId));
    }

    @Transactional
    public void updateParkingZoneName(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateNameRequest request) {
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(parkingZoneId);
        validateOwner(parkingZone, authUser.getId());
        parkingZone.updateParkingZoneName(request.getName());
    }

    @Transactional
    public void updateParkingZoneStatus(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateStatusRequest request) {
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(parkingZoneId);
        ParkingZoneStatus newStatus = ParkingZoneStatus.from(request.getStatus());
        validateOwner(parkingZone, authUser.getId());
        parkingZone.updateParkingZoneStatus(newStatus);
    }

    @Transactional
    public void updateParkingZoneImage(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateImageRequest request) {
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(parkingZoneId);
        validateOwner(parkingZone, authUser.getId());
        parkingZone.updateParkingZoneImage(request.getImageUrl());
    }

    public void deleteParkingZone(AuthUser authUser, Long parkingZoneId, LocalDateTime deletedAt) {
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(parkingZoneId);
        validateOwner(parkingZone, authUser.getId());
        parkingZoneWriter.deleteParkingZone(parkingZoneId, deletedAt);
    }

    private static void validateNotPublicData(ParkingLot parkingLot) {
        if (parkingLot.isPublicData()){
            throw new ParkingEasyException(ParkingZoneErrorCode.PUBLIC_DATA_CREATION_NOT_ALLOWED);
        }
    }

    private void validateOwner(ParkingZone parkingZone, Long ownerId) {
        boolean isOwned = parkingZoneReader.isOwnedParkingZone(parkingZone.getId(), ownerId);
        if (!isOwned){
            throw new ParkingEasyException(ParkingZoneErrorCode.FORBIDDEN_TO_ACTION);
        }
    }
}
