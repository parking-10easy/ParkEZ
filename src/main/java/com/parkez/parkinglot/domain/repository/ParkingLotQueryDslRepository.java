package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkingLotQueryDslRepository {

    // 주차장 다건 조회
    Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address, Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable);

    // 주차장 단건 조회
    ParkingLotSearchResponse searchParkingLotById(Long parkingLotId);

    // 본인이 소유한 주차장 조회
    Page<ParkingLot> findMyParkingLots(Long userId, Pageable pageable);
}
