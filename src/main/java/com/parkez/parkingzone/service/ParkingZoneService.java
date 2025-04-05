package com.parkez.parkingzone.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import lombok.RequiredArgsConstructor;
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
}
