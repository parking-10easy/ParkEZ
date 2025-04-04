package com.parkez.parkingzone.service;

import com.parkez.parkingzone.domain.repository.ParkingZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingZoneQueryService {

    private final ParkingZoneRepository parkingZoneRepository;

    public void findParkingZone () {
        parkingZoneRepository.findAll();
    }
}
