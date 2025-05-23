package com.parkez.parkingzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "주차공간 이미지 수정 요청")
@NoArgsConstructor
public class ParkingZoneUpdateImageRequest {

    @NotBlank(message = "이미지 값 입력은 필수입니다")
    @Schema(description = "이미지 주소", example = "http://example.com/image.jpg")
    private String imageUrl;

    @Builder
    private ParkingZoneUpdateImageRequest(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
