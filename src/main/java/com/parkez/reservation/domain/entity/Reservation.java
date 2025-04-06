package com.parkez.reservation.domain.entity;

import com.parkez.common.entity.BaseDeleteEntity;
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
public class Reservation extends BaseDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_zone_id", nullable = false)
    private ParkingZone parkingZone;
    private String parkingLotName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
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
}
