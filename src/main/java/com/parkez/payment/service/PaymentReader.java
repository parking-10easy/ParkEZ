package com.parkez.payment.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.repository.PaymentRepository;
import com.parkez.payment.exception.PaymentErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentReader {

    private final PaymentRepository paymentRepository;

    public Payment getPayment(String orderId) {
        return paymentRepository.findByOrderId(orderId).orElseThrow(
                () -> new ParkingEasyException(PaymentErrorCode.PAYMENT_NOT_FOUND));

    }

    public Optional<Payment> findByReservation(Reservation reservation) {
        return paymentRepository.findByReservation(reservation);
    }

    public List<Payment> findPendingPayments(LocalDateTime expiredTime) {
        return paymentRepository.findPendingPaymentsToExpire(expiredTime);
    }
}
