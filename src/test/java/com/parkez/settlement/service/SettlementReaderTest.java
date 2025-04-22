package com.parkez.settlement.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import com.parkez.settlement.exception.SettlementErrorCode;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SettlementReaderTest {

    @InjectMocks
    private SettlementReader settlementReader;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private SettlementDetailRepository settlementDetailRepository;

    @Nested
    class validateNotSettled{
        @Test
        void 정산이_이미_존재하면_ALREADY_SETTLED_예외_발생() {
            // given
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);
            Settlement existingSettlement = mock(Settlement.class);

            when(settlementRepository.findByOwnerAndSettlementMonth(owner, month))
                    .thenReturn(Optional.of(existingSettlement));

            // when & then
            assertThatThrownBy(() -> settlementReader.validateNotSettled(owner, month))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.ALREADY_SETTLED.getDefaultMessage());
        }

        @Test
        void 정산이_없으면_예외없이_통과() {
            // given
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);

            when(settlementRepository.findByOwnerAndSettlementMonth(owner, month))
                    .thenReturn(Optional.empty());

            // when & then
            assertThatCode(() -> settlementReader.validateNotSettled(owner, month))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    class GetByIdTest {
        @Test
        void settlement_ID로_정산_조회에_성공한다() {
            Long settlementId = 1L;
            Settlement settlement = mock(Settlement.class);
            when(settlementRepository.findById(settlementId)).thenReturn(Optional.of(settlement));

            Settlement result = settlementReader.getById(settlementId);

            assertThat(result).isEqualTo(settlement);
        }

        @Test
        void settlement_ID로_정산_조회시_없으면_SETTLEMENT_NOT_FOUND_예외_발생() {
            Long settlementId = 1L;
            when(settlementRepository.findById(settlementId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementReader.getById(settlementId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.SETTLEMENT_NOT_FOUND.getDefaultMessage());
        }
    }
    @Nested
    class GetByOwnerAndMonthTest {
        @Test
        void Owner와_월로_정산_조회_정상적으로_성공() {
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);
            Settlement settlement = mock(Settlement.class);
            when(settlementRepository.findByOwnerAndSettlementMonth(owner, month)).thenReturn(Optional.of(settlement));

            Settlement result = settlementReader.getByOwnerAndMonth(owner, month);

            assertThat(result).isEqualTo(settlement);
        }

        @Test
        void Owner와_월로_정산_조회시_없으면_SETTLEMENT_NOT_FOUND_예외_발생() {
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);
            when(settlementRepository.findByOwnerAndSettlementMonth(owner, month)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> settlementReader.getByOwnerAndMonth(owner, month))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.SETTLEMENT_NOT_FOUND.getDefaultMessage());
        }
    }

    @Nested
    class FindSettlementStatusTest {
        @Test
        void 예약ID로_정산상태_정상적으로_조회_성공() {
            Long reservationId = 10L;
            Settlement settlement = mock(Settlement.class);
            when(settlement.getStatus()).thenReturn(SettlementStatus.CONFIRMED);

            SettlementDetail detail = mock(SettlementDetail.class);
            when(detail.getSettlement()).thenReturn(settlement);

            when(settlementDetailRepository.findByReservationId(reservationId)).thenReturn(Optional.of(detail));

            SettlementStatus result = settlementReader.findSettlementStatus(reservationId);

            assertThat(result).isEqualTo(SettlementStatus.CONFIRMED);
        }

        @Test
        void 정산_세부내역_없으면_PENDING을_반환한다() {
            Long reservationId = 10L;
            when(settlementDetailRepository.findByReservationId(reservationId)).thenReturn(Optional.empty());

            SettlementStatus result = settlementReader.findSettlementStatus(reservationId);

            assertThat(result).isEqualTo(SettlementStatus.PENDING);
        }
    }

}
