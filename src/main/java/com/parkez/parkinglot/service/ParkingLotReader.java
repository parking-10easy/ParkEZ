package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingLotReader {

    private final ParkingLotRepository parkingLotRepository;

    // 주차장 다건 조회 (이름, 주소)
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ParkingLot> parkingLots = parkingLotRepository.searchParkingLotsByConditions(name, address, pageable);
        return parkingLots.map(ParkingLotSearchResponse::from);
    }

    // 주차장 단건 조회
    public ParkingLot searchParkingLotById(Long parkingLotId) {
        return parkingLotRepository.searchParkingLotById(parkingLotId).orElseThrow(
                () -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    // 본인이 소유한 주차장 조회
    public Page<MyParkingLotSearchResponse> getMyParkingLots(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ParkingLot> parkingLots = parkingLotRepository.findMyParkingLots(userId, pageable);
        return parkingLots.map(MyParkingLotSearchResponse::from);
    }

    //  soft delete 제외 + null 처리하여 아이디로 단건 조회 + authUser 본인확인
    public ParkingLot getOwnedParkingLot(Long userId, Long parkingLotId) {
        ParkingLot parkingLot = getActiveParkingLot(parkingLotId);
        checkParkingLotOwnership(userId, parkingLot);
        return parkingLot;
    }

    private ParkingLot getActiveParkingLot(Long parkingLotId) {
        return parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    private void checkParkingLotOwnership(Long userId, ParkingLot parkingLot) {
        if (!parkingLot.isOwned(userId)) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);

    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 존재확인 메서드 */
    public void validateExistence(Long parkingLotId) {
        boolean exists = parkingLotRepository.existsByIdAndDeletedAtIsNull(parkingLotId);
        if (!exists) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND);
        }
    }
}