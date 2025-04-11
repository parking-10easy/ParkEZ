package com.parkez.reservation.service.jpaconcurrency.pessimistic;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.domain.repository.pessimistic.PessimisticLockReservationRepository;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PessimisticLockReservationWriter {

    private final PessimisticLockReservationRepository pessimisticLockReservationRepository;

    public Reservation createPessimisticLockReservation(
            User user,
            ParkingZone parkingZone,
            String parkingLotName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            BigDecimal price
    ) {
        // 이미 해당 시간에 예약이 존재할 경우
        List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
        List<Reservation> reservations = pessimisticLockReservationRepository.findByConditions(parkingZone, startDateTime, endDateTime, statusList);
        if (!reservations.isEmpty()) {
            throw new ParkingEasyException(ReservationErrorCode.ALREADY_RESERVED);
        }

        Reservation reservation = Reservation.builder()
                .user(user)
                .parkingZone(parkingZone)
                .parkingLotName(parkingLotName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .price(price)
                .build();

        return pessimisticLockReservationRepository.save(reservation);
    }
}
