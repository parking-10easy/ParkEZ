package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.processor.EmailAlarmProcessor;
import com.parkez.alarm.service.processor.FcmAlarmProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSender {

    private final AlarmRepository alarmRepository;
    private final EmailAlarmProcessor emailAlarmProcessor;
    private final FcmAlarmProcessor fcmAlarmProcessor;

    @Transactional
    public void processReservationAlarms() {
        List<Alarm> pendingAlarms = alarmRepository.findAllBySentFalse();

        for (Alarm alarm : pendingAlarms) {
            try {
                switch (alarm.getChannel()) {
                    case EMAIL -> emailAlarmProcessor.processReservation(alarm);
                    case FCM -> fcmAlarmProcessor.processReservation(alarm);
                    default -> throw new IllegalArgumentException("지원하지 않는 알림 채널입니다: " + alarm.getChannel());
                }
                alarm.updateSent(true);
                alarm.updateSentAt(LocalDateTime.now());
            } catch (Exception e) {
                alarm.updateFailReason(e.getMessage());
                log.error("알림 전송 실패: alarmId={}, reason={}", alarm.getId(), e.getMessage(), e);
            }
        }
    }

    public void processPaymentAlarms(ReservationAlarmInfo reservationAlarmInfo, NotificationType notificationType) {
        emailAlarmProcessor.processPayment(reservationAlarmInfo, notificationType);
        fcmAlarmProcessor.processPayment(reservationAlarmInfo, notificationType);
    }
}