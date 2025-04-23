package com.parkez.settlement.service;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.settlement.domain.entity.Settlement;
import com.parkez.settlement.domain.entity.SettlementDetail;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.settlement.domain.repository.SettlementDetailRepository;
import com.parkez.settlement.domain.repository.SettlementRepository;
import com.parkez.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SettlementWriter {

    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;

    private static final double FEE_PERCENTAGE = 0.033; // 3.3%로 고정

    public void writeMonthlySettlement(User owner, YearMonth month, List<Payment> payments, BigDecimal totalAmount, BigDecimal totalFee, BigDecimal netAmount) {
        Settlement settlement = settlementRepository.save(
                Settlement.builder()
                        .owner(owner)
                        .settlementMonth(month)
                        .totalAmount(totalAmount)
                        .totalFee(totalFee)
                        .netAmount(netAmount)
                        .calculatedAt(LocalDateTime.now())
                        .status(SettlementStatus.CONFIRMED)
                        .build()
        );

        List<SettlementDetail> details = payments.stream()
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

        settlementDetailRepository.saveAll(details);
    }

    public void completeSettlement(Settlement settlement) {
        settlement.complete(LocalDateTime.now());
    }
}
