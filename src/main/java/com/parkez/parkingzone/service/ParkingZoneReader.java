package com.parkez.parkingzone.service;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingZoneReader {

    private final ParkingZoneRepository parkingZoneRepository;

    public Page<ParkingZoneResponse> getParkingZones(int page, int size, Long parkingLotId) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<ParkingZone> parkingZones = parkingZoneRepository.findAllOrderByModifiedAt(pageable, parkingLotId);

        return parkingZones.map(parkingZone -> new ParkingZoneResponse(
                parkingZone.getId(),
                parkingZone.getParkingLot().getId(),
                parkingZone.getName(),
                parkingZone.getImageUrl(),
                parkingZone.getStatus()
        ));
}

}
