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
import java.util.Objects;

@Entity
@Table(
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_order_id", columnNames = "order_id"),
                @UniqueConstraint(name = "uk_payment_key", columnNames = "payment_key")
        }
)
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
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private String orderId;

    private String paymentKey;

    private int cardFee;

    private LocalDateTime approvedAt;

    private LocalDateTime canceledAt;

    @Builder
    public Payment(User user, Reservation reservation,
                   PaymentType paymentType, PaymentStatus paymentStatus,
                   String paymentKey, String orderId, int cardFee, LocalDateTime approvedAt, LocalDateTime canceledAt) {
        this.user = user;
        this.reservation = reservation;
        this.price = reservation.getPrice();
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
        this.paymentKey = paymentKey;
        this.orderId = orderId;
        this.cardFee = cardFee;
        this.approvedAt = approvedAt;
        this.canceledAt = canceledAt;
    }


    // 승인 이후 payment 정보 update 하는 메서드
    public void approvePaymentInfo(String paymentKey, String approvedAt, PaymentType type) {
        this.paymentKey = paymentKey;
        this.paymentStatus = PaymentStatus.APPROVED;
        this.approvedAt = OffsetDateTime.parse(approvedAt).toLocalDateTime();
        this.paymentType = type;

    }

    public String getUserEmail() {
        return Objects.nonNull(user) ? user.getEmail() : null;
    }

    public String getUserPhone() {
        return Objects.nonNull(user) ? user.getPhone() : null;
    }

    public Long getUserId() {
        return Objects.nonNull(user) ? user.getId() : null;
    }

    public String getUserNickName(){
        return Objects.nonNull(user) ? user.getNickname() : null;
    }

    public Long getReservationId() {
        return Objects.nonNull(reservation) ? reservation.getId() : null;
    }

    public void cancel(LocalDateTime canceledAt) {
        this.paymentStatus = PaymentStatus.CANCELED;
        this.canceledAt = canceledAt;
    }


}
