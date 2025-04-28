package com.parkez.settlement.batch;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementItemWriterTest {

    @Mock
    private SettlementService settlementService;

    @InjectMocks
    private SettlementItemWriter settlementItemWriter;

    private User mockUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Settlement mockSettlement(Long id) {
        Settlement settlement = Settlement.builder().build();
        ReflectionTestUtils.setField(settlement, "id", id);
        return settlement;
    }

    private SettlementDetail mockSettlementDetail(Long id) {
        SettlementDetail settlementDetail = SettlementDetail.builder().build();
        ReflectionTestUtils.setField(settlementDetail, "id", id);
        return settlementDetail;
    }

    @Test
    void write_메서드_호출_시_정산_성공_case_success_값이_true_인_dto에_대해_정상적으로_settlement_및_settlementDetails_를_DB에_저장한다() {
        // given
        User owner = mockUser(1L);

        Settlement mockSettlement = mockSettlement(1L);
        ReflectionTestUtils.setField(mockSettlement, "owner", owner);

        SettlementDetail mockSettlementDetail = mockSettlementDetail(1L);
        List<SettlementDetail> details = List.of(mockSettlementDetail);
        SettlementBatchProcessResponse item = new SettlementBatchProcessResponse(mockSettlement, details, true, null);

        doNothing().when(settlementService).saveSettlementAndDetails(any(Settlement.class), anyList());

        // when
        settlementItemWriter.write(Chunk.of(item));

        // then
        verify(settlementService).saveSettlementAndDetails(any(Settlement.class), anyList());
    }

    @Test
    void write_메서드_호출_시_정산_실패_case_success_값이_false_인_dto에_대해_정산_미진행() {
        // given
        String errorMessage = "정산 생성 중 에러 발생";
        SettlementBatchProcessResponse item = new SettlementBatchProcessResponse(null, null, false, errorMessage);

        // when
        settlementItemWriter.write(Chunk.of(item));

        // then
        verify(settlementService, never()).saveSettlementAndDetails(any(Settlement.class), anyList());
    }
}