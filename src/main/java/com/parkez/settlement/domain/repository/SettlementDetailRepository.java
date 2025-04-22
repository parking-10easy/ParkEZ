package com.parkez.settlement.domain.repository;

import com.parkez.settlement.domain.entity.SettlementDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {
    Optional<SettlementDetail> findByReservationId(Long reservationId);
}
