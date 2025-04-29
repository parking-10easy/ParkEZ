package com.parkez.alarm.pubsub;

import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.domain.enums.NotificationType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
public class PaymentAlarmMessage implements Serializable {
    private ReservationAlarmInfo reservationAlarmInfo;
    private NotificationType notificationType;

    public PaymentAlarmMessage(ReservationAlarmInfo reservationAlarmInfo, NotificationType notificationType) {
        this.reservationAlarmInfo = reservationAlarmInfo;
        this.notificationType = notificationType;
    }
}