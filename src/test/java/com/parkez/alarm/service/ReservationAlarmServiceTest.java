package com.parkez.alarm.service;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationAlarmServiceTest {

    @Mock
    private AlarmService alarmService;

    @Mock
    private ReservationReader reservationReader;

    @InjectMocks
    private ReservationAlarmService reservationAlarmService;

    @Test
    void 알람_전송_예약_만료예정과_만료에_대해_알람이_전송된다() {
        // given
        Reservation upcoming = mock(Reservation.class);
        Reservation expired = mock(Reservation.class);

        when(reservationReader.findUpcomingReservations(
                any(LocalDateTime.class),
                any(LocalDateTime.class))
        ).thenReturn(List.of(upcoming));

        when(reservationReader.findExpiredReservations(
                any(LocalDateTime.class))
        ).thenReturn(List.of(expired));

        // when
        reservationAlarmService.checkReservations();

        // then
        verify(alarmService).createReservationAlarms(upcoming, NotificationType.UPCOMING);
        verify(alarmService).createReservationAlarms(expired, NotificationType.EXPIRED);
    }

    @Test
    void 알람_전송할_예약이_없어도_예외없이_동작한다() {
        // given
        when(reservationReader.findUpcomingReservations(any(), any()))
                .thenReturn(List.of());

        when(reservationReader.findExpiredReservations(any()))
                .thenReturn(List.of());


        // when
        reservationAlarmService.checkReservations();

        // then
        verify(alarmService, never()).createReservationAlarms(any(), any());
    }

    @Test
    void 예약만료예정_알람_중_예외발생해도_만료알람은_정상_전송된다() {
        // given
        Reservation upcoming = mock(Reservation.class);
        Reservation expired = mock(Reservation.class);

        when(reservationReader.findUpcomingReservations(any(), any()))
                .thenReturn(List.of(upcoming));
        when(reservationReader.findExpiredReservations(any()))
                .thenReturn(List.of(expired));

        doThrow(new RuntimeException("예외 발생")).when(alarmService)
                .createReservationAlarms(upcoming, NotificationType.UPCOMING);

        // when
        reservationAlarmService.checkReservations();

        // then
        verify(alarmService).createReservationAlarms(upcoming, NotificationType.UPCOMING);
        verify(alarmService).createReservationAlarms(expired, NotificationType.EXPIRED);
    }

}