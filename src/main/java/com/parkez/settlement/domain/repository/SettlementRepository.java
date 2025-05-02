package com.parkez.settlement.domain.repository;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByOwnerAndSettlementMonth(User owner, YearMonth settlementMonth);
}
