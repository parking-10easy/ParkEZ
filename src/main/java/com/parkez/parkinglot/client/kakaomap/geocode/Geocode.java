package com.parkez.parkinglot.client.kakaomap.geocode;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Geocode {

    public Double longitude;

    public Double latitude;

    @Builder
    Geocode(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

}
