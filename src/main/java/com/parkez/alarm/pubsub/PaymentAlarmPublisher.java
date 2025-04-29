package com.parkez.alarm.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAlarmPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String CHANNEL = "payment-alarm";

    public void publish(PaymentAlarmMessage message) {
        redisTemplate.convertAndSend(CHANNEL, message);
    }
}
