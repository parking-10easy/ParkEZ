package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import com.parkez.alarm.util.AlarmTestFactory;
import com.parkez.reservation.domain.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private FcmDeviceRepository fcmDeviceRepository;

    @InjectMocks
    private AlarmService alarmService;

    @Test
    void 예약_알림_이메일_FCM_생성_성공() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.FCM))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserIdAndStatusTrue(anyLong())).thenReturn(Optional.of(new FcmDevice(reservation.getUserId(), "test-token")));

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        verify(alarmRepository, times(2)).save(any(Alarm.class));
    }

    @Test
    void 이미_존재하는_알림이면_생성하지_않는다() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(true);
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.FCM))).thenReturn(true);
        when(fcmDeviceRepository.findFirstByUserIdAndStatusTrue(anyLong())).thenReturn(Optional.of(new FcmDevice(reservation.getUserId(), "test-token")));

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        verify(alarmRepository, never()).save(any(Alarm.class));
    }

    @Test
    void FCM_디바이스가_없으면_FCM_알림_생성하지_않는다() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserIdAndStatusTrue(anyLong()))
                .thenReturn(Optional.empty());

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        // 이메일 알림만 저장 호출
        verify(alarmRepository, times(1)).save(any(Alarm.class));
    }

    @Test
    void 예약_알림_만료_상태_생성() {
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserIdAndStatusTrue(anyLong())).thenReturn(Optional.empty());

        alarmService.createReservationAlarms(reservation, NotificationType.EXPIRED);

        verify(alarmRepository, times(1)).save(any(Alarm.class));
    }
}