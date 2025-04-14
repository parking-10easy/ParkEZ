package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
@Schema(description = "결제 요청 생성")
public class PaymentCreateRequest {

    @NotNull(message = "결제를 위한 예약 ID는 필수입니다.")
    private Long reservationId;

}
