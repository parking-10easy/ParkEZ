package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.reservation.domain.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;

    public void createReservationAlarms(Reservation reservation, NotificationType notificationType) {
        Long userId = reservation.getUserId();
        Long reservationId = reservation.getId();

        String title = "[예약] " + reservation.getParkingZoneName() +
                (notificationType == NotificationType.UPCOMING ? " 종료 예정" : " 만료");
        String message = notificationType == NotificationType.UPCOMING ? "10분 후 예약이 종료됩니다." : "예약이 종료되었습니다.";

        // 이메일 알림 생성
        boolean emailExists = alarmRepository.existsAlarm(reservationId, AlarmTargetType.RESERVATION, notificationType, AlarmChannel.EMAIL);
        if (emailExists) {
            log.warn("이미 이메일 알림 전송됨: reservationId={}, type={}", reservationId, notificationType);
        } else {
            Alarm emailAlarm = Alarm.createEmailAlarm(userId, reservationId, reservation.getUserEmail(), title, message, notificationType);
            alarmRepository.save(emailAlarm);
        }
    }
}
