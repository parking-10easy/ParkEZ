package com.parkez.parkinglot.domain.repository;


import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {
}
