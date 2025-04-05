package com.parkez.parkinglot.dto.response;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ParkingLotResponse {

    private Long id;
    private String name;
    private String address;
    private LocalTime openedAt;
    private LocalTime closedAt;
    private BigDecimal pricePerHour;
    private String description;
    private Integer quantity;
    private ParkingLotStatus status;
    private ChargeType chargeType;
    private SourceType sourceType;
    private LocalDateTime createdAt;
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

    public static ParkingLotResponse from (ParkingLot parkingLot){
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
