package com.parkez.parkinglot.domain.repository;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParkingLotRepository extends JpaRepository<ParkingLot, Long> {

    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 조회 메서드   */
    Optional<ParkingLot> findByIdAndDeletedAtIsNull(Long parkingLotId);
    /* ParkingZone 도메인에서 필요한 메서드 - 특정 주차장 존재확인 메서드 */
    boolean existsByIdAndDeletedAtIsNull(Long parkingLotId);
}
