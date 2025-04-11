package com.parkez.reservation.domain.repository.pessimistic;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PessimisticLockReservationRepository extends JpaRepository<Reservation, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.parkingZone = :parkingZone
            AND r.status IN :statusList
            AND (
            (:start < r.endDateTime AND :end > r.startDateTime)
            )
            """)
    List<Reservation> findByConditions(@Param("parkingZone") ParkingZone parkingZone,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("statusList") List<ReservationStatus> statusList
    );
}
