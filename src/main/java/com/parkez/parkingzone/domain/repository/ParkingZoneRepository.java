package com.parkez.parkingzone.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {

    @Query("""
           SELECT pz FROM ParkingZone pz
           LEFT JOIN FETCH pz.parkingLot pl
           WHERE pz.parkingLot.id = :parkingLotId
           AND pz.deletedAt IS NULL
           ORDER BY pz.modifiedAt DESC
       """)
    Page<ParkingZone> findAllByParkingLotIdOrderByModifiedAtDesc(Pageable pageable, @Param("parkingLotId") Long parkingLotId);

    @Query("""
        SELECT pz FROM ParkingZone pz
        JOIN FETCH pz.parkingLot pl
        WHERE pz.id = :parkingZoneId
        AND pz.deletedAt IS NULL
        AND pl.deletedAt IS NULL
    """)
    Optional<ParkingZone> findByIdAndDeletedAtIsNull(@Param("parkingZoneId") Long parkingZoneId);

    @Query("""
        SELECT COUNT(pz) > 0 FROM ParkingZone pz
        JOIN pz.parkingLot pl
        WHERE pz.id = :parkingZoneId
        AND pl.owner.id = :ownerId
        AND pl.deletedAt IS NULL
        AND pz.deletedAt IS NULL
    """)
    boolean existsByIdAndOwnerId(@Param("parkingZoneId") Long parkingZoneId, @Param("ownerId") Long ownerId);

    @Modifying
    @Query("""
            UPDATE ParkingZone pz SET pz.deletedAt = :deletedAt
            WHERE pz.id = :parkingZoneId
        """)
    void softDeleteById(@Param("parkingZoneId") Long parkingZoneId, @Param("deletedAt") LocalDateTime deletedAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pz
            FROM ParkingZone pz
            WHERE pz.id = :parkingZoneId
            """)
    Optional<ParkingZone> findByIdWithPessimisticLock(@Param("parkingZoneId") Long parkingZoneId);
}
