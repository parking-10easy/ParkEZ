package com.parkez.parkinglot.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
public class ParkingLotSearchResponse {
    private final Long parkingLotId;
    private final String name;
    private final String address;
    private final LocalTime openedAt;
    private final LocalTime closedAt;
    private final BigDecimal pricePerHour;
    private final Integer totalQuantity;
    private final Integer availableQuantity;
    private final ChargeType chargeType;
    private final SourceType sourceType;
    private final ParkingLotStatus parkingLotStatus;
    private final Integer reviewCount;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0")
    private final Double averageRating;
    private final List<String> images; // 이미지 수정 필요

    @Builder
    public ParkingLotSearchResponse(Long parkingLotId, String name, String address,
                                    LocalTime openedAt, LocalTime closedAt, BigDecimal pricePerHour,
                                    Integer totalQuantity, Integer availableQuantity,
                                    ChargeType chargeType, SourceType sourceType,
                                    ParkingLotStatus parkingLotStatus, Integer reviewCount,
                                    Double averageRating, List<String> images) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.address = address;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.pricePerHour = pricePerHour;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
        this.chargeType = chargeType;
        this.sourceType = sourceType;
        this.parkingLotStatus = parkingLotStatus;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
        this.images = images;
    }

    public static ParkingLotSearchResponse from(ParkingLot parkingLot) {
        return ParkingLotSearchResponse.builder()
                .parkingLotId(parkingLot.getId())
                .name(parkingLot.getName())
                .address(parkingLot.getAddress())
                .openedAt(parkingLot.getOpenedAt())
                .closedAt(parkingLot.getClosedAt())
                .pricePerHour(parkingLot.getPricePerHour())
                .totalQuantity(parkingLot.getQuantity())
                //.availableQuantity(availableQuantity) // 추가 필요
                .chargeType(parkingLot.getChargeType())
                .sourceType(parkingLot.getSourceType())
                .parkingLotStatus(parkingLot.getStatus())
                //.reviewCount(reviewCount) // 추가 필요
                //.averageRating(averageRating) // 추가 필요
                //.images(images) // 추가 필요
                .build();
    }
}
