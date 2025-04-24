package com.parkez.alarm.pubsub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkez.alarm.service.AsyncAlarmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentAlarmSubscriber implements MessageListener {

    private final AsyncAlarmService asyncAlarmService;
    private final GenericJackson2JsonRedisSerializer redisSerializer;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            Object rawObject = redisSerializer.deserialize(message.getBody());
            PaymentAlarmMessage alarmMessage = objectMapper.convertValue(rawObject, PaymentAlarmMessage.class);

            asyncAlarmService.sendAlarms(alarmMessage.getReservationAlarmInfo(), alarmMessage.getNotificationType());

        } catch (Exception e) {
            log.error("❌ Redis 결제 알람 처리 실패", e);
        }
    }
}
