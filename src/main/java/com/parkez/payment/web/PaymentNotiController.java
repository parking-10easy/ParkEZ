package com.parkez.payment.web;

import com.parkez.common.dto.response.Response;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.service.PaymentService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/")
@RequiredArgsConstructor
public class PaymentNotiController {

    private final PaymentService paymentService;

    @Hidden
    @Secured(UserRole.Authority.USER)
    @PostMapping("payment-confirm")
    public Response<PaymentConfirmResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request
    ){
        return Response.of(paymentService.confirmPayment(request));
    }
}
