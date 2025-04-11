package com.parkez.parkinglot.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingLotWriter {

    private final ParkingLotRepository parkingLotRepository;

    // 주차장 생성
    public ParkingLot createParkingLot(ParkingLot parkingLot) {
        return parkingLotRepository.save(parkingLot);
    }

    // 주차장 삭제
    public void deleteParkingLot(ParkingLot parkingLot) {
        parkingLot.updateDeletedAt(LocalDateTime.now());
    }
}
