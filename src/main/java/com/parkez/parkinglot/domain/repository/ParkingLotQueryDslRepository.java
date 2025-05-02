package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.dto.aggregation.ParkingLotAggregation;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ParkingLotQueryDslRepository {

    // 주차장 다건 조회
    Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address, Double userLatitude, Double userLongitude, Integer radiusInMeters, Pageable pageable);

    // 주차장 단건 조회
    ParkingLotSearchResponse searchParkingLotById(Long parkingLotId);

    // 본인이 소유한 주차장 조회
    Page<MyParkingLotSearchResponse> findMyParkingLots(Long userId, Pageable pageable);

    // 이미지 조회
    List<String> findImageListByParkingLotId(Long parkingLotId);

    // 조회에 필요한 집계
    Optional<ParkingLotAggregation> getAggregationByParkingLotId(Long parkingLotId);
}
