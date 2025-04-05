package com.parkez.parkingzone.dto.response;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ParkingZoneCreateResponse {

    private final Long id;
    private final Long parkingLotId;
    private final String name;
    private final String imageUrl;
    private final ParkingZoneStatus status;

    public static ParkingZoneCreateResponse from(ParkingZone parkingZone) {
        return new ParkingZoneCreateResponse(
                parkingZone.getId(),
                parkingZone.getParkingLot().getId(),
                parkingZone.getName(),
                parkingZone.getImageUrl(),
                parkingZone.getStatus()
        );
    }
}
