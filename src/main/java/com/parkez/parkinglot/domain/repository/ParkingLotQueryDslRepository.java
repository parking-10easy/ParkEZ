package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ParkingLotQueryDslRepository {

    // 주차장 다건 조회
    Page<ParkingLot> searchParkingLotsByConditions(String name, String address, Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable);

    // 주차장 단건 조회
    Optional<ParkingLot> searchParkingLotById(Long parkingLotId);

    // 본인이 소유한 주차장 조회
    Page<ParkingLot> findMyParkingLots(Long userId, Pageable pageable);
}
