package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParkingLotSearchRepository {

    // 주차장 다건 조회
    Page<ParkingLot> searchParkingLots(String name, String address, Pageable pageable);

    // 주차장 단건 조회
    Optional<ParkingLot> searchParkingLot(Long parkingLotId);
}
