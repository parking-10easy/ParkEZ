package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.dto.response.ParkingLotSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ParkingLotQueryRepository {

    // 주차장 다건 조회
    Page<ParkingLot> searchParkingLots(ParkingLotSearchRequest request, Pageable pageable);

    // 주차장 단건 조회
    ParkingLot findParkingLotById(Long parkingLotId);
}
