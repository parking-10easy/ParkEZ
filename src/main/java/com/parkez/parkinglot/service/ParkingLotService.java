package com.parkez.parkinglot.service;

import com.parkez.common.PageRequest;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingLotService {

    private final ParkingLotWriter parkingLotWriter;
    private final ParkingLotReader parkingLotReader;
    private final UserReader userReader;

    @Value("${parking-lot.default-image-url}")
    private String defaultParkingLotImageUrl;

    // 주차장 생성
    public ParkingLotResponse createParkingLot(AuthUser authUser, ParkingLotRequest request) {
        User user = userReader.getActiveById(authUser.getId());

        ParkingLot parkingLot = ParkingLot.builder()
                .owner(user)
                .name(request.getName())
                .quantity(request.getQuantity())
                .closedAt(request.getClosedAt())
                .openedAt(request.getOpenedAt())
                .pricePerHour(request.getPricePerHour())
                .description(request.getDescription())
                .address(request.getAddress())
                .chargeType(ChargeType.PAID)
                .sourceType(SourceType.OWNER_REGISTERED)
                .build();

        ParkingLotImage defaultImage = ParkingLotImage.builder()
                .imageUrl(defaultParkingLotImageUrl)
                .build();
        parkingLot.addImage(defaultImage);

        return ParkingLotResponse.from(parkingLotWriter.createParkingLot(parkingLot));
    }

    // 주차장 다건 조회 (이름, 주소)
    public Page<ParkingLotSearchResponse> searchParkingLotsByConditions(ParkingLotSearchRequest request, PageRequest pageRequest) {
        return parkingLotReader.searchParkingLotsByConditions(request.getName(), request.getAddress(), pageRequest.getPage(), pageRequest.getSize());
    }

    // 주차장 단건 조회
    public ParkingLotSearchResponse searchParkingLotById(Long parkingLotId) {
        return ParkingLotSearchResponse.from(parkingLotReader.searchParkingLotById(parkingLotId));
    }

    // 본인이 소유한 주차장 조회
    public Page<MyParkingLotSearchResponse> getMyParkingLots(AuthUser authUser, PageRequest pageRequest) {
        Long userId = authUser.getId();
        return parkingLotReader.getMyParkingLots(userId, pageRequest.getPage(), pageRequest.getSize());
    }

    // 주차장 수정 (writer 사용x)
    @Transactional
    public void updateParkingLot(AuthUser authUser, Long parkingLotId, ParkingLotRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        parkingLot.update(
                request.getName(), request.getAddress(),
                request.getOpenedAt(), request.getClosedAt(),
                request.getPricePerHour(), request.getDescription(), request.getQuantity()
        );
    }

    // 주차장 상태 변경 (writer 사용x)
    @Transactional
    public void updateParkingLotStatus(AuthUser authUser, Long parkingLotId, ParkingLotStatusRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        ParkingLotStatus newStatus = ParkingLotStatus.from(request.getStatus());
        parkingLot.updateStatus(newStatus);
    }

    // 주차장 이미지 수정 (writer 사용x)
    @Transactional
    public void updateParkingLotImages(AuthUser authUser, Long parkingLotId, ParkingLotImagesRequest request) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        List<ParkingLotImage> newImages = request.getImageUrls().stream()
                .map(url -> ParkingLotImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }

    // 주차장 삭제
    public void deleteParkingLot(AuthUser authUser, Long parkingLotId) {
        Long userId = authUser.getId();
        ParkingLot parkingLot = parkingLotReader.getOwnedParkingLot(userId, parkingLotId);
        parkingLotWriter.deleteParkingLot(parkingLot);
    }

}
