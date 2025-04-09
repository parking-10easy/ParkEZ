package com.parkez.parkingzone.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.dto.request.*;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ParkingZoneService {

    private final ParkingZoneWriter parkingZoneWriter;
    private final ParkingZoneReader parkingZoneReader;
    private final ParkingLotReader parkingLotReader;

    public ParkingZoneCreateResponse createParkingZone(AuthUser authUser, ParkingZoneCreateRequest request) {
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(authUser, request.getParkingLotId());
        return ParkingZoneCreateResponse.from(parkingZoneWriter.createParkingZone(request.getName(), request.getImageUrl(), parkingLot));
    }

    public Page<ParkingZoneResponse> getParkingZones(PageRequest pageRequest, Long parkingLotId) {
        parkingLotReader.validateExistence(parkingLotId);
        return parkingZoneReader.getParkingZones(pageRequest.getPage(), pageRequest.getSize(), parkingLotId);
    }

    public ParkingZoneResponse getParkingZone(Long parkingZoneId) {
        return ParkingZoneResponse.from(parkingZoneReader.getParkingZone(parkingZoneId));
    }

    public void updateParkingZone(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateRequest request) {
        parkingZoneWriter.updateParkingZone(authUser, parkingZoneId, request.getName());
    }

    public void updateParkingZoneStatus(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateStatusRequest request) {
        parkingZoneWriter.updateParkingZoneStatus(authUser, parkingZoneId, request.getStatus());
    }

    public void updateParkingZoneImage(AuthUser authUser, Long parkingZoneId, ParkingZoneUpdateImageRequest request) {
        parkingZoneWriter.updateParkingZoneImage(authUser, parkingZoneId, request.getImageUrl());
    }

    public void deleteParkingZone(AuthUser authUser, Long parkingZoneId, LocalDateTime deletedAt) {
        parkingZoneWriter.deleteParkingZone(authUser, parkingZoneId, deletedAt);
    }
}
