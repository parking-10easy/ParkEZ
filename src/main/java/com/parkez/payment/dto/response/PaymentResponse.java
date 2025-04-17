package com.parkez.payment.dto.response;

import com.parkez.payment.domain.entity.Payment;
import com.parkez.payment.domain.enums.PaymentStatus;
import com.parkez.payment.domain.enums.PaymentType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {
    private Long id;
    private Long userId;
    private Long reservationId;
    private BigDecimal price;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private String orderId;
    private String paymentKey;
    private int cardFee;
    private LocalDateTime approvedAt;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .userId(payment.getUserId()) 
                .reservationId(payment.getReservationId())
                .price(payment.getPrice())
                .paymentType(payment.getPaymentType())
                .paymentStatus(payment.getPaymentStatus())
                .orderId(payment.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .cardFee(payment.getCardFee())
                .approvedAt(payment.getApprovedAt())
                .build();
    }


}
