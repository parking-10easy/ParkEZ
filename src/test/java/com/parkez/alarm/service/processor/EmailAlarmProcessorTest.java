package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.email.SesEmailService;
import com.parkez.alarm.service.email.SmtpEmailService;
import com.parkez.alarm.service.template.EmailTemplateService;
import com.parkez.alarm.util.AlarmTestFactory;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailAlarmProcessorTest {

    @Mock
    private EmailTemplateService emailTemplateService;

    @Mock
    private SesEmailService sesEmailService;

    @Mock
    private SmtpEmailService smtpEmailService;

    @Mock
    private ReservationReader reservationReader;

    @InjectMocks
    private EmailAlarmProcessor emailAlarmProcessor;

    @Test
    void 예약_이메일_알림_처리_성공() {
        // given
        Alarm alarm = AlarmTestFactory.createEmailTestAlarm();
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(reservationReader.findReservation(alarm.getTargetId())).thenReturn(reservation);
        when(emailTemplateService.generateEmailContent(anyString(), anyMap())).thenReturn("<html>메일 내용</html>");

        // when
        emailAlarmProcessor.processReservation(alarm);

        // then
        verify(reservationReader).findReservation(alarm.getTargetId());
        verify(emailTemplateService).generateEmailContent(eq("reservation_notification.html"), anyMap());
        verify(sesEmailService).sendEmail(eq(alarm.getEmailAddress()), eq(alarm.getTitle()), anyString());
    }

    @Test
    void 예약_알림_처리시_잘못된_TargetType_예외발생() {
        // given
        Alarm alarm = AlarmTestFactory.createEmailTestAlarm();
        // 일부러 잘못된 TargetType 설정
        ReflectionTestUtils.setField(alarm, "targetType", AlarmTargetType.PAYMENT);

        // when & then
        assertThatThrownBy(() -> emailAlarmProcessor.processReservation(alarm))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 기능은 예약 알림만 지원합니다.");
    }

    @Test
    void 예약_알림_만료_상태_처리() {
        // given
        Alarm alarm = AlarmTestFactory.createEmailTestAlarm();
        ReflectionTestUtils.setField(alarm, "notificationType", NotificationType.EXPIRED);
        Reservation reservation = AlarmTestFactory.createTestReservation();

        when(reservationReader.findReservation(alarm.getTargetId())).thenReturn(reservation);
        when(emailTemplateService.generateEmailContent(anyString(), anyMap())).thenReturn("<html>메일 내용</html>");

        // when
        emailAlarmProcessor.processReservation(alarm);

        // then
        verify(emailTemplateService).generateEmailContent(eq("reservation_notification.html"), anyMap());
        verify(sesEmailService).sendEmail(eq(alarm.getEmailAddress()), eq(alarm.getTitle()), anyString());
    }

    @Test
    void 결제_이메일_알림_처리_성공() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();
        ReservationAlarmInfo info = ReservationAlarmInfo.from(reservation);

        when(emailTemplateService.generateEmailContent(anyString(), anyMap())).thenReturn("<html>결제 메일</html>");

        // when
        emailAlarmProcessor.processPayment(info, NotificationType.CANCELED);

        // then
        verify(emailTemplateService).generateEmailContent(eq("payment_notification.html"), anyMap());
        verify(sesEmailService).sendEmail(eq(info.getUserEmail()), contains("[결제]"), anyString());
    }

    @Test
    void 결제_이메일_알림_실패_처리() {
        // given
        Reservation reservation = AlarmTestFactory.createTestReservation();
        ReservationAlarmInfo info = ReservationAlarmInfo.from(reservation);

        when(emailTemplateService.generateEmailContent(anyString(), anyMap())).thenReturn("<html>결제 실패 메일</html>");

        // when
        emailAlarmProcessor.processPayment(info, NotificationType.FAILED);

        // then
        verify(emailTemplateService).generateEmailContent(eq("payment_notification.html"), anyMap());
        verify(sesEmailService).sendEmail(eq(info.getUserEmail()), contains("[결제]"), anyString());
    }

}
