package com.parkez.parkingzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkingZoneUpdateImageRequest {

    @NotBlank(message = "이미지 값 입력은 필수입니다")
    private String imageUrl;

    @Builder
    private ParkingZoneUpdateImageRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
