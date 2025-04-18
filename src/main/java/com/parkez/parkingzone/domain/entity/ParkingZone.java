package com.parkez.parkingzone.domain.entity;

import com.parkez.common.entity.BaseDeleteEntity;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "parking_zone")
public class ParkingZone extends BaseDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(nullable = false)
    private String name;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ParkingZoneStatus status;

    private int reviewCount;

    @Builder
    private ParkingZone(ParkingLot parkingLot,
                        String name,
                        String imageUrl) {
        this.parkingLot = parkingLot;
        this.name = name;
        this.imageUrl = imageUrl;
        this.status = ParkingZoneStatus.AVAILABLE;
        this.reviewCount = 0;
    }

    public void updateParkingZoneName(String name) {
        this.name = name;
    }

    public void updateParkingZoneStatus(ParkingZoneStatus status) {
        this.status = status;
    }

    public void updateParkingZoneImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getParkingLotId() {
        return parkingLot.getId();
    }

    public String getParkingLotName() {
        return this.parkingLot.getName();
    }

    public BigDecimal getParkingLotPricePerHour() {
        return this.parkingLot.getPricePerHour();
    }

    public void incrementReviewCount() {
        this.reviewCount++;
    }

    public void decrementReviewCount() {
        if (this.reviewCount > 0) {
            this.reviewCount--;
        }
    }

    public LocalTime getParkingLotOpenedAt() {
        return this.getParkingLot().getOpenedAt();
    }

    public LocalTime getParkingLotClosedAt() {
        return this.getParkingLot().getClosedAt();
    }

    public boolean isOpened(LocalTime startTime, LocalTime endTime) {
        return !startTime.isBefore(this.getParkingLotOpenedAt())
                && !endTime.isAfter(this.getParkingLotClosedAt());
    }
}
