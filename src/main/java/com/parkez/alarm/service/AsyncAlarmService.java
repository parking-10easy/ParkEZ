package com.parkez.alarm.service;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncAlarmService {

    private final AlarmService alarmService;
    private final AlarmSender alarmSender;

    @Async
    public void sendAlarms(ReservationAlarmInfo reservationAlarmInfo, NotificationType notificationType) {
        alarmService.createPaymentAlarms(reservationAlarmInfo, notificationType);
        alarmSender.processAlarms();
    }
}
