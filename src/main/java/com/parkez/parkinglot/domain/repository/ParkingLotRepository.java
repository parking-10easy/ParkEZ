package com.parkez.parkinglot.domain.repository;


import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long>, ParkingLotQueryRepository {

    Optional<ParkingLot> findByIdAndDeletedAtIsNull(Long parkingLotId);

}
