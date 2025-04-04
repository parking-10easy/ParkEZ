package com.parkez.parkinglot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotCommandService parkingLotCommandService;
    private final ParkingLotQueryService parkingLotQueryService;
}
