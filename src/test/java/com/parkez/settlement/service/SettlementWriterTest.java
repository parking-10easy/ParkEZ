package com.parkez.settlement.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementWriterTest {

    @InjectMocks
    private SettlementWriter settlementWriter;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementDetailRepository settlementDetailRepository;

    @Test
    void 정산_및_정산_Details_저장_테스트() {
        // given
        User owner = mock(User.class);
        YearMonth month = YearMonth.of(2025, 4);

        Settlement savedSettlement = mock(Settlement.class);
        SettlementDetail settlementDetail = mock(SettlementDetail.class);
        List<SettlementDetail> savedSettlementDetail = List.of(settlementDetail);

        when(settlementRepository.saveAndFlush(any())).thenReturn(savedSettlement);
        when(settlementDetailRepository.saveAll(any())).thenReturn(savedSettlementDetail);

        // when
        settlementWriter.save(savedSettlement, savedSettlementDetail);

        // then
        verify(settlementRepository).saveAndFlush(any(Settlement.class));
        verify(settlementDetailRepository).saveAll(anyList());
    }

    @Test
    void 정산을_완료상태로_변경한다() {
        // given
        Settlement settlement = mock(Settlement.class);

        // when
        settlementWriter.completeSettlement(settlement);

        // then
        verify(settlement).complete(any());
    }
}
