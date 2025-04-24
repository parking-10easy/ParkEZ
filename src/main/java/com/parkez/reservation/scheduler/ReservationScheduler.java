package com.parkez.reservation.scheduler;

import com.parkez.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ReservationScheduler {

    private final ReservationService reservationService;

    // 예약 생성 후 10분(결제 timeout)이 지나도 상태가 PENDING 인 예약들에 대하여 자동으로 PAYMENT_EXPIRED 으로 상태 변경
    @Scheduled(initialDelay = 5000, fixedDelay = 60000) // 5초 후부터 60초 간격
    @SchedulerLock(name = "reservationScheduler_expire", lockAtLeastFor = "55s", lockAtMostFor = "2m")
    public void expire() {
        reservationService.expireReservation();
    }
}
