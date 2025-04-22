package com.parkez.settlement.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import com.parkez.settlement.exception.SettlementErrorCode;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SettlementReader {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    public void validateNotSettled(User owner, YearMonth month) {
        settlementRepository.findByOwnerAndSettlementMonth(owner, month)
                .ifPresent(settlement -> { throw new ParkingEasyException(SettlementErrorCode.ALREADY_SETTLED); });
    }

    public Settlement getById(Long settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new ParkingEasyException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));
    }

    public Settlement getByOwnerAndMonth(User owner, YearMonth month) {
        return settlementRepository.findByOwnerAndSettlementMonth(owner, month)
                .orElseThrow(() -> new ParkingEasyException(SettlementErrorCode.SETTLEMENT_NOT_FOUND));
    }

    public SettlementStatus findSettlementStatus(Long reservationId) {
        return settlementDetailRepository.findByReservationId(reservationId)
                .map(detail -> detail.getSettlement().getStatus())
                .orElse(SettlementStatus.PENDING);
    }

}
