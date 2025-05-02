package com.parkez.parkinglot.client.kakaomap.geocode;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Geocode {

    public Double longitude;

    public Double latitude;

    @Builder
    private Geocode(Double longitude, Double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static Geocode from(KakaoGeocodeResponse response) {
        KakaoGeocodeResponse.Document document = response.getDocuments().get(0);
        return Geocode.builder()
                .longitude(Double.valueOf(document.getLongitude()))
                .latitude(Double.valueOf(document.getLatitude()))
                .build();
    }
}
