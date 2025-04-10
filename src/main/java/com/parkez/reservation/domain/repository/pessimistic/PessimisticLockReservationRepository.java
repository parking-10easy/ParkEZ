package com.parkez.reservation.domain.repository.pessimistic;

import com.parkez.reservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PessimisticLockReservationRepository extends JpaRepository<Reservation, Long> {
}
