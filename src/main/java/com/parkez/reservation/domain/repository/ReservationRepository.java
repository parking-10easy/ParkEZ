package com.parkez.reservation.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.parkingZone = :parkingZone
          AND r.status <> 'CANCELED'
          AND (
            (:start < r.endDateTime AND :end > r.startDateTime)
          )
    """)
    boolean existsReservation(@Param("parkingZone") ParkingZone parkingZone,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    boolean existsByUserId(Long userId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);
}
