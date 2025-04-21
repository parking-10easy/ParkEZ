package com.parkez.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.repository.AlarmRepository;
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

    @Transactional
    public void sendPush(Alarm alarm, String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder().setTitle(title).setBody(body).build())
                .build();
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸쉬 알림 전송 성공 response={} ",response);
            alarm.updateSent(true);
            alarm.updateSentAt(LocalDateTime.now());

        } catch (FirebaseMessagingException e) {
            log.info("푸쉬 알림 전송 실패 e.getMessage={} ",e.getMessage());
            alarm.updateFailReason(e.getMessage());
            alarmRepository.flush();
        }
    }
}
