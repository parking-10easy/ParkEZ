package com.parkez.payment.web;


import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "결제 요청 생성, 결제 승인, 결제 내역 조회 기능입니다.")
public class PaymentController { //todo : 각 api에 사용자 권한 추가하기

    private final PaymentService paymentService;

    @PostMapping("/api/v1/payment")
    public Response<PaymentCreateResponse> create(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @RequestBody PaymentCreateRequest request
    ){
        return Response.of(paymentService.createPayment(authUser, request));
    }

    // todo - 6 : response로 응답에 URL을 포함해볼까? 오 좋은생각일지도

    @PostMapping("/confirm") //todo : 수정하기, 프론트 url도 수정
    public Response<PaymentConfirmResponse> confirm(@RequestBody PaymentConfirmRequest request){
        log.info("Confirm payment request: {}, {}, {}", request.getPaymentKey(), request.getOrderId(), request.getAmount());
        return Response.of(paymentService.confirmPayment(request));
    }

    @GetMapping("/api/v1/paymentInfo")
    public Response<PaymentInfoResponse> getPaymentInfo(@RequestParam String orderId){
        return Response.of(paymentService.getPaymentInfo(orderId));
    }

}

