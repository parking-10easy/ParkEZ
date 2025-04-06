package com.parkez.parkingzone.dto.request;

import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ParkingZoneUpdateStatusRequest {

    @NotNull(message = "상태 값 입력은 필수입니다")
    private ParkingZoneStatus status;

    @Builder
    private ParkingZoneUpdateStatusRequest(ParkingZoneStatus status) {
        this.status = status;
    }
}
