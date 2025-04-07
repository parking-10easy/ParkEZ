package com.parkez.parkinglot.domain.entity;

import com.parkez.common.entity.BaseDeleteEntity;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "parking_lot")
public class ParkingLot extends BaseDeleteEntity {

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
    private LocalTime openedAt;

    @Column(nullable = false)
    private LocalTime closedAt;

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

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FakeImage> images = new ArrayList<>();

    @Builder
    private ParkingLot(User owner, String name, String address,
                      Double latitude, Double longitude,
                       LocalTime openedAt, LocalTime closedAt,
                       BigDecimal pricePerHour, String description,
                      Integer quantity, ChargeType chargeType,
                      SourceType sourceType
    ) {
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

    public void update(ParkingLotRequest request){
        this.name = request.getName();
        this.address = request.getAddress();
        this.openedAt = request.getOpenedAt();
        this.closedAt = request.getClosedAt();
        this.pricePerHour = request.getPricePerHour();
        this.description = request.getDescription();
        this.quantity = request.getQuantity();
    }

    public void updateStatus(ParkingLotStatusRequest request){
        this.status = request.getStatus();
    }

    public void updateImages(List<FakeImage> newImages) {
        this.images.clear();
        if (newImages != null) {
            newImages.forEach(image -> {
                image.updateParkingLot(this);
                this.images.add(image);
            });
        }
    }
}
