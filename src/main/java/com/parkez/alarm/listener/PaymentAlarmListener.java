package com.parkez.alarm.listener;

import com.parkez.alarm.event.PaymentEvent;
import com.parkez.alarm.service.AlarmSender;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAlarmListener {

    private final AlarmSender alarmSender;

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        alarmSender.processPaymentAlarms(event.getReservationAlarmInfo(), event.getNotificationType());
    }
}