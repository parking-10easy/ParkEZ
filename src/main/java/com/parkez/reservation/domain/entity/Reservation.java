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
import java.time.Duration;
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

    private LocalDateTime useCompletionTime;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private boolean reviewWritten;

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
        this.useCompletionTime = null;
        this.price = price;
        this.status = ReservationStatus.PENDING;
        this.reviewWritten = false;
    }

    public void complete(LocalDateTime useCompletionTime) {
        this.status = ReservationStatus.COMPLETED;
        this.useCompletionTime = useCompletionTime;
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

    public boolean isCompleted() {
        return this.status == ReservationStatus.COMPLETED;
    }

    public void confirm(){
        this.status = ReservationStatus.CONFIRMED;
    }


    public boolean isTimeout(LocalDateTime currentTime, long timeoutMinutes) {
        long elapsedMillis = Duration.between(this.getCreatedAt(), currentTime).toMillis();
        return elapsedMillis > timeoutMinutes * 60 * 1000;
    }

    public void writeReview() {
        this.reviewWritten = true;
    }

    public boolean canBeCanceled() {
        return this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED;
    }

    public boolean isAfter(LocalDateTime cancelLimitHour, LocalDateTime now) {
        return now.isAfter(cancelLimitHour);
    }

    public String getParkingZoneName() {
        return parkingZone.getName();
    }

    public String getUserEmail() {
        return user.getEmail();
    }
}
