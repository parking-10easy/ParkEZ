package com.parkez.alarm.config;

import com.parkez.alarm.pubsub.PaymentAlarmPublisher;
import com.parkez.alarm.pubsub.PaymentAlarmSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class AlarmRedisPubSubConfig {

    private final RedisConnectionFactory connectionFactory;
    private final PaymentAlarmSubscriber paymentAlarmSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(paymentAlarmSubscriber, new PatternTopic(PaymentAlarmPublisher.CHANNEL));
        return container;
    }
}

