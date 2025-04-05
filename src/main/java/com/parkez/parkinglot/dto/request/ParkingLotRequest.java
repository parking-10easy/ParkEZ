
package com.parkez.parkinglot.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.user.domain.entity.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
public class ParkingLotRequest {

    @NotEmpty(message = "이름은 필수 값입니다.")
    private String name;

    @NotEmpty(message = "주소는 필수 값 입니다.")
    private String address;

    @NotNull(message = "시작 시간은 필수 값 입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime openedAt;

    @NotNull(message = "마감 시간은 필수 값 입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime closedAt;

    @NotNull(message = "시간 당 가격은 필수 값 입니다.")
    private BigDecimal pricePerHour;

    @NotEmpty(message = "설명은 필수 값 입니다.")
    private String description;

    @Min(value = 0, message = "주차 수량은 0 이상이어야 합니다.")
    @NotNull(message = "주차 수량은 필수 값 입니다.")
    private Integer quantity;

    public ParkingLotRequest(){}

    @Builder
    private ParkingLotRequest(String name, String address,
                              LocalTime openedAt, LocalTime closedAt,
                              BigDecimal pricePerHour, String description,
                              Integer quantity
    ){
        this.name = name;
        this.quantity = quantity;
        this.closedAt = closedAt;
        this.address = address;
        this.openedAt = openedAt;
        this.pricePerHour = pricePerHour;
        this.description = description;
    }

    public static ParkingLot toEntity(User owner, ParkingLotRequest dto){
        return ParkingLot.builder()
                .owner(owner)
                .name(dto.getName())
                .quantity(dto.getQuantity())
                .closedAt(dto.getClosedAt())
                .openedAt(dto.getOpenedAt())
                .pricePerHour(dto.getPricePerHour())
                .description(dto.description)
                .address(dto.getAddress())
                .build();
    }
}