package com.parkez.parkinglot.service;

import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotWriter parkingLotWriter;
    private final ParkingLotReader parkingLotReader;


    public ParkingLotResponse createParkingLot(User user, ParkingLotRequest request) {
        return ParkingLotResponse.from(parkingLotWriter.createParkingLot(user, request) );
    }
}
