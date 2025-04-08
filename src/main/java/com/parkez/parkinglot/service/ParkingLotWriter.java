package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.FakeImage;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParkingLotWriter {

    private final ParkingLotRepository parkingLotRepository;

    // 주차장 생성
    public ParkingLot createParkingLot(User user, ParkingLotRequest request) {
        validateOwner(user);
        ParkingLot parkingLot = request.toEntity(user);
        return parkingLotRepository.save(parkingLot);
    }

    // 주차장 수정
    public void updateParkingLot(User user, Long parkingLotId, ParkingLotRequest request) {
        ParkingLot parkingLot = validateAndGetParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.update(request);
    }

    // 주차장의 Owner인지 검증
    private void validateOwner(User user, ParkingLot parkingLot) {
        if (!user.equals(parkingLot.getOwner())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_OWNER);
        }
    }

    // Owner 검증
    private void validateOwner(User user) {
        if (!UserRole.ROLE_OWNER.equals(user.getRole())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_OWNER);
        }
    }

    // 주차장 상태 변경
    public void updateParkingLotStatus(User user, Long parkingLotId, ParkingLotStatusRequest request) {
        ParkingLot parkingLot = validateAndGetParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.updateStatus(request);
    }

    // 주차장 삭제
    public void deleteParkingLot(User user, Long parkingLotId) {
        ParkingLot parkingLot = validateAndGetParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.softDelete(LocalDateTime.now());
    }

    // 주차장 이미지 수정
    public void updateParkingLotImages(User user, Long parkingLotId, ParkingLotImagesRequest request) {
        ParkingLot parkingLot = validateAndGetParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        List<FakeImage> newImages = request.getImageUrls().stream()
                .map(url -> FakeImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }

    //  soft delete 조건 + null 처리 단건 조회
    public ParkingLot validateAndGetParkingLot(Long parkingLotId) {
        ParkingLot parkingLot = parkingLotRepository.findById(parkingLotId)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
        if (parkingLot.getDeletedAt() != null) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND);
        }
        return parkingLot;
    }
}
