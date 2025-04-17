package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "결제 실패 요청")
public class PaymentFailRequest {

    @NotBlank
    private String orderId;

    private String errorCode;

    private String errorMessage;


}
