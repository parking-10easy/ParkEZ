package com.parkez.alarm.event;

import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PaymentEvent {
    private final ReservationAlarmInfo reservationAlarmInfo;
    private final NotificationType notificationType;
}
