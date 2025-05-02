package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.fcm.PushService;
import com.parkez.alarm.util.AlarmTestFactory;
import com.parkez.reservation.domain.entity.Reservation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmAlarmProcessorTest {

    @Mock
    private PushService pushService;

    @InjectMocks
    private FcmAlarmProcessor fcmAlarmProcessor;

    private ReservationAlarmInfo createTestReservationAlarmInfo() {
        Reservation reservation = AlarmTestFactory.createTestReservation();
        return ReservationAlarmInfo.from(reservation);
    }

    @Test
    void 예약_FCM_알림_처리_성공() {
        // given
        Alarm alarm = AlarmTestFactory.createPushTestAlarm();

        // when
        fcmAlarmProcessor.processReservation(alarm);

        // then
        verify(pushService).sendReservationPush(
                eq(alarm),
                eq(alarm.getDeviceToken()),
                eq(alarm.getTitle()),
                eq(alarm.getMessage())
        );
    }

    @Test
    void 결제_FCM_알림_처리_취소() {
        // given
        ReservationAlarmInfo info = createTestReservationAlarmInfo();

        // when
        fcmAlarmProcessor.processPayment(info, NotificationType.CANCELED);

        // then
        verify(pushService).sendPaymentPush(
                eq(info.getUserId()),
                contains("[결제]"),
                eq("결제가 취소되었습니다.")
        );
    }

    @Test
    void 결제_FCM_알림_처리_실패() {
        // given
        ReservationAlarmInfo info = createTestReservationAlarmInfo();

        // when
        fcmAlarmProcessor.processPayment(info, NotificationType.FAILED);

        // then
        verify(pushService).sendPaymentPush(
                eq(info.getUserId()),
                contains("[결제]"),
                eq("결제가 실패했습니다.")
        );
    }
}
