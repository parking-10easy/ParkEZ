package com.parkez.alarm.service;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationAlarmService {

    private final AlarmService alarmService;
    private final ReservationReader reservationReader;

    @Transactional
    public void checkReservations() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinLater = now.plusMinutes(10);

        // 10분 전 알림
        List<Reservation> upcoming = reservationReader.findUpcomingReservations(now, tenMinLater);
        for (Reservation r : upcoming) {
            try {
                alarmService.createReservationAlarms(r, NotificationType.UPCOMING);
            } catch (Exception e) {
                log.error("UPCOMING 알람 전송 실패: reservationId={}", r.getId(), e);
            }
        }

        // 만료 알림
        List<Reservation> expired = reservationReader.findExpiredReservations(now);
        for (Reservation r : expired) {
            try {
                alarmService.createReservationAlarms(r, NotificationType.EXPIRED);
            } catch (Exception e) {
                log.error("EXPIRED 알람 전송 실패: reservationId={}", r.getId(), e);
            }
        }
    }
}
