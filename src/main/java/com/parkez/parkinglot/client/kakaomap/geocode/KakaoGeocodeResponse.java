package com.parkez.parkinglot.client.kakaomap.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class KakaoGeocodeResponse {

    private List<Document> documents;

    @Data
    public static class Document {

        private String address_name;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;

    }
}
