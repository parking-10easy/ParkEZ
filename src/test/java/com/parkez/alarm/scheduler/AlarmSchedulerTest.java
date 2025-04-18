package com.parkez.alarm.scheduler;

import com.parkez.alarm.service.AlarmSender;
import com.parkez.alarm.service.ReservationAlarmService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AlarmSchedulerTest {

    @Mock
    private ReservationAlarmService reservationAlarmService;

    @Mock
    private AlarmSender alarmSender;

    @InjectMocks
    private AlarmScheduler alarmScheduler;

    @Test
    void 예약알람_스케줄러_작동시_reservationService_호출된다() {
        // when
        alarmScheduler.setReservationAlarmService();

        // then
        verify(reservationAlarmService).checkReservations();
    }

    @Test
    void 알람전송_스케줄러_작동시_alarmSender_호출된다() {
        // when
        alarmScheduler.setAlarmSender();

        // then
        verify(alarmSender).processAlarms();
    }
}