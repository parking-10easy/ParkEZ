package com.parkez.parkingzone.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ParkingZoneCreateRequest {

    @NotNull(message = "주차장 아이디 값 입력은 필수입니다")
    private Long parkingLotId;
    @NotBlank(message = "이름 값 입력은 필수입니다")
    private String name;
    private String imageUrl;

    @Builder
    private ParkingZoneCreateRequest(Long parkingLotId, String name, String imageUrl) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
