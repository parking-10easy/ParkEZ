package com.parkez.settlement.domain.entity;

import com.parkez.reservation.domain.entity.Reservation;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", unique = true, nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private BigDecimal netAmount;

    @Builder
    public SettlementDetail(Settlement settlement, Reservation reservation, BigDecimal amount, BigDecimal fee, BigDecimal netAmount) {
        this.settlement = settlement;
        this.reservation = reservation;
        this.amount = amount;
        this.fee = fee;
        this.netAmount = netAmount;
    }
}
