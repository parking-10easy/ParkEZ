package com.parkez.alarm.dto;

import com.parkez.reservation.domain.entity.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationAlarmInfo {
    private final Long reservationId;
    private final Long userId;
    private final String userEmail;
    private final String userName;
    private final String parkingLotName;
    private final String parkingZoneName;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    private ReservationAlarmInfo(Long reservationId, Long userId, String userEmail, String userName, String parkingLotName, String parkingZoneName, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.parkingLotName = parkingLotName;
        this.parkingZoneName = parkingZoneName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

    public static ReservationAlarmInfo from(Reservation reservation) {
        return new ReservationAlarmInfo(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getUserEmail(),
                reservation.getUserNickName(),
                reservation.getParkingLotName(),
                reservation.getParkingZoneName(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime()
        );
    }
}

