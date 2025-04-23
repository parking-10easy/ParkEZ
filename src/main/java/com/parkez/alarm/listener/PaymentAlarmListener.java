package com.parkez.alarm.listener;

import com.parkez.alarm.event.PaymentEvent;
import com.parkez.alarm.service.AsyncAlarmService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAlarmListener {

    private final AsyncAlarmService asyncAlarmService;

    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        asyncAlarmService.sendAlarms(event.getReservationAlarmInfo(), event.getNotificationType());
    }
}

