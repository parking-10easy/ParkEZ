package com.parkez.alarm.service.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushService {

    private final AlarmRepository alarmRepository;
    private final FcmDeviceRepository fcmDeviceRepository;

    @Transactional
    public void sendReservationPush(Alarm alarm, String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("예약 푸시 알림 전송 성공 response={} ",response);
            alarm.updateSent(true);
            alarm.updateSentAt(LocalDateTime.now());

        } catch (FirebaseMessagingException e) {
            log.info("예약 푸시 알림 전송 실패 e.getMessage={} ",e.getMessage());
            alarm.updateFailReason(e.getMessage());
            alarmRepository.flush();
        }
    }

    public void sendPaymentPush(Long userId, String title, String message) {
        try {
            String token = fetchDeviceToken(userId);

            Message fcmMessage = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder().setTitle(title).setBody(message).build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(fcmMessage);
            log.info("결제 푸시 알림 전송 성공 response={}", response);

        } catch (FirebaseMessagingException e) {
            log.error("결제 푸시 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

    private String fetchDeviceToken(Long userId) {
        return fcmDeviceRepository.findFirstByUserId(userId)
                .map(FcmDevice::getToken)
                .orElseThrow(() -> new IllegalArgumentException("FCM 토큰이 존재하지 않습니다. userId=" + userId));
    }

}