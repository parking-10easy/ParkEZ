package com.parkez.payment.web;


import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.dto.response.PaymentResponse;
import com.parkez.payment.service.PaymentService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "결제 API", description = "결제 요청 생성, 결제 승인, 결제 내역 조회 기능입니다.")
public class PaymentController {

    private final PaymentService paymentService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/payment")
    public Response<PaymentCreateResponse> create(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody PaymentCreateRequest request
    ){
        return Response.of(paymentService.createPayment(authUser, request));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/payment-info")
    public Response<PaymentInfoResponse> getPaymentInfo(@RequestParam String orderId){
        return Response.of(paymentService.getPaymentInfo(orderId));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/{reservationId}/payment")
    public Response<PaymentResponse> getMyPayment(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ){
        return Response.of(paymentService.getMyPayment(authUser, reservationId));
    }

}

