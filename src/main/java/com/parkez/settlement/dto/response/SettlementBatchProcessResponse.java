package com.parkez.settlement.dto.response;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class SettlementBatchProcessResponse {

    private final Settlement settlement;
    private final List<SettlementDetail> settlementDetails;
    private final boolean success;
    private final String errorMessage;

    public static SettlementBatchProcessResponse success(Settlement settlement, List<SettlementDetail> settlementDetails) {
        return new SettlementBatchProcessResponse(settlement, settlementDetails, true, null);
    }

    public static SettlementBatchProcessResponse failure(String errorMessage) {
        return new SettlementBatchProcessResponse(null, List.of(), false, errorMessage);
    }
}
