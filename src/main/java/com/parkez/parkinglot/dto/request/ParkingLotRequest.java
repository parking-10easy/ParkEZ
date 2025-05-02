
package com.parkez.parkinglot.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Schema(description = "주차장 생성 및 수정 요청 DTO")
@NoArgsConstructor
public class ParkingLotRequest {

    @NotBlank(message = "이름은 필수 값입니다.")
    @Schema(description = "주차장 이름", example = "한빛 주차장")
    private String name;

    @NotBlank(message = "주소는 필수 값 입니다.")
    @Schema(description = "주차장 주소", example = "서울시 강남구 테헤란로 131")
    private String address;

    @NotNull(message = "시작 시간은 필수 값 입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "주차장 오픈 시간", example = "09:00")
    private LocalTime openedAt;

    @NotNull(message = "마감 시간은 필수 값 입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Schema(description = "주차장 마감 시간", example = "23:00")
    private LocalTime closedAt;

    @Min(value = 0, message = "시간 당 가격 0 이상이어야 합니다.")
    @NotNull(message = "시간 당 가격은 필수 값 입니다.")
    @Schema(description = "시간 당 가격", example = "2000")
    private BigDecimal pricePerHour;

    @NotBlank(message = "설명은 필수 값 입니다.")
    @Schema(description = "주차장 설명", example = "한빛 주차장은 최신 시설을 갖추고 있습니다.")
    private String description;

    @Min(value = 0, message = "주차 수량은 0 이상이어야 합니다.")
    @NotNull(message = "주차 수량은 필수 값 입니다.")
    @Schema(description = "주차장 수량", example = "100")
    private Integer quantity;

    @Builder
    private ParkingLotRequest(String name, String address,
                              LocalTime openedAt, LocalTime closedAt,
                              BigDecimal pricePerHour, String description,
                              Integer quantity
    ) {
        this.name = name;
        this.quantity = quantity;
        this.closedAt = closedAt;
        this.address = address;
        this.openedAt = openedAt;
        this.pricePerHour = pricePerHour;
        this.description = description;
    }
}