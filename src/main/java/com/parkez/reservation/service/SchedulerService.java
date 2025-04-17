package com.parkez.reservation.service;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class SchedulerService {

    private final ReservationRepository reservationRepository;

    private static final long EXPIRATION_TIME = 10L;

    // 예약 생성 후 10분(결제 timeout)이 지나도 상태가 PENDING 인 예약들에 대하여 자동으로 PAYMENT_EXPIRED 으로 상태 변경
    @Scheduled(fixedDelay = 60_000) // 60초 간격으로 실행
    public void expire() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(EXPIRATION_TIME);
        List<Reservation> expireToReservation = reservationRepository.findReservationsToExpire(expiredTime);

        if (!expireToReservation.isEmpty()) {
            expireToReservation.forEach(Reservation::expire);
            reservationRepository.saveAll(expireToReservation);
        }
    }
}
