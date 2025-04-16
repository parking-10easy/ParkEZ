package com.parkez.parkinglot.dto.aggregation;

import lombok.Getter;

@Getter
public class ParkingLotAggregation {

    private final Long parkingLotId;
    private final Long parkingZoneCount;
    private final Long reviewCount;
    private final Double avgRating;

    public ParkingLotAggregation(Long parkingLotId, Long parkingZoneCount, Long reviewCount, Double avgRating) {
        this.parkingLotId = parkingLotId;
        this.parkingZoneCount = parkingZoneCount;
        this.reviewCount = reviewCount;
        this.avgRating = avgRating;
    }
}
