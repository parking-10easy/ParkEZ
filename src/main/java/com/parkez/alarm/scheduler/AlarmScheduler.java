package com.parkez.alarm.scheduler;

import com.parkez.alarm.service.AlarmSender;
import com.parkez.alarm.service.ReservationAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final ReservationAlarmService reservationAlarmService;
    private final AlarmSender alarmSender;

    @Scheduled(initialDelay = 1000, fixedRate = 60000)
    public void scheduleAlarmsInOrder() {
        reservationAlarmService.checkReservations();
        alarmSender.processAlarms();
    }
}
