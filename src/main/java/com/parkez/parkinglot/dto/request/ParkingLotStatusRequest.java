package com.parkez.parkinglot.dto.request;

import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "주차장 상태 변경 요청 DTO")
public class ParkingLotStatusRequest {

   @NotNull(message = "상태는 필수 값 입니다.")
   @Schema(description = "주차장 상태", example = "AVAILABLE")
   private ParkingLotStatus status;

}
