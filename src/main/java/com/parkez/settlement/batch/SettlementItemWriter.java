package com.parkez.settlement.batch;

import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.service.SettlementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SettlementItemWriter implements ItemWriter<SettlementBatchProcessResponse> {

    private final SettlementService settlementService;

    public SettlementItemWriter(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @Override
    public void write(Chunk<? extends SettlementBatchProcessResponse> items) {
        for (SettlementBatchProcessResponse item : items) {
            if (!item.isSuccess()) {
                log.warn("[정산 생략] 정산 오류 발생 : {}", item.getErrorMessage());
                continue;
            }
            // 성공한 정산 DB에 저장
            settlementService.saveSettlementAndDetails(item.getSettlement(), item.getSettlementDetails());
            log.info("[정산 저장 완료] ownerId={}, month={}",
                    item.getSettlement().getOwner().getId(),
                    item.getSettlement().getSettlementMonth());
        }
    }
}
