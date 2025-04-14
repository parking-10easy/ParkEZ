package com.parkez.payment.service;

import com.parkez.common.config.WebClientConfig;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {


    private final PaymentWriter paymentWriter;
    private final PaymentReader paymentReader;

    private final UserReader userReader;
    private final ReservationReader reservationReader;
    private final WebClient tossWebClient;
    private final WebClientConfig webClientConfig;

    public PaymentCreateResponse createPayment(AuthUser authUser, PaymentCreateRequest request) {
        User user = userReader.getActiveById(authUser.getId());

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), request.getReservationId());

        paymentReader.findByReservation(reservation).ifPresent(payment -> {
            throw new ParkingEasyException(PaymentErrorCode.ALREADY_PAID_RESERVATION);
        });

        String orderId = UUID.randomUUID().toString().replace("-", "");

        Payment payment = paymentWriter.createPayment(user, reservation, orderId, request);

        return PaymentCreateResponse.of(payment.getId(), reservation.getPrice(), payment.getOrderId());

    }


    //todo : timeout 추가할 예정
    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){

        Payment payment = paymentReader.getPayment(request.getOrderId());

        PaymentConfirmResponse confirmResponse = tossWebClient.post()
                .uri("/confirm")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            System.out.println("클라이언트 오류: " + body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (4xx): " + body));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            System.out.println("서버 오류: " + body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (5xx): " + body));
                        })
                )
                .bodyToMono(PaymentConfirmResponse.class)
                .block();

        if(confirmResponse!=null) {
            paymentWriter.savePayment(payment, confirmResponse);
        }

        // todo : 결제 승인 후 예약 상태변경 추가하기


        return confirmResponse;

    }

    public PaymentInfoResponse getPaymentInfo(String orderId) {
        return paymentReader.getPaymentInfo(orderId);
    }
}
