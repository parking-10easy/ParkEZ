package com.parkez.settlement.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.service.PaymentReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.dto.response.SettlementPreviewResponse;
import com.parkez.settlement.dto.response.SettlementReservationResponse;
import com.parkez.settlement.dto.response.SettlementResponse;
import com.parkez.settlement.exception.SettlementErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SettlementServiceTest {

    @InjectMocks
    private SettlementService settlementService;

    @Mock
    private SettlementReader settlementReader;
    @Mock
    private SettlementWriter settlementWriter;
    @Mock
    private PaymentReader paymentReader;
    @Mock
    private UserReader userReader;
    @Mock
    private ReservationReader reservationReader;

    @Nested
    class GenerateMonthlySettlementTest {

        @Test
        void 정산이_정상적으로_생성된다() {
            // given
            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);
            List<Payment> payments = List.of(
                    mock(Payment.class),
                    mock(Payment.class)
            );
            LocalDateTime settledAt = LocalDateTime.now();

            when(paymentReader.findApprovedPaymentsWithCompletedReservations(owner, month)).thenReturn(payments);
            when(payments.get(0).getPrice()).thenReturn(new BigDecimal("10000"));
            when(payments.get(1).getPrice()).thenReturn(new BigDecimal("5000"));

            // when
            SettlementBatchProcessResponse result = settlementService.generateMonthlySettlement(owner, month, settledAt);

            // then
            verify(settlementReader).validateNotSettled(owner, month);
            verify(paymentReader).findApprovedPaymentsWithCompletedReservations(owner, month);
        }
    }

    @Nested
    class CompleteSettlementTest {

        @Test
        void 정산이_CONFIRMED_상태일_경우_완료_처리된다() {
            // given
            Long settlementId = 1L;
            Settlement settlement = mock(Settlement.class);

            when(settlement.getStatus()).thenReturn(SettlementStatus.CONFIRMED);
            when(settlementReader.getById(settlementId)).thenReturn(settlement);

            // when
            settlementService.completeSettlement(settlementId);

            // then
            verify(settlementWriter).completeSettlement(settlement);
        }

        @Test
        void 정산이_PENDING_상태면_예외발생() {
            // given
            Long settlementId = 1L;
            Settlement settlement = mock(Settlement.class);

            when(settlement.getStatus()).thenReturn(SettlementStatus.PENDING);
            when(settlementReader.getById(settlementId)).thenReturn(settlement);

            // when & then
            assertThatThrownBy(() -> settlementService.completeSettlement(settlementId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.SETTLEMENT_NOT_CONFIRMABLE.getDefaultMessage());
        }

        @Test
        void 정산이_ALREADY_COMPLETED_상태면_예외발생() {
            // given
            Long settlementId = 1L;
            Settlement settlement = mock(Settlement.class);

            when(settlement.getStatus()).thenReturn(SettlementStatus.COMPLETED);
            when(settlementReader.getById(settlementId)).thenReturn(settlement);

            // when & then
            assertThatThrownBy(() -> settlementService.completeSettlement(settlementId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.ALREADY_SETTLED.getDefaultMessage());
        }
    }

    @Nested
    class GetPreviewTest {

        @Test
        void 예상_정산_정보를_정상적으로_조회한다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.builder()
                    .id(userId)
                    .email("owner@test.com")
                    .roleName("ROLE_OWNER")
                    .nickname("사장님")
                    .build();

            User owner = mock(User.class);
            YearMonth month = YearMonth.of(2025, 4);

            Payment p1 = mock(Payment.class);
            Payment p2 = mock(Payment.class);
            when(p1.getPrice()).thenReturn(new BigDecimal("10000"));
            when(p2.getPrice()).thenReturn(new BigDecimal("5000"));

            when(userReader.getActiveUserById(userId)).thenReturn(owner);
            when(paymentReader.findApprovedPaymentsWithCompletedReservations(owner, month)).thenReturn(List.of(p1, p2));

            // when
            SettlementPreviewResponse response = settlementService.getPreview(authUser, month);

            // then
            assertThat(response.getTotalAmount()).isEqualByComparingTo("15000");
            assertThat(response.getTotalFee()).isEqualByComparingTo("495.00");
            assertThat(response.getNetAmount()).isEqualByComparingTo("14505.00");
            assertThat(response.getReservationCount()).isEqualTo(2);
        }
    }

    @Nested
    class GetReservationSettlementTest {

        @Test
        void 정산대상_결제가_없는_경우_NOT_SETTLEMENT_ELIGIBLE_예외_발생() {
            // given
            Long userId = 1L;
            Long reservationId = 10L;

            AuthUser authUser = AuthUser.builder()
                    .id(userId)
                    .email("test@user.com")
                    .nickname("사용자")
                    .roleName("ROLE_OWNER")
                    .build();

            User owner = mock(User.class);
            Reservation reservation = mock(Reservation.class);
            when(reservation.getId()).thenReturn(reservationId);

            when(userReader.getActiveUserById(userId)).thenReturn(owner);
            when(reservationReader.findReservation(reservationId)).thenReturn(reservation);
            when(paymentReader.getApprovedPaymentWithCompletedReservation(owner, reservationId)).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> settlementService.getReservationSettlement(authUser, reservationId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(SettlementErrorCode.NOT_SETTLEMENT_ELIGIBLE.getDefaultMessage());
        }

        @Test
        void 예약에_대한_정산_내역을_정상적으로_조회한다() {
            // given
            Long userId = 1L;
            Long reservationId = 10L;

            AuthUser authUser = AuthUser.builder()
                    .id(userId)
                    .email("test@user.com")
                    .nickname("사용자")
                    .roleName("ROLE_OWNER")
                    .build();

            User owner = mock(User.class);

            Reservation reservation = spy(Reservation.class);
            ReflectionTestUtils.setField(reservation, "id", reservationId);

            Payment payment = mock(Payment.class);

            when(userReader.getActiveUserById(userId)).thenReturn(owner);
            when(reservationReader.findReservation(reservationId)).thenReturn(reservation);
            when(paymentReader.getApprovedPaymentWithCompletedReservation(owner, reservationId)).thenReturn(payment);
            when(settlementReader.findSettlementStatus(reservationId)).thenReturn(SettlementStatus.CONFIRMED);

            when(payment.getReservation()).thenReturn(reservation);
            when(payment.getReservationId()).thenReturn(reservationId);
            when(payment.getPrice()).thenReturn(new BigDecimal("10000"));
            when(payment.getOrderId()).thenReturn("ORD123");
            when(payment.getApprovedAt()).thenReturn(LocalDateTime.of(2025, 4, 10, 10, 0));

            // when
            SettlementReservationResponse response = settlementService.getReservationSettlement(authUser, reservationId);

            // then
            assertThat(response.getReservationId()).isEqualTo(reservationId);
            assertThat(response.getOrderId()).isEqualTo("ORD123");
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("10000"));
            assertThat(response.getFee()).isEqualByComparingTo(new BigDecimal("330"));
            assertThat(response.getNetAmount()).isEqualByComparingTo(new BigDecimal("9670"));
            assertThat(response.getStatus()).isEqualTo(SettlementStatus.CONFIRMED);
            assertThat(response.getPaidAt()).isEqualTo(LocalDateTime.of(2025, 4, 10, 10, 0));
        }

    }

    @Nested
    class GetConfirmedSettlementTest {

        @Test
        void 확정된_정산_내역을_정상적으로_조회한다() {
            // given
            Long userId = 1L;
            YearMonth month = YearMonth.of(2025, 4);

            AuthUser authUser = AuthUser.builder()
                    .id(userId)
                    .email("owner@test.com")
                    .nickname("사장님")
                    .roleName("ROLE_OWNER")
                    .build();

            User owner = mock(User.class);
            Settlement settlement = mock(Settlement.class);

            when(userReader.getActiveUserById(userId)).thenReturn(owner);
            when(settlementReader.getByOwnerAndMonth(owner, month)).thenReturn(settlement);

            // when
            SettlementResponse response = settlementService.getConfirmedSettlement(authUser, month);

            // then
            SettlementResponse expected = SettlementResponse.from(settlement);
            assertThat(response).usingRecursiveComparison().isEqualTo(expected);
        }
    }
}
