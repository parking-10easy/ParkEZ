package com.parkez.alarm.dto;

import com.parkez.reservation.domain.entity.Reservation;
import lombok.Getter;

@Getter
public class ReservationAlarmInfo {
    private final Long reservationId;
    private final Long userId;
    private final String userEmail;
    private final String parkingZoneName;

    private ReservationAlarmInfo(Long reservationId, Long userId, String userEmail, String parkingZoneName) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.parkingZoneName = parkingZoneName;
    }

    public static ReservationAlarmInfo from(Reservation reservation) {
        return new ReservationAlarmInfo(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getUserEmail(),
                reservation.getParkingZoneName()
        );
    }
}

