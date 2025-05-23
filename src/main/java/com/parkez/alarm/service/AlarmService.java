package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import com.parkez.reservation.domain.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final FcmDeviceRepository fcmDeviceRepository;

    public void createReservationAlarms(Reservation reservation, NotificationType notificationType) {
        Long userId = reservation.getUserId();
        Long reservationId = reservation.getId();

        String title = String.format("[예약] %s %s",
                reservation.getParkingZoneName(),
                notificationType == NotificationType.UPCOMING ? "예약 종료 예정" : "예약 만료");

        String message = String.format("%s",
                notificationType == NotificationType.UPCOMING ? "10분 후 예약이 종료됩니다." : "예약이 종료되었습니다.");

        // 이메일 알림 생성
        boolean emailExists = alarmRepository.existsAlarm(reservationId, AlarmTargetType.RESERVATION, notificationType, AlarmChannel.EMAIL);
        if (!emailExists) {
            Alarm emailAlarm = Alarm.createEmailAlarm(userId, reservationId, AlarmTargetType.RESERVATION, reservation.getUserEmail(), title, message, notificationType);
            alarmRepository.save(emailAlarm);
        }

        // FCM 알림 생성
        fcmDeviceRepository.findFirstByUserIdAndStatusTrue(userId).ifPresent(device -> {
            boolean fcmExists = alarmRepository.existsAlarm(reservationId, AlarmTargetType.RESERVATION, notificationType, AlarmChannel.FCM);
            if (!fcmExists) {
                Alarm fcmAlarm = Alarm.createFcmAlarm(userId, reservationId, AlarmTargetType.RESERVATION, device.getToken(), title, message, notificationType);
                alarmRepository.save(fcmAlarm);
            }
        });
    }
}
