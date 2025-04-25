package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.service.fcm.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmAlarmProcessor {

    private final PushService pushService;

    public void processReservation(Alarm alarm) {
        pushService.sendReservationPush(alarm, alarm.getDeviceToken(), alarm.getTitle(), alarm.getMessage());
    }

    public void processPayment(com.parkez.alarm.dto.ReservationAlarmInfo info, com.parkez.alarm.domain.enums.NotificationType type) {
        String title = String.format("[결제] %d번 예약 결제 %s",
                info.getReservationId(),
                type == com.parkez.alarm.domain.enums.NotificationType.CANCELED ? "취소" : "실패");

        String message = type == com.parkez.alarm.domain.enums.NotificationType.CANCELED
                ? "결제가 취소되었습니다."
                : "결제에 실패했습니다.";

        pushService.sendPaymentPush(info.getUserId(), title, message);
    }
}
