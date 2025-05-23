package com.parkez.reservation.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.response.ReservationWithReviewDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("""
                SELECT
                    CASE
                        WHEN COUNT(r) > 0 THEN true
                        ELSE false
                    END
                FROM Reservation r
                WHERE r.parkingZone = :parkingZone
                  AND r.status IN :statusList
                  AND (
                    (:start < r.endDateTime AND :end > r.startDateTime)
                  )
            """)
    boolean existsReservationByConditions(@Param("parkingZone") ParkingZone parkingZone,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end,
                                          @Param("statusList") List<ReservationStatus> statusList);

    boolean existsByUser_Id(Long userId);

    @Query("""
                SELECT new com.parkez.reservation.dto.response.ReservationWithReviewDto(r,
                    CASE
                        WHEN rv.id IS NOT NULL THEN true
                        ELSE false
                    END)
                FROM Reservation r
                LEFT JOIN Review rv ON rv.reservation = r
                WHERE r.user.id = :userId
            """)
    Page<ReservationWithReviewDto> findAllWithReviewByUser_Id(@Param("userId") Long userId, Pageable pageable);

    boolean existsByParkingZone_Id(Long parkingZoneId);

    @Query("""
    SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
    FROM Reservation r
    WHERE r.parkingZone.id = :parkingZoneId
    AND r.status IN ('PENDING', 'CONFIRMED')
""")
    boolean existsActiveReservationByParkingZoneId(@Param("parkingZoneId") Long parkingZoneId);

    Page<Reservation> findAllByParkingZone_Id(Long parkingZoneId, Pageable pageable);

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.status = 'PENDING'
              AND r.createdAt <= :expiredTime
            """)
    List<Reservation> findReservationsToExpire(@Param("expiredTime") LocalDateTime expiredTime);

    /* alarm 도메인에서 필요 - 예약 만료 10분 전 알림 */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.status = :status
              AND r.endDateTime BETWEEN :start AND :end
    """)
    List<Reservation> findConfirmedReservationsBetween(
            @Param("status") ReservationStatus status,
            @Param("start") LocalDateTime now,
            @Param("end") LocalDateTime tenMinLater
    );

    /* alarm 도메인에서 필요 - 예약 만료 시 알림 */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.status = :status
              AND r.endDateTime < :now
    """)
    List<Reservation> findExpiredReservations(
            @Param("status") ReservationStatus status,
            @Param("now") LocalDateTime now
    );

    Optional<Reservation> findByParkingZone_IdAndStartDateTimeAndEndDateTime(Long parkingZoneId, LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.user
            JOIN FETCH r.parkingZone
            WHERE r.id = :id
    """)
    Optional<Reservation> findByIdWithUserAndParkingZone(@Param("id") Long reservationId);

    @Query("""
    SELECT
        CASE
            WHEN COUNT(r) > 0 THEN true
            ELSE false
        END
    FROM Reservation r
    WHERE r.parkingZone = :parkingZone
      AND r.user.id = :userId
      AND r.status IN :statusList
      AND (
        (:start < r.endDateTime AND :end > r.startDateTime)
      )
""")
    boolean existsReservationByConditionsForUser(
            @Param("parkingZone") ParkingZone parkingZone,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("userId") Long userId,
            @Param("statusList") List<ReservationStatus> statusList
    );
}