package com.parkez.payment.web;

import com.parkez.common.dto.response.Response;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentFailRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.service.PaymentService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Tag(name = "09. 결제 - 승인/실패 API", description = "결제 승인/실패 기능입니다. 실제로는 클라이언트 측에서 실행됩니다.")
public class PaymentResultController {

    private final PaymentService paymentService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/confirm")
    @Operation(summary = "결제 승인 요청", description = "paymentKey로 Toss에게 결제 승인을 요청합니다. 실제 반환된 값을 입력해주세요.")
    public Response<PaymentConfirmResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request
    ){
        return Response.of(paymentService.confirmPayment(request));
    }

    @Secured(UserRole.Authority.USER)
    @PostMapping("/fail")
    @Operation(summary = "결제 실패", description = "결제가 실패되면 예약과 결제의 상태값이 바뀝니다. 실제 반환된 값을 입력해주세요.")
    public void fail(
            @Valid @RequestBody PaymentFailRequest request
    ){
        paymentService.failPayment(request);
    }

    @Hidden
    @Secured(UserRole.Authority.USER)
    @GetMapping("/info")
    public Response<PaymentInfoResponse> getPaymentInfo(@RequestParam String orderId){
        return Response.of(paymentService.getPaymentInfo(orderId));
    }
}
