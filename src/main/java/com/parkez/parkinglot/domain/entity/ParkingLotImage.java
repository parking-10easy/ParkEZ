package com.parkez.parkinglot.domain.entity;

import com.parkez.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "parking_lot_image")
public class ParkingLotImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(nullable = false)
    private String imageUrl;

    @Builder
    private ParkingLotImage(ParkingLot parkingLot, String imageUrl) {
        this.parkingLot = parkingLot;
        this.imageUrl = imageUrl;
    }

    public void updateParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
}