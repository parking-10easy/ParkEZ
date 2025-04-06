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

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationWriter {

    private final ReservationRepository reservationRepository;

    public Reservation createReservation(
            User user,
            ParkingZone parkingZone,
            String parkingLotName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            BigDecimal price
    ) {
        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLotName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();

        reservationRepository.save(reservation);
        return reservation;
    }
}
