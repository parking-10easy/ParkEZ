package com.parkez.alarm.scheduler;

import com.parkez.alarm.service.AlarmSender;
import com.parkez.alarm.service.ReservationAlarmService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AlarmScheduler {

    private final ReservationAlarmService reservationAlarmService;
    private final AlarmSender alarmSender;

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    @SchedulerLock(name = "alarmScheduler_scheduleAlarmsInOrder", lockAtLeastFor = "55s", lockAtMostFor = "2m")
    public void scheduleAlarmsInOrder() {
        reservationAlarmService.checkReservations();
        alarmSender.processReservationAlarms();
    }
}
