package com.parkez.settlement.service;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementWriter {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    public void save(Settlement settlement, List<SettlementDetail> settlementDetails) {
        settlementRepository.saveAndFlush(settlement);
        for (SettlementDetail settlementDetail : settlementDetails) {
            settlementDetail.updateSettlement(settlement);
        }
        settlementDetailRepository.saveAll(settlementDetails);
    }

    public void completeSettlement(Settlement settlement) {
        settlement.complete(LocalDateTime.now());
    }
}
