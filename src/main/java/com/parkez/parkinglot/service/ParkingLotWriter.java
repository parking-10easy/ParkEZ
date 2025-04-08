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
        validateUserNotNull(user);
        validateUserRoleIsOwner(user);
        ParkingLot parkingLot = request.toEntity(user);
        return parkingLotRepository.save(parkingLot);
    }

    // 주차장 수정
    public void updateParkingLot(User user, Long parkingLotId, ParkingLotRequest request) {
        ParkingLot parkingLot = getParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.update(request);
    }

    // 주차장 상태 변경
    public void updateParkingLotStatus(User user, Long parkingLotId, ParkingLotStatusRequest request) {
        ParkingLot parkingLot = getParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.updateStatus(request);
    }

    // 주차장 삭제
    public void deleteParkingLot(User user, Long parkingLotId) {
        ParkingLot parkingLot = getParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        parkingLot.softDelete(LocalDateTime.now());
    }

    // 주차장 이미지 수정
    public void updateParkingLotImages(User user, Long parkingLotId, ParkingLotImagesRequest request) {
        ParkingLot parkingLot = getParkingLot(parkingLotId);
        validateOwner(user, parkingLot);
        List<FakeImage> newImages = request.getImageUrls().stream()
                .map(url -> FakeImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }

    //  soft delete 제외 + null 처리하여 아이디로 단건 조회
    public ParkingLot getParkingLot (Long parkingLotId){
        return parkingLotRepository.findByIdAndDeletedAtIsNull(parkingLotId)
                .orElseThrow(() -> new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND));
    }

    // 사용자가 null 인지 확인
    // 에러 코드 추후 수정
    private void validateUserNotNull(User user) {
        if (user == null) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND_USER);
        }
    }

    // 사용자가 owner 인지 확인
    private void validateUserRoleIsOwner(User user) {
        if (!UserRole.ROLE_OWNER.equals(user.getRole())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_OWNER);
        }
    }

    // 주차장의 소유자인지 확인
    private void validateUserIsOwnerOfParkingLot(User user, ParkingLot parkingLot) {
        if (parkingLot != null && !user.equals(parkingLot.getOwner())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
        }
    }

    // 세가지 검증
    private void validateOwner(User user, ParkingLot parkingLot) {
        validateUserNotNull(user);
        validateUserRoleIsOwner(user);
        validateUserIsOwnerOfParkingLot(user, parkingLot);
    }
}
