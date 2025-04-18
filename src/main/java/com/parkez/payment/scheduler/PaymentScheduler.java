package com.parkez.payment.scheduler;

import com.parkez.payment.service.PaymentReader;
import com.parkez.payment.service.PaymentWriter;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.reservation.service.ReservationWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentReader paymentReader;
    private final PaymentWriter paymentWriter;
    private final ReservationWriter reservationWriter;

    private static final long EXPIRATION_TIME = 10L;

    // 결제 요청 생성 후 10분이 지나도 상태가 PENDING 인 결제들에 대하여 자동으로 예약/결제 CANCELED 로 상태 변경
    @Scheduled(initialDelay = 1000, fixedRate = 60000) // 1초 후부터 60초 간격
    @Transactional
    public void expirePendingPayments() {

        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);

        List<Payment> expiredPayments = paymentReader.findPendingPayments(expiredTime);

        for (Payment payment : expiredPayments) {

            paymentWriter.cancelPayment(payment);

            Reservation reservation = payment.getReservation();
            reservationWriter.expirePaymentTimeout(reservation);

        }
    }

}
