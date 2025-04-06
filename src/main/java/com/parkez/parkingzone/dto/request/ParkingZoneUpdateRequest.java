package com.parkez.parkingzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkingZoneUpdateRequest {

    @NotBlank(message = "이름 값 입력은 필수입니다")
    private String name;

    @Builder
    private ParkingZoneUpdateRequest(String name) {
        this.name = name;
    }
}
