package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmSenderTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private SmtpEmailService smtpEmailService;

    @Mock
    private SesEmailService sesEmailService;

    @Mock
    private PushService pushService;

    @InjectMocks
    private AlarmSender alarmSender;

    @Test
    void 이메일알람_전송_성공시_sent가_true로_업데이트된다() throws MessagingException {
        // given
        Alarm emailAlarm = Alarm.createEmailAlarm(1L, 1L, AlarmTargetType.RESERVATION, "test@email.com", "제목", "본문", NotificationType.UPCOMING);

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(emailAlarm));

        // when
        alarmSender.processAlarms();

        // then
//        verify(smtpEmailService).sendEmail("test@email.com", "noreply@test.com", "제목", "본문");
        verify(sesEmailService).sendEmail("test@email.com", "제목", "본문");
        assertThat(emailAlarm.isSent()).isTrue();
        assertThat(emailAlarm.getSentAt()).isNotNull();
    }

    @Test
    void 이메일알람_전송_실패시_예외처리_및_failReason_설정된다() throws MessagingException {
        // given
        Alarm failedAlarm = Alarm.createEmailAlarm(1L, 1L, AlarmTargetType.RESERVATION, "fail@email.com", "제목", "본문", NotificationType.UPCOMING);
        ReflectionTestUtils.setField(failedAlarm, "channel", AlarmChannel.EMAIL);

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(failedAlarm));

        doThrow(new RuntimeException("메일 전송 실패")).when(sesEmailService).sendEmail(any(), any(), any());

        // when
        alarmSender.processAlarms();

        // then
        assertThat(failedAlarm.getFailReason()).isEqualTo("메일 전송 실패");
        assertThat(failedAlarm.isSent()).isFalse();
        assertThat(failedAlarm.getSentAt()).isNull();
    }

    @Test
    void processAlarms_FCM_알림_전송_검증() {
        // given
        Alarm fcmAlarm = Alarm.builder()
                .id(1L)
                .channel(AlarmChannel.FCM)
                .deviceToken("testDeviceToken")
                .title("Test FCM Title")
                .message("Test FCM Message")
                .build();

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(fcmAlarm));

        // when
        alarmSender.processAlarms();

        // then
        verify(pushService, times(1))
                .sendPush(fcmAlarm, "testDeviceToken", "Test FCM Title", "Test FCM Message");

        // 이메일 전송은 호출되지 않는지 검증
        verify(sesEmailService, never()).sendEmail(any(), any(), any());
    }
}