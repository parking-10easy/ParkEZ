package com.parkez.parkinglot.dto.request;

import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingLotStatusRequest {

   @NotNull(message = "상태는 필수 값 입니다.")
   private ParkingLotStatus status;

}
