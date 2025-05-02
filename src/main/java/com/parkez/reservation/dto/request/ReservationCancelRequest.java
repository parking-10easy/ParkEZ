package com.parkez.reservation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "예약 취소 요청 DTO")
public class ReservationCancelRequest {

    @NotBlank
    @Schema(description = "예약 취소 사유", example = "단순 변심으로 인한 취소")
    private String cancelReason;

}
