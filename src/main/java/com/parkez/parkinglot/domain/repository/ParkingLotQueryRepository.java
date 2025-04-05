package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkingLotQueryRepository {

    // 주차장 다건 조회
    Page<ParkingLot> searchParkingLots(String name, String address, Pageable pageable);
}
