package com.parkez.payment.scheduler;

import com.parkez.payment.service.PaymentService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.payment.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentService paymentService;

    // 결제 요청 생성 후 10분이 지나도 상태가 PENDING 인 결제들에 대하여 자동으로 예약/결제 CANCELED 로 상태 변경
    @Scheduled(initialDelay = 1000, fixedDelay = 60000) // 1초 후부터 60초 간격
    @Transactional
    public void expirePendingPayments() {

        paymentService.expirePayment();

    }

}
