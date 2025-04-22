package com.parkez.settlement.domain.entity;

import com.parkez.common.entity.BaseEntity;
import com.parkez.settlement.domain.enums.SettlementStatus;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private YearMonth settlementMonth;

    @Column(nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal totalFee;

    @Column(nullable = false)
    private BigDecimal netAmount;

    private LocalDateTime calculatedAt;

    private LocalDateTime settledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @Builder
    public Settlement(User owner, YearMonth settlementMonth, BigDecimal totalAmount, BigDecimal totalFee, BigDecimal netAmount, LocalDateTime calculatedAt, LocalDateTime settledAt, SettlementStatus status) {
        this.owner = owner;
        this.settlementMonth = settlementMonth;
        this.totalAmount = totalAmount;
        this.totalFee = totalFee;
        this.netAmount = netAmount;
        this.calculatedAt = calculatedAt;
        this.settledAt = settledAt;
        this.status = status;
    }

    public void complete(LocalDateTime settledAt) {
        this.status = SettlementStatus.COMPLETED;
        this.settledAt = settledAt;
    }
}