package com.parkez.parkingzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "주차공간 이름 수정 요청")
public class ParkingZoneUpdateNameRequest {

    @NotBlank(message = "이름 값 입력은 필수입니다")
    @Schema(description = "주차공간명", example = "B구역")
    private String name;

    @Builder
    private ParkingZoneUpdateNameRequest(String name) {
        this.name = name;
    }
}
