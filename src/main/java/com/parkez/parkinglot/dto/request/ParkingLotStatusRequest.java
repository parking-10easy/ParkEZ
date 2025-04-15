package com.parkez.parkinglot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "주차장 상태 변경 요청 DTO")
public class ParkingLotStatusRequest {

    @NotBlank(message = "상태는 필수 값 입니다.")
    @Schema(description = "주차장 상태", example = "OPEN")
    private String status;

    @Builder
    private ParkingLotStatusRequest (String status){
        this.status = status;
    }

}
