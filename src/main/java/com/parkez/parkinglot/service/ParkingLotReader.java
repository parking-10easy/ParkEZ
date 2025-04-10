package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ParkingLotReader {

    private final ParkingLotRepository parkingLotRepository;

    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 조회 메서드 */
    public ParkingLot getOwnedParkingLot(Long userId, Long parkingLotId){
        ParkingLot parkingLot = getActiveParkingLot(parkingLotId);

        if (!parkingLot.isOwned(userId)) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
        }

        return parkingLot;
    }

    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 조회 메서드 */
    private ParkingLot getActiveParkingLot(Long parkingLotId) {
        return parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 존재확인 메서드 */
    public void validateExistence(Long parkingLotId) {
        boolean exists = parkingLotRepository.existsByIdAndDeletedAtIsNull(parkingLotId);
        if (!exists) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND);
        }
    }
}
