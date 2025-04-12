package com.parkez.parkingzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "주차공간 상태 변경 요청")
public class ParkingZoneUpdateStatusRequest {

    @NotNull(message = "상태 값 입력은 필수입니다")
    @Schema(description = "주차공간 상태", example = "UNAVAILABLE", allowableValues = {"AVAILABLE", "UNAVAILABLE"})
    private String status;

    @Builder
    private ParkingZoneUpdateStatusRequest(String status) {
        this.status = status;
    }
}
