package com.parkez.parkingzone.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingZoneService {

    private final ParkingZoneCommandService parkingZoneCommandService;
    private final ParkingZoneReader parkingZoneQueryService;
}
