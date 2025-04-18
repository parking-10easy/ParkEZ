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

    @Scheduled(cron = "0 * * * * *")
    public void setReservationAlarmService() {
        reservationAlarmService.checkReservations();
    }

    @Scheduled(cron = "30 * * * * *")
    public void setAlarmSender() {
        alarmSender.processAlarms();
    }
}
