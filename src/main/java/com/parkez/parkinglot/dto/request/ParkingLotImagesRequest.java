package com.parkez.parkinglot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "주차장 이미지 수정 요청 DTO")
public class ParkingLotImagesRequest {

    @NotEmpty(message = "이미지 URL 목록은 비어있을 수 없습니다.")
    @Schema(
            description = "수정할 이미지의 URL 목록",
            example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]"
    )
    private List<String> imageUrls;

    @Builder
    private ParkingLotImagesRequest(List<String> imageUrls){
        this.imageUrls = imageUrls;
    }

}