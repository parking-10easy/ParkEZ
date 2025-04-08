package com.parkez.parkinglot.service;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.entity.ParkingLotImage;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.repository.ParkingLotRepository;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.user.domain.entity.User;
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
        return parkingLotRepository.save(parkingLot);
    }

    // 주차장 수정
    public void updateParkingLot(ParkingLot parkingLot, ParkingLotRequest request) {
        parkingLot.update(
                request.getName(), request.getAddress(),
                request.getOpenedAt(), request.getClosedAt(),
                request.getPricePerHour(), request.getDescription(), request.getQuantity()
        );
    }

    // 주차장 상태 변경
    public void updateParkingLotStatus(ParkingLot parkingLot, ParkingLotStatus newStatus) {
        parkingLot.updateStatus(newStatus);
    }

    // 주차장 삭제
    public void deleteParkingLot(ParkingLot parkingLot) {
        parkingLot.updateDeletedAt(LocalDateTime.now());
    }

    // 주차장 이미지 수정
    public void updateParkingLotImages(ParkingLot parkingLot, ParkingLotImagesRequest request) {
        List<ParkingLotImage> newImages = request.getImageUrls().stream()
                .map(url -> ParkingLotImage.builder().imageUrl(url).parkingLot(parkingLot).build())
                .toList();
        parkingLot.updateImages(newImages);
    }
}
