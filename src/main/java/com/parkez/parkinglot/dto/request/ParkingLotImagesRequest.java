package com.parkez.parkinglot.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ParkingLotImagesRequest {

    @NotEmpty(message = "이미지 URL 목록은 비어있을 수 없습니다.")
    private List<String> imageUrls;

    @Builder
    public ParkingLotImagesRequest(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}