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
public class FakeImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private String imageUrl;

    @Builder
    public FakeImage(ParkingLot parkingLot, String name, String imageUrl) {
        this.parkingLot = parkingLot;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public void updateParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }
}