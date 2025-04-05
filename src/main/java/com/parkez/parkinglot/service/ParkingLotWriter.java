package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingLotWriter {

    private final ParkingLotRepository parkingLotRepository;


    // 주차장 생성
    public ParkingLot createParkingLot(User user, ParkingLotRequest request) {
        validateOwner(user);
        ParkingLot parkingLot = ParkingLotRequest.toEntity(user, request);
        return parkingLotRepository.save(parkingLot);

    }

    // Owner 검증
    private void validateOwner(User user) {
        if (!UserRole.ROLE_OWNER.equals(user.getRole())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_OWNER);
        }
    }

}
