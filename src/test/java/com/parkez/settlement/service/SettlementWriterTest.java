package com.parkez.settlement.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.settlement.domain.entity.Settlement;
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
    void 정산과_정산상세가_정상적으로_저장된다() {
        // given
        User owner = mock(User.class);
        YearMonth month = YearMonth.of(2025, 4);

        Reservation reservation = mock(Reservation.class);
        Payment payment = mock(Payment.class);
        when(payment.getPrice()).thenReturn(new BigDecimal("10000"));
        when(payment.getReservation()).thenReturn(reservation);

        List<Payment> payments = List.of(payment);

        BigDecimal totalAmount = new BigDecimal("10000");
        BigDecimal totalFee = totalAmount.multiply(BigDecimal.valueOf(0.033));
        BigDecimal netAmount = totalAmount.subtract(totalFee);

        Settlement savedSettlement = mock(Settlement.class);
        when(settlementRepository.save(any())).thenReturn(savedSettlement);

        // when
        settlementWriter.writeMonthlySettlement(owner, month, payments, totalAmount, totalFee, netAmount);

        // then
        verify(settlementRepository).save(any(Settlement.class));
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
