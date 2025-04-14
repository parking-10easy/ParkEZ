package com.parkez.payment.domain.entity;

import com.parkez.common.entity.BaseEntity;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.domain.enums.PaymentType;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private String paymentKey;

    private int cardFee;

    private LocalDateTime approvedAt;

    @Builder
    public Payment(User user, Reservation reservation, BigDecimal totalPrice,
                   PaymentType paymentType, PaymentStatus paymentStatus,
                   String paymentKey, String orderId, int cardFee, LocalDateTime approvedAt) {
        this.user = user;
        this.reservation = reservation;
        this.totalPrice = totalPrice;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.cardFee = cardFee;
        this.approvedAt = approvedAt;
    }


    // 승인 이후 payment 정보 update 하는 메서드
    public void updatePaymentInfo(String paymentKey, String approvedAt, String type) {
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = OffsetDateTime.parse(approvedAt).toLocalDateTime();
        this.paymentType = PaymentType.from(type);

    }


}
