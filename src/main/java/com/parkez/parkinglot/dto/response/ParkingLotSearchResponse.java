package com.parkez.parkinglot.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@Schema(description = "주차장 조회 응답 DTO")
public class ParkingLotSearchResponse {


    @Schema(description = "주차장 ID", example = "1")
    private final Long parkingLotId;

    @Schema(description = "주차장 이름", example = "한빛 주차장")
    private final String name;

    @Schema(description = "주차장 주소", example = "서울시 강남구 테헤란로 1111")
    private final String address;

    @Schema(description = "주차장 오픈 시간", example = "09:00")
    private final LocalTime openedAt;


    @Schema(description = "주차장 마감 시간", example = "23:00")
    private final LocalTime closedAt;

    @Schema(description = "시간 당 가격", example = "2000")
    private final BigDecimal pricePerHour;

    @Schema(description = "총 주차 대수", example = "100")
    private final Integer totalQuantity;

    @Schema(description = "사용 가능한 주차 대수", example = "80")
    private final Integer availableQuantity;

    @Schema(description = "요금 유형", example = "PAID")
    private final ChargeType chargeType;

    @Schema(description = "데이터 출처", example = "OWNER_REGISTERED")
    private final SourceType sourceType;

    @Schema(description = "주차장 상태", example = "OPEN")
    private final ParkingLotStatus parkingLotStatus;

    @Schema(description = "리뷰 수", example = "10")
    private final Integer reviewCount;

    @Schema(description = "평균 평점", example = "4.5")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0")
    private final Double averageRating;

    @Schema(description = "이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private final List<String> images; // 이미지 수정 필요

    @Builder
    private ParkingLotSearchResponse(Long parkingLotId, String name, String address,
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
