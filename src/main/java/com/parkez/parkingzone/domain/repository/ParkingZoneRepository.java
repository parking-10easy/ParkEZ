package com.parkez.parkingzone.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone, Long> {

    @Query("""
           SELECT pz FROM ParkingZone pz
           LEFT JOIN FETCH pz.parkingLot
           WHERE pz.parkingLot.id = :parkingLotId
           AND pz.deletedAt IS NULL
           ORDER BY pz.modifiedAt DESC
       """)
    Page<ParkingZone> findAllOrderByModifiedAt(Pageable pageable, @Param("parkingLotId") Long parkingLotId);

    Optional<ParkingZone> findByIdAndDeletedAtIsNull(Long parkingZoneId);

    boolean existsByIdAndDeletedAtIsNull(Long parkingZoneId);

    @Modifying
    @Query("""
            UPDATE ParkingZone pz SET pz.deletedAt = :now
            WHERE pz.id = :parkingZoneId
        """)
    void softDeleteById(@Param("parkingZoneId") Long parkingZoneId, @Param("now") LocalDateTime now);
}
