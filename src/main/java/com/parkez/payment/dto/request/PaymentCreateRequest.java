package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Schema(description = "결제 요청 생성")
public class PaymentCreateRequest {

    private Long reservationId;

}
