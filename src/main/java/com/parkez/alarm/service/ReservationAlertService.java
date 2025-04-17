package com.parkez.alarm.service;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationAlertService {

    private final ReservationRepository reservationRepository;
    private final AlarmService alarmService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinLater = now.plusMinutes(10);

        // 10분 전 알림
        List<Reservation> upcoming = reservationRepository.findConfirmedReservationsBetween(ReservationStatus.CONFIRMED, now, tenMinLater);
        for (Reservation r : upcoming) {
            alarmService.createReservationAlarms(r, NotificationType.UPCOMING);
        }

        // 만료 알림
        List<Reservation> expired = reservationRepository.findExpiredReservations(ReservationStatus.CONFIRMED, now);
        for (Reservation r : expired) {
            alarmService.createReservationAlarms(r, NotificationType.EXPIRED);
        }
    }
}
