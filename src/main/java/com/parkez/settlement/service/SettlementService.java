package com.parkez.settlement.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.service.PaymentReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.dto.response.SettlementBatchProcessResponse;
import com.parkez.settlement.dto.response.SettlementPreviewResponse;
import com.parkez.settlement.dto.response.SettlementReservationResponse;
import com.parkez.settlement.dto.response.SettlementResponse;
import com.parkez.settlement.exception.SettlementErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private static final double FEE_PERCENTAGE = 0.033; // 3.3%로 고정

    private final UserReader userReader;
    private final PaymentReader paymentReader;
    private final ReservationReader reservationReader;
    private final SettlementReader settlementReader;
    private final SettlementWriter settlementWriter;

    @Getter
    @RequiredArgsConstructor
    public static class SettlementAmounts {
        private final BigDecimal totalAmount;
        private final BigDecimal totalFee;
        private final BigDecimal netAmount;
    }

    public SettlementPreviewResponse getPreview(AuthUser authUser, YearMonth yearMonth) {

        User owner = userReader.getActiveUserById(authUser.getId());

        List<Payment> payments = paymentReader.findApprovedPaymentsWithCompletedReservations(owner, yearMonth);

        SettlementAmounts settlementAmounts = calculateSettlementAmounts(payments);

        long reservationCount = payments.size();

        return SettlementPreviewResponse.of(yearMonth, settlementAmounts.getTotalAmount(), settlementAmounts.getTotalFee(), settlementAmounts.getNetAmount(), reservationCount);
    }

    public SettlementReservationResponse getReservationSettlement(AuthUser authUser, Long reservationId) {

        User owner = userReader.getActiveUserById(authUser.getId());

        Reservation reservation = reservationReader.findReservation(reservationId);

        Payment payment = paymentReader.getApprovedPaymentWithCompletedReservation(owner, reservation.getId());

        if (payment == null) {
            throw new ParkingEasyException(SettlementErrorCode.NOT_SETTLEMENT_ELIGIBLE);
        }

        SettlementStatus status = settlementReader.findSettlementStatus(payment.getReservationId());

        return SettlementReservationResponse.of(payment, FEE_PERCENTAGE, status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public SettlementBatchProcessResponse generateMonthlySettlement(User owner, YearMonth month) {
        // 이미 정산된 달인지 확인
        settlementReader.validateNotSettled(owner, month);

        // 결제 완료 + 예약 완료된 Payment 리스트 조회
        List<Payment> payments = paymentReader.findApprovedPaymentsWithCompletedReservations(owner, month);

        SettlementAmounts settlementAmounts = calculateSettlementAmounts(payments);

        Settlement settlement = Settlement.builder()
                .owner(owner)
                .settlementMonth(month)
                .totalAmount(settlementAmounts.getTotalAmount())
                .totalFee(settlementAmounts.getTotalFee())
                .netAmount(settlementAmounts.getNetAmount())
                .calculatedAt(LocalDateTime.now())
                .status(SettlementStatus.CONFIRMED)
                .build();

        List<SettlementDetail> settlementDetails = payments.stream()
                .map(p -> {
                    BigDecimal price = p.getPrice();
                    BigDecimal fee = price.multiply(BigDecimal.valueOf(FEE_PERCENTAGE));
                    BigDecimal net = price.subtract(fee);
                    return SettlementDetail.builder()
                            .settlement(settlement)
                            .reservation(p.getReservation())
                            .amount(price)
                            .fee(fee)
                            .netAmount(net)
                            .build();
                })
                .toList();

        return SettlementBatchProcessResponse.success(settlement, settlementDetails);
    }

    private SettlementAmounts calculateSettlementAmounts(List<Payment> payments) {

        BigDecimal totalAmount = payments.stream()
                .map(Payment::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalFee = totalAmount.multiply(BigDecimal.valueOf(FEE_PERCENTAGE));
        BigDecimal netAmount = totalAmount.subtract(totalFee);

        return new SettlementAmounts(totalAmount, totalFee, netAmount);
    }

    public void completeSettlement(Long settlementId) {

        Settlement settlement = settlementReader.getById(settlementId);

        switch (settlement.getStatus()) {
            case PENDING:
                throw new ParkingEasyException(SettlementErrorCode.SETTLEMENT_NOT_CONFIRMABLE);
            case COMPLETED:
                throw new ParkingEasyException(SettlementErrorCode.ALREADY_SETTLED);
        }

        settlementWriter.completeSettlement(settlement);
    }

    public SettlementResponse getConfirmedSettlement(AuthUser authUser, YearMonth month) {

        User owner = userReader.getActiveUserById(authUser.getId());

        Settlement settlement = settlementReader.getByOwnerAndMonth(owner, month);
        return SettlementResponse.from(settlement);
    }

    public void saveSettlementAndDetails(Settlement settlement, List<SettlementDetail> settlementDetails) {

        settlementWriter.save(settlement, settlementDetails);
    }
}
