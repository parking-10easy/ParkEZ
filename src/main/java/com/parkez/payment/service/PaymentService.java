package com.parkez.payment.service;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.pubsub.PaymentAlarmMessage;
import com.parkez.alarm.pubsub.PaymentAlarmPublisher;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.dto.request.PaymentConfirmRequest;
import com.parkez.payment.dto.request.PaymentCreateRequest;
import com.parkez.payment.dto.request.PaymentFailRequest;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.payment.dto.response.PaymentCreateResponse;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.dto.response.PaymentResponse;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.reservation.service.ReservationWriter;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentWriter paymentWriter;
    private final PaymentReader paymentReader;

    private final UserReader userReader;
    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final WebClient tossWebClient;
    private final TossPaymentService tossPaymentService;
    private final PaymentAlarmPublisher paymentAlarmPublisher;

    private static final long TIME_OUT_MINUTE = 10;
    private static final long EXPIRATION_TIME = 10L;


    public PaymentCreateResponse createPayment(AuthUser authUser, PaymentCreateRequest request, String orderId) {
        User user = userReader.getActiveUserById(authUser.getId());

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), request.getReservationId());

        validateReservationStatus(reservation);

        checkReservationTimeout(reservation, LocalDateTime.now());

        validatePaymentStatus(reservation);

        Payment payment = paymentWriter.createPayment(user, reservation, orderId);

        return PaymentCreateResponse.of(payment.getId(), reservation.getPrice(), payment.getOrderId());

    }

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){

        Payment payment = paymentReader.getPayment(request.getOrderId());

        PaymentConfirmResponse confirmResponse = tossPaymentService.confirmPayment(request);

        if(confirmResponse!=null) {
            paymentWriter.savePayment(payment, confirmResponse);
        }

        Reservation reservation = reservationReader.findMyReservation(payment.getUserId(), payment.getReservationId());
        reservationWriter.updateStatusConfirm(reservation);

        return confirmResponse;

    }

    public PaymentInfoResponse getPaymentInfo(String orderId) {
        Payment payment = paymentReader.getPayment(orderId);
        return PaymentInfoResponse.of(payment);
    }

    public PaymentResponse getMyPayment(AuthUser authUser, Long reservationId) {

        Reservation reservation = reservationReader.findMyReservation(authUser.getId(), reservationId);

        Payment payment = paymentReader.findByReservation(reservation).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return PaymentResponse.from(payment);

    }

    private void validateReservationStatus(Reservation reservation){
        switch (reservation.getStatus()){
            case CONFIRMED:
                throw new ParkingEasyException(PaymentErrorCode.PAYMENT_ALREADY_APPROVED);
            case COMPLETED:
                throw new ParkingEasyException(ReservationErrorCode.RESERVATION_ALREADY_USED);
            case CANCELED:
                throw new ParkingEasyException(ReservationErrorCode.RESERVATION_ALREADY_CANCELED);
            case PAYMENT_EXPIRED:
                throw new ParkingEasyException(PaymentErrorCode.PAYMENT_TIME_OUT);
        }
    }

    private void checkReservationTimeout(Reservation reservation, LocalDateTime now) {

        if(reservation.isTimeout(now, TIME_OUT_MINUTE)) {
            reservationWriter.cancel(reservation);
            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_TIME_OUT);
        }

    }

    private void validatePaymentStatus(Reservation reservation){
        paymentReader.findByReservation(reservation)
                .ifPresent(existingPayment -> {
                    switch (existingPayment.getPaymentStatus()) {
                        case PENDING:
                            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_IN_PROGRESS);
                        case APPROVED:
                            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_ALREADY_APPROVED);
                        case CANCELED:
                            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_CANCELED);
                    }
                });
    }


    public void failPayment(PaymentFailRequest request) {
        Payment payment = paymentReader.getPayment(request.getOrderId());

        paymentWriter.cancelPayment(payment);

        Reservation reservation = reservationReader.findMyReservation(payment.getUserId(), payment.getReservationId());

        reservationWriter.cancel(reservation);

        paymentAlarmPublisher.publish(new PaymentAlarmMessage(ReservationAlarmInfo.from(reservation), NotificationType.FAILED));
    }

    public void cancelPayment(Reservation reservation, ReservationCancelRequest request){

        Payment payment = paymentReader.findByReservation(reservation).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(payment.getPaymentStatus() == PaymentStatus.CANCELED){
            throw new ParkingEasyException(PaymentErrorCode.PAYMENT_CANCELED);
        }

        if(payment.getPaymentStatus() == PaymentStatus.PENDING){
            paymentWriter.cancelPayment(payment);
        }

        if(payment.getPaymentStatus() == PaymentStatus.APPROVED){

            String paymentKey = payment.getPaymentKey();

            tossPaymentService.cancelPayment(paymentKey, request);

            paymentWriter.cancelPayment(payment);
        }

        paymentAlarmPublisher.publish(new PaymentAlarmMessage(ReservationAlarmInfo.from(reservation), NotificationType.CANCELED));
    }

    public void expirePayment() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);

        List<Payment> expiredPayments = paymentReader.findPendingPayments(expiredTime);

        for (Payment payment : expiredPayments) {

            paymentWriter.cancelPayment(payment);

            Reservation reservation = payment.getReservation();
            reservationWriter.expirePaymentTimeout(reservation);

        }
    }
}
