package com.parkez.payment.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.dto.response.PaymentConfirmResponse;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentWriter {
    private final PaymentRepository paymentRepository;

    // 결제 요청 생성 저장
    public Payment createPayment(User user, Reservation reservation, String orderId) {

        Payment payment = Payment.builder()
                .user(user)
                .reservation(reservation)
                .paymentStatus(PaymentStatus.PENDING)
                .orderId(orderId)
                .build();

        return paymentRepository.save(payment);
    }

    public void savePayment(Payment payment, PaymentConfirmResponse response) {
        payment.approvePaymentInfo(response.getPaymentKey(), response.getApprovedAt(), response.getType());
        paymentRepository.save(payment);
    }

    public void cancelPayment(Payment payment){
        payment.cancel(LocalDateTime.now());
        paymentRepository.save(payment);
    }

}
