package com.parkez.parkingzone.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
