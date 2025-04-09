package com.parkez.parkinglot.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotWriter parkingLotWriter;
    private final ParkingLotReader parkingLotReader;

    // 주차장 생성
    public ParkingLotResponse createParkingLot(User user, ParkingLotRequest request) {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(user)
                .name(request.getName())
                .quantity(request.getQuantity())
                .closedAt(request.getClosedAt())
                .openedAt(request.getOpenedAt())
                .pricePerHour(request.getPricePerHour())
                .description(request.getDescription())
                .address(request.getAddress())
                .build();
        return ParkingLotResponse.from(parkingLotWriter.createParkingLot(parkingLot));
    }

    // 주차장 다건 조회 (이름, 주소)
    public Page<ParkingLotSearchResponse> searchParkingLots(ParkingLotSearchRequest request, Pageable pageable) {
        return parkingLotReader.searchParkingLots(request.getName(), request.getAddress(), pageable);
    }

    // 주차장 단건 조회
    public ParkingLotSearchResponse searchParkingLot(Long parkingLotId) {
        return ParkingLotSearchResponse.from(parkingLotReader.searchParkingLot(parkingLotId));
    }

    // 주차장 수정 (writer 사용x)
    @Transactional
    public void updateParkingLot(User user, Long parkingLotId, ParkingLotRequest request) {
        ParkingLot parkingLot = parkingLotReader.getParkingLot(parkingLotId);
        validateUserIsOwnerOfParkingLot(user, parkingLot);
        parkingLot.update(
                request.getName(), request.getAddress(),
                request.getOpenedAt(), request.getClosedAt(),
                request.getPricePerHour(), request.getDescription(), request.getQuantity()
        );
    }

    // 주차장 상태 변경 (writer 사용x)
    @Transactional
    public void updateParkingLotStatus(User user, Long parkingLotId, ParkingLotStatusRequest request) {
        ParkingLot parkingLot = parkingLotReader.getParkingLot(parkingLotId);
        validateUserIsOwnerOfParkingLot(user, parkingLot);
        ParkingLotStatus newStatus;
        try {
            newStatus = ParkingLotStatus.valueOf(request.getStatus().toUpperCase());
            parkingLot.updateStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new ParkingEasyException(ParkingLotErrorCode.INVALID_PARKING_LOT_STATUS);
        }
    }

    // 주차장 이미지 수정 (writer 사용x)
    @Transactional
    public void updateParkingLotImages(User user, Long parkingLotId, ParkingLotImagesRequest request) {
        ParkingLot parkingLot = parkingLotReader.getParkingLot(parkingLotId);
        validateUserIsOwnerOfParkingLot(user, parkingLot);
        List<ParkingLotImage> newImages = request.getImageUrls().stream()
                .map(url -> ParkingLotImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }

    // 주차장 삭제
    public void deleteParkingLot(User user, Long parkingLotId) {
        ParkingLot parkingLot = parkingLotReader.getParkingLot(parkingLotId);
        validateUserIsOwnerOfParkingLot(user, parkingLot);
        parkingLotWriter.deleteParkingLot(parkingLot);
    }

    // 주차장의 소유자인지 확인
    private void validateUserIsOwnerOfParkingLot(User user, ParkingLot parkingLot) {
        if (!user.equals(parkingLot.getOwner())) {
            throw new ParkingEasyException(ParkingLotErrorCode.NOT_PARKING_LOT_OWNER);
        }
    }
}
