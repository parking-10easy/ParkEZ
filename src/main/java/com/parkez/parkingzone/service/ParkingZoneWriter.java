package com.parkez.parkingzone.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingZoneWriter {

    private final ParkingZoneRepository parkingZoneRepository;

    public ParkingZone createParkingZone(String name, String imageUrl, ParkingLot parkingLot) {
        ParkingZone parkingZone = ParkingZone.builder()
                .parkingLot(parkingLot)
                .name(name)
                .imageUrl(imageUrl)
                .build();
        return parkingZoneRepository.save(parkingZone);
    }

    public void deleteParkingZone(Long parkingZoneId, LocalDateTime deletedAt) {
        parkingZoneRepository.softDeleteById(parkingZoneId, deletedAt);
    }


}
