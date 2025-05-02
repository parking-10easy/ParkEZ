package com.parkez.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "결제 실패 요청")
public class PaymentFailRequest {

    @Schema(description = "주문번호", example = "orderId")
    @NotBlank
    private String orderId;

    @Schema(description = "에러코드", example = "400")
    private String errorCode;

    @Schema(description = "에러메세지", example = "이미 결제된 주문번호입니다.")
    private String errorMessage;


}
