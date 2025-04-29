package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.processor.EmailAlarmProcessor;
import com.parkez.alarm.service.processor.FcmAlarmProcessor;
import com.parkez.alarm.util.AlarmTestFactory;
import com.parkez.reservation.domain.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmSenderTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private EmailAlarmProcessor emailAlarmProcessor;

    @Mock
    private FcmAlarmProcessor fcmAlarmProcessor;

    @InjectMocks
    private AlarmSender alarmSender;

    @Test
    void 예약_알림_이메일과_FCM_전송_성공() {
        // given
        Alarm emailAlarm = spy(AlarmTestFactory.createEmailAlarm());
        Alarm fcmAlarm = spy(AlarmTestFactory.createFcmAlarm());

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(emailAlarm, fcmAlarm));

        // when
        alarmSender.processReservationAlarms();

        // then
        verify(emailAlarmProcessor).processReservation(emailAlarm);
        verify(fcmAlarmProcessor).processReservation(fcmAlarm);

        verify(emailAlarm).updateSent(true);
        verify(fcmAlarm).updateSent(true);
    }

    @Test
    void 예약_알림_전송_실패시_실패사유저장() {
        // given
        Alarm emailAlarm = spy(AlarmTestFactory.createEmailAlarm());

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(emailAlarm));
        doThrow(new RuntimeException("전송 실패")).when(emailAlarmProcessor).processReservation(emailAlarm);

        // when
        alarmSender.processReservationAlarms();

        // then
        verify(emailAlarm).updateFailReason("전송 실패");
    }

    @Test
    void 결제_알림_전송_성공() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();
        ReservationAlarmInfo info = ReservationAlarmInfo.from(reservation);

        NotificationType type = NotificationType.CANCELED;

        // when
        alarmSender.processPaymentAlarms(info, type);

        // then
        verify(emailAlarmProcessor).processPayment(info, type);
        verify(fcmAlarmProcessor).processPayment(info, type);
    }
}