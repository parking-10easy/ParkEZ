package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.dto.response.PaymentInfoResponse;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentReader {
    // todo : 메서드명 확인하기 (find of get)

    private final PaymentRepository paymentRepository;

    public Payment getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

    }

    public PaymentInfoResponse getPaymentInfo(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return PaymentInfoResponse.builder()
                .customerEmail(payment.getUser().getEmail())
                .customerName(payment.getUser().getNickname())
                .customerMobilePhone(payment.getUser().getPhone())
                .totalPrice(payment.getTotalPrice())
                .build();
    }

    public Optional<Payment> findByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

}
