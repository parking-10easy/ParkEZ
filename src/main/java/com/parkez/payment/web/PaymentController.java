package com.parkez.payment.web;


import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentResponse;
import com.parkez.payment.service.PaymentService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "결제 - 요청/조회 API", description = "결제 요청 생성, 결제 내역 조회 기능입니다.")
public class PaymentController {

    private final PaymentService paymentService;

    @Secured(UserRole.Authority.USER)
    @PostMapping("/payment")
    @Operation(summary = "결제 생성 요청", description = "예약 건에 대해 결제 요청을 생성합니다. 반환된 orderId로 실제 결제를 진행합니다.")
    public Response<PaymentCreateResponse> create(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody PaymentCreateRequest request
    ){
        String orderId = UUID.randomUUID().toString().replace("-", "");
        return Response.of(paymentService.createPayment(authUser, request, orderId));
    }

    @Secured(UserRole.Authority.USER)
    @GetMapping("/{reservationId}/payment")
    @Operation(summary = "나의 결제 조회", description = "예약 건에 대한 결제 정보를 조회합니다. (본인만 조회 가능)")
    public Response<PaymentResponse> getMyPayment(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long reservationId
    ){
        return Response.of(paymentService.getMyPayment(authUser, reservationId));
    }

}

