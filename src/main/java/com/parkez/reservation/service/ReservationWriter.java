package com.parkez.reservation.service;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.repository.ReservationRepository;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationWriter {

    private final ReservationRepository reservationRepository;

    public Reservation create(
            User user,
            ParkingZone parkingZone,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        // 요금 계산
        long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime);
        BigDecimal price = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingZone.getParkingLotName())
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();

        return reservationRepository.save(reservation);
    }

    public void complete(Reservation reservation) {
        reservation.complete(LocalDateTime.now());
    }

    public void cancel(Reservation reservation) {
        reservation.cancel();
    }

    public void updateStatusConfirm(Reservation reservation){
        reservation.confirm();
    }
}
