package com.parkez.parkingzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "주차공간 생성 요청")
public class ParkingZoneCreateRequest {

    @NotNull(message = "주차장 아이디 값 입력은 필수입니다")
    @Schema(description = "주차장 아이디", example = "1")
    private Long parkingLotId;
    @NotBlank(message = "이름 값 입력은 필수입니다")
    @Schema(description = "주차공간명", example = "A구역")
    private String name;

    @Builder
    public ParkingZoneCreateRequest(Long parkingLotId, String name) {
        this.parkingLotId = parkingLotId;
        this.name = name;
    }
}
