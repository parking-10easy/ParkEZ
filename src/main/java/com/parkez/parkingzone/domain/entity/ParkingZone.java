package com.parkez.parkingzone.domain.entity;

import com.parkez.common.entity.BaseEntity;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "parking_zone")
public class ParkingZone extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_lot_id", nullable = false)
    private ParkingLot parkingLot;

    private String name;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private ParkingZoneStatus status;

    private int reviewCount;

    private LocalDateTime deletedAt;

    @Builder
    private ParkingZone(ParkingLot parkingLot,
                        String name,
                        String imageUrl,
                        ParkingZoneStatus status) {
        this.parkingLot = parkingLot;
        this.name = name;
        this.imageUrl = imageUrl;
        this.status = status;
        this.reviewCount = 0;
    }
}
