package com.parkez.parkingzone.domain.repository;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParkingZoneRepository extends JpaRepository<ParkingZone,Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pz
            FROM ParkingZone pz
            WHERE pz.id = :parkingZoneId
            """)
    Optional<ParkingZone> findByIdWithPessimisticLock(@Param("parkingZoneId") Long parkingZoneId);
}
