package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
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
    private EmailService emailService;

    @InjectMocks
    private AlarmSender alarmSender;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(alarmSender, "fromMail", "noreply@test.com");
    }

    @Test
    void 이메일알람_전송_성공시_sent가_true로_업데이트된다() throws MessagingException {
        // given
        Alarm emailAlarm = Alarm.createEmailAlarm(1L, 1L, AlarmTargetType.RESERVATION, "test@email.com", "제목", "본문", NotificationType.UPCOMING);

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(emailAlarm));

        // when
        alarmSender.processAlarms();

        // then
        verify(emailService).sendEmail("test@email.com", "noreply@test.com", "제목", "본문");
        assertThat(emailAlarm.isSent()).isTrue();
        assertThat(emailAlarm.getSentAt()).isNotNull();
    }

    @Test
    void 이메일알람_전송_실패시_예외처리_및_failReason_설정된다() throws MessagingException {
        // given
        Alarm failedAlarm = Alarm.createEmailAlarm(1L, 1L, AlarmTargetType.RESERVATION, "fail@email.com", "제목", "본문", NotificationType.UPCOMING);
        ReflectionTestUtils.setField(failedAlarm, "channel", AlarmChannel.EMAIL);

        when(alarmRepository.findAllBySentFalse()).thenReturn(List.of(failedAlarm));

        doThrow(new RuntimeException("메일 전송 실패")).when(emailService).sendEmail(any(), any(), any(), any());

        // when
        alarmSender.processAlarms();

        // then
        assertThat(failedAlarm.getFailReason()).isEqualTo("메일 전송 실패");
        assertThat(failedAlarm.isSent()).isFalse();
        assertThat(failedAlarm.getSentAt()).isNull();
    }
}