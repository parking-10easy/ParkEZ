package com.parkez.parkinglot.domain.entity;

import com.parkez.common.entity.BaseEntity;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "parking_lot")
public class ParkingLot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 200)
    private String address;
    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private LocalDateTime openedAt;

    @Column(nullable = false)
    private LocalDateTime closedAt;

    @Column(nullable = false)
    private BigDecimal pricePerHour;

    @Lob
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private ChargeType chargeType;

    @Enumerated(EnumType.STRING)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    private ParkingLotStatus status;

    private LocalDateTime deletedAt;

    @Builder
    private ParkingLot(User owner, String name, String address,
                      Double latitude, Double longitude,
                      LocalDateTime openedAt, LocalDateTime closedAt,
                       BigDecimal pricePerHour, String description,
                      Integer quantity, ChargeType chargeType,
                      SourceType sourceType) {
        this.owner = owner;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.pricePerHour = pricePerHour;
        this.description = description;
        this.quantity = quantity;
        this.chargeType = chargeType;
        this.sourceType = sourceType;
        this.status = ParkingLotStatus.OPEN;
    }
}
