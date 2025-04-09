package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
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

    // 주차장 다건 조회 (이름, 주소)
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(String name, String address, Pageable pageable) {
        Page<ParkingLot> parkingLots = parkingLotRepository.searchParkingLotsByConditions(name, address, pageable);
        return parkingLots.map(ParkingLotSearchResponse::from);
    }

    // 주차장 단건 조회
    public ParkingLot searchParkingLotById(Long parkingLotId) {
        return parkingLotRepository.searchParkingLotById(parkingLotId).orElseThrow(
                () -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    //  soft delete 제외 + null 처리하여 아이디로 단건 조회 + authUser 본인확인
    public ParkingLot getOwnedParkingLot(AuthUser authUser, Long parkingLotId) {
        ParkingLot parkingLot = getActiveParkingLot(parkingLotId);
        checkParkingLotOwnership(authUser, parkingLot);
        return parkingLot;
    }

    private ParkingLot getActiveParkingLot(Long parkingLotId) {
        return parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    private void checkParkingLotOwnership(AuthUser authUser, ParkingLot parkingLot) {
        if (!parkingLot.isOwned(authUser.getId())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
        }
    }
}
