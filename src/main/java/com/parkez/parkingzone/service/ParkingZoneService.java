package com.parkez.parkingzone.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.dto.request.*;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingZoneService {

    private final ParkingZoneWriter parkingZoneWriter;
    private final ParkingZoneReader parkingZoneReader;
    private final ParkingLotReader parkingLotReader;

    public ParkingZoneCreateResponse createParkingZone(ParkingZoneCreateRequest request) {
        ParkingLot parkingLot = parkingLotReader.getParkingLot(request.getParkingLotId());
        return ParkingZoneCreateResponse.from(parkingZoneWriter.createParkingZone(request, parkingLot));
    }

    public Page<ParkingZoneResponse> getParkingZones(PageRequest pageRequest, Long parkingLotId) {
        return parkingZoneReader.getParkingZones(pageRequest.getPage(), pageRequest.getSize(), parkingLotId);
    }

    public ParkingZoneResponse getParkingZone(Long parkingZoneId) {
        return ParkingZoneResponse.from(parkingZoneReader.getParkingZone(parkingZoneId));
    }

    public ParkingZoneResponse updateParkingZone(Long parkingZoneId, ParkingZoneUpdateRequest request) {
        return ParkingZoneResponse.from(parkingZoneWriter.updateParkingZone(parkingZoneId, request));
    }

    public ParkingZoneResponse updateParkingZoneStatus(Long parkingZoneId, ParkingZoneUpdateStatusRequest request) {
        return ParkingZoneResponse.from(parkingZoneWriter.updateParkingZoneStatus(parkingZoneId, request));
    }

    public ParkingZoneResponse updateParkingZoneImage(Long parkingZoneId, ParkingZoneUpdateImageRequest request) {
        return ParkingZoneResponse.from(parkingZoneWriter.updateParkingZoneImage(parkingZoneId, request));
    }

    public void deleteParkingZone(Long parkingZoneId) {
        parkingZoneWriter.deleteParkingZone(parkingZoneId);
    }
}
