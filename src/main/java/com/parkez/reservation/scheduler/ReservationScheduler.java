package com.parkez.reservation.scheduler;

import com.parkez.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationService reservationService;

    // 예약 생성 후 10분(결제 timeout)이 지나도 상태가 PENDING 인 예약들에 대하여 자동으로 PAYMENT_EXPIRED 으로 상태 변경
    @Scheduled(fixedDelay = 60_000) // 60초 간격으로 실행
    public void expire() {
        reservationService.expireReservation();
    }
}
