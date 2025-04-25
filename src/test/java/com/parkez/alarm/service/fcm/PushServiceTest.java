package com.parkez.alarm.service.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private FcmDeviceRepository fcmDeviceRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;

    @InjectMocks
    private PushService pushService;

    private Alarm createTestAlarm() {
        return Alarm.builder()
                .userId(1L)
                .targetId(100L)
                .targetType(AlarmTargetType.RESERVATION)
                .channel(AlarmChannel.FCM)
                .title("Test Title")
                .message("Test Message")
                .deviceToken("test-token")
                .notificationType(NotificationType.UPCOMING)
                .sent(false)
                .build();
    }

    @Nested
    class SendReservationPush{
        @Test
        void 예약_푸시알림_전송_성공() throws Exception {
            Alarm alarm = createTestAlarm();

            try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
                firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
                when(firebaseMessaging.send(any(Message.class))).thenReturn("success-response");

                pushService.sendReservationPush(alarm, alarm.getDeviceToken(), alarm.getTitle(), alarm.getMessage());

                verify(firebaseMessaging).send(any(Message.class));
                assertThat(alarm.isSent()).isTrue();
                assert alarm.getSentAt() != null;
            }
        }

        @Test
        void 예약_푸시알림_전송_실패시_실패사유_저장() throws Exception {
            Alarm alarm = createTestAlarm();

            try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
                firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // 가짜 예외 생성
                FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
                when(mockException.getMessage()).thenReturn("예약 푸시 알림 전송 실패");

                when(firebaseMessaging.send(any(Message.class))).thenThrow(mockException);

                pushService.sendReservationPush(alarm, alarm.getDeviceToken(), alarm.getTitle(), alarm.getMessage());

                verify(alarmRepository).flush();
                assertThat(alarm.getFailReason()).contains("예약 푸시 알림 전송 실패");
            }
        }
    }


    @Test
    void 결제_푸시알림_전송_성공() throws Exception {
        Long userId = 1L;
        String token = "payment-token";

        when(fcmDeviceRepository.findFirstByUserId(userId))
                .thenReturn(Optional.of(FcmDevice.of(userId, token)));

        try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
            firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);
            when(firebaseMessaging.send(any(Message.class))).thenReturn("payment-success-response");

            pushService.sendPaymentPush(userId, "결제 알림", "결제가 완료되었습니다.");

            verify(firebaseMessaging).send(any(Message.class));
        }
    }

    @Nested
    class SendPaymentPush{
        @Test
        void 결제_푸시알림_토큰없음_예외발생() {
            Long userId = 2L;

            when(fcmDeviceRepository.findFirstByUserId(userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    pushService.sendPaymentPush(userId, "결제 알림", "결제가 완료되었습니다.")
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("FCM 토큰이 존재하지 않습니다");
        }

        @Test
        void 결제_푸시알림_전송_실패_로깅() throws Exception {
            Long userId = 1L;
            String token = "payment-token";

            when(fcmDeviceRepository.findFirstByUserId(userId))
                    .thenReturn(Optional.of(FcmDevice.of(userId, token)));

            try (MockedStatic<FirebaseMessaging> firebaseMessagingStatic = mockStatic(FirebaseMessaging.class)) {
                firebaseMessagingStatic.when(FirebaseMessaging::getInstance).thenReturn(firebaseMessaging);

                // 여기 수정! -> mock 예외 생성
                FirebaseMessagingException mockException = mock(FirebaseMessagingException.class);
                when(mockException.getMessage()).thenReturn("결제 푸시 실패");

                when(firebaseMessaging.send(any(Message.class))).thenThrow(mockException);

                pushService.sendPaymentPush(userId, "결제 알림", "결제가 실패했습니다.");

                verify(firebaseMessaging).send(any(Message.class));
            }
        }
    }
}
