package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.dto.response.PaymentResponse;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.reservation.service.ReservationWriter;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentWriter paymentWriter;
    private final PaymentReader paymentReader;

    private final UserReader userReader;
    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final WebClient tossWebClient;

    private static final int TIME_OUT_MINUTE = 30;

    public PaymentCreateResponse createPayment(AuthUser authUser, PaymentCreateRequest request) {
        User user = userReader.getActiveById(authUser.getId());

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), request.getReservationId());

        checkReservationTimeout(reservation);

        if(paymentReader.existsPayment(reservation)){
            throw new ParkingEasyException(PaymentErrorCode.ALREADY_PAID_RESERVATION);
        }
        /* todo 여기서 에러코드를 구분하자
              결제가 생성되어 있지만 payment Pending 상태라면 -> 결제 진행중입니다.
              결제 취소된 내역에 대해서 재결제 불가능 , 예약 새로 생성해주세요
              결제 승인된 내역에 대해서는 이미 결제된 예약입니다.
        */

        String orderId = UUID.randomUUID().toString().replace("-", "");

        Payment payment = paymentWriter.createPayment(user, reservation, orderId);

        return PaymentCreateResponse.of(payment.getId(), reservation.getPrice(), payment.getOrderId());

    }

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){

        Payment payment = paymentReader.getPayment(request.getOrderId());

        PaymentConfirmResponse confirmResponse = tossWebClient.post()
                .uri("/confirm")
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("클라이언트 오류: " + body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (4xx): " + body));
                        })
                )
                .onStatus(status -> status.is5xxServerError(), response ->
                        response.bodyToMono(String.class).flatMap(body -> {
                            log.info("서버 오류: " + body);
                            return Mono.error(new RuntimeException("결제 승인 실패 (5xx): " + body));
                        })
                )
                .bodyToMono(PaymentConfirmResponse.class)
                .block();

        if(confirmResponse!=null) {
            paymentWriter.savePayment(payment, confirmResponse);
        }

        Reservation reservation = reservationReader.findMyReservation(payment.getUserId(), payment.getReservation().getId());
        reservationWriter.updateStatusConfirm(reservation);

        return confirmResponse;

    }

    public PaymentInfoResponse getPaymentInfo(String orderId) {
        return paymentReader.getPaymentInfo(orderId);
    }

    public PaymentResponse getMyPayment(AuthUser authUser, Long reservationId) {

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        Payment payment = paymentReader.findByReservation(reservation).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);

    }


    private void checkReservationTimeout(Reservation reservation) {
        LocalDateTime createdAt = reservation.getCreatedAt();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        if (duration.toMinutes() > TIME_OUT_MINUTE) {
            reservationWriter.updateStatusCancel(reservation);

            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_TIME_OUT);

        }
    }
}
