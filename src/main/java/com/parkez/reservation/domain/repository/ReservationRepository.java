package com.parkez.reservation.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.parkingZone = :parkingZone
          AND r.status IN :statusList
          AND (
            (:start < r.endDateTime AND :end > r.startDateTime)
          )
    """)
    boolean existsReservation(@Param("parkingZone") ParkingZone parkingZone,
                              @Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end,
                              @Param("statusList") List<ReservationStatus> statusList);

    boolean existsByUserId(Long userId);

    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    boolean existsByParkingZoneId(Long parkingZoneId);

    Page<Reservation> findByParkingZoneId(Long parkingZoneId, Pageable pageable);
}
