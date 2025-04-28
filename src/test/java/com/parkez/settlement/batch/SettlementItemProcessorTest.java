package com.parkez.settlement.batch;

import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.service.SettlementService;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class SettlementItemProcessorTest {

    @Mock
    private SettlementService settlementService;

    private SettlementItemProcessor settlementItemProcessor;

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
    void process_메서드_호출_시_정산_성공_case_정상적으로_settlement_및_settlementDetails_를_순차적으로_생성한다() {
        // given
         YearMonth targetMonth = YearMonth.of(2025, 3);
         String targetMonthString = targetMonth.toString();
//        LocalDateTime now = LocalDateTime.now();
//        String targetMonthString = now.toString();
        settlementItemProcessor = new SettlementItemProcessor(settlementService, targetMonthString);

        User owner = mockUser(1L);

        Settlement mockSettlement = mockSettlement(1L);
        SettlementDetail mockSettlementDetail = mockSettlementDetail(1L);
        List<SettlementDetail> details = List.of(mockSettlementDetail);
        SettlementBatchProcessResponse response = new SettlementBatchProcessResponse(mockSettlement, details, true, null);

        given(settlementService.generateMonthlySettlement(any(User.class), any(YearMonth.class), any(LocalDateTime.class))).willReturn(response);

        // when
        SettlementBatchProcessResponse result = settlementItemProcessor.process(owner);

        // then
        assertThat(result).extracting("settlement", "settlementDetails", "success")
                .isEqualTo(List.of(mockSettlement, details, true));
    }

    @Test
    void process_메서드_호출_시_정산_실패_case_예외_발생_시_settlement_및_settlementDetails_미생성_및_errorMessage_를_반환한다() {
        // given
         YearMonth targetMonth = YearMonth.of(2025, 3);
         String targetMonthString = targetMonth.toString();
//        LocalDateTime now = LocalDateTime.now();
//        String targetMonthString = now.toString();
        settlementItemProcessor = new SettlementItemProcessor(settlementService, targetMonthString);

        User owner = mockUser(1L);
        String errorMessage = "정산 생성 중 에러 발생";

        given(settlementService.generateMonthlySettlement(any(User.class), any(YearMonth.class), any(LocalDateTime.class))).willThrow(new RuntimeException(errorMessage));

        // when
        SettlementBatchProcessResponse result = settlementItemProcessor.process(owner);

        // then
        assertThat(result).extracting("success", "errorMessage")
                .isEqualTo(List.of(false, errorMessage));
    }
}