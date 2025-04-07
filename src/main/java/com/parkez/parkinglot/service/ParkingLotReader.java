package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.response.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingLotReader {

    private final ParkingLotRepository parkingLotRepository;

    // 주차장 전체 조회
    public Page<ParkingLotSearchResponse> searchParkingLots(ParkingLotSearchRequest request, Pageable pageable) {
        Page<ParkingLot> parkingLots = parkingLotRepository.searchParkingLots(request, pageable);
        return parkingLots.map(ParkingLotSearchResponse::from);
    }

    // 주차장 단건 조회
    public ParkingLot getParkingLot(Long parkingLotId) {
        return parkingLotRepository.findParkingLotById(parkingLotId).orElseThrow(
                () -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }
}
