package com.parkez.reservation.domain.entity;

import com.parkez.common.entity.BaseEntity;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parking_zone_id", nullable = false)
    private ParkingZone parkingZone;

    @Column(nullable = false)
    private String parkingLotName;

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Builder
    private Reservation(
            User user,
            ParkingZone parkingZone,
            String parkingLotName,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            BigDecimal price
    ) {
        this.user = user;
        this.parkingZone = parkingZone;
        this.parkingLotName = parkingLotName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.price = price;
        this.status = ReservationStatus.PENDING;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public void cancel() {
        this.status = ReservationStatus.CANCELED;
    }

    public void expire() {
        this.status = ReservationStatus.PAYMENT_EXPIRED;
    }

    public Long getUserId() {
        return this.user.getId();
    }

    public boolean isOwnedBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    public Long getParkingZoneId() {
        return this.parkingZone.getId();
    }
}
