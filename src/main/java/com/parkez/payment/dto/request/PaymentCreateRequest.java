package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema(description = "결제 요청 생성")
@NoArgsConstructor
public class PaymentCreateRequest {

    @NotNull(message = "결제를 위한 예약 ID는 필수입니다.")
    @Schema(description = "본인의 예약에만 결제 요청 생성이 가능합니다.", example = "1")
    private Long reservationId;

}
