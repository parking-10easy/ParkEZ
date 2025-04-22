package com.parkez.parkinglot.domain.entity;

import com.parkez.common.entity.BaseDeleteEntity;
import com.parkez.parkinglot.domain.enums.ChargeType;
import com.parkez.parkinglot.domain.enums.ParkingLotStatus;
import com.parkez.parkinglot.domain.enums.SourceType;
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
import java.util.Objects;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(
        name = "parking_lot",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_latitude_longitude", columnNames = {"longitude", "latitude"})
        }
)
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
    @Column(nullable = false)
    private SourceType sourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParkingLotStatus status;

    @OneToMany(mappedBy = "parkingLot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParkingLotImage> images;

    @Builder
    private ParkingLot(User owner, String name, String address,
                       Double latitude, Double longitude,
                       LocalTime openedAt, LocalTime closedAt,
                       BigDecimal pricePerHour, String description,
                       Integer quantity, ChargeType chargeType,
                       SourceType sourceType, List<ParkingLotImage> images
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
        this.images = images;
    }

    public void update(String name, String address,
                       Double latitude, Double longitude,
                       LocalTime openedAt, LocalTime closedAt, BigDecimal pricePerHour,
                       String description, Integer quantity) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.pricePerHour = pricePerHour;
        this.description = description;
        this.quantity = quantity;
    }

    public void updateStatus(ParkingLotStatus newStatus) {
        this.status = newStatus;
    }

    public void addImage(ParkingLotImage image) {
        images = new ArrayList<>();
        image.updateParkingLot(this);
        images.add(image);
    }

    public void updateImages(List<ParkingLotImage> newImages) {
        this.images.clear();
        this.images.addAll(newImages);
    }

    public boolean isOwned(Long userId) {
        return Objects.equals(this.owner.getId(), userId);
    }

    public void updateGeocode(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isPublicData() {
        return Objects.equals(this.sourceType, SourceType.PUBLIC_DATA);
    }
}