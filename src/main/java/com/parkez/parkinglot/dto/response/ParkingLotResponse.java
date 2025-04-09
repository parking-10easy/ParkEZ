package com.parkez.parkinglot.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Schema(description = "주차장 생성 응답 DTO")
public class ParkingLotResponse {

    @Schema(description = "주차장 ID", example = "1")
    private Long id;

    @Schema(description = "주차장 이름", example = "한빛 주차장")
    private String name;

    @Schema(description = "주차장 주소", example = "서울시 강남구 테헤란로 1111")
    private String address;

    @Schema(description = "주차장 오픈 시간", example = "09:00")
    private LocalTime openedAt;

    @Schema(description = "주차장 마감 시간", example = "23:00")
    private LocalTime closedAt;

    @Schema(description = "시간 당 가격", example = "2000")
    private BigDecimal pricePerHour;

    @Schema(description = "주차장 설명", example = "한빛 주차장은 최신 시설을 갖추고 있습니다.")
    private String description;

    @Schema(description = "주차장 수량", example = "100")
    private Integer quantity;

    @Schema(description = "주차장 상태", example = "OPEN")
    private ParkingLotStatus status;

    @Schema(description = "요금 유형", example = "PAID")
    private ChargeType chargeType;

    @Schema(description = "데이터 등록 방식", example = "OWNER_REGISTERED")
    private SourceType sourceType;

    @Schema(description = "생성 일시", example = "2025-04-04 08:00:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "수정 일시", example = "2025-04-04 08:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime modifiedAt;

    @Builder
    private ParkingLotResponse(Long id, String name, String address,
                               LocalDateTime createdAt, LocalDateTime modifiedAt,
                               SourceType sourceType, ChargeType chargeType,
                               ParkingLotStatus status, Integer quantity,
                               String description, BigDecimal pricePerHour,
                               LocalTime openedAt, LocalTime closedAt
    ) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.sourceType = sourceType;
        this.modifiedAt = modifiedAt;
        this.chargeType = chargeType;
        this.status = status;
        this.quantity = quantity;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.address = address;
    }

    public static ParkingLotResponse from(ParkingLot parkingLot) {
        return ParkingLotResponse.builder()
                .id(parkingLot.getId())
                .name(parkingLot.getName())
                .createdAt(parkingLot.getCreatedAt())
                .sourceType(parkingLot.getSourceType())
                .modifiedAt(parkingLot.getModifiedAt())
                .chargeType(parkingLot.getChargeType())
                .status(parkingLot.getStatus())
                .quantity(parkingLot.getQuantity())
                .description(parkingLot.getDescription())
                .pricePerHour(parkingLot.getPricePerHour())
                .openedAt(parkingLot.getOpenedAt())
                .closedAt(parkingLot.getClosedAt())
                .address(parkingLot.getAddress())
                .build();
    }

}
