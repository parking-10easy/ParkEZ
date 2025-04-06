package com.parkez.reservation.domain.repository;

import com.parkez.reservation.domain.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
