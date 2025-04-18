package com.parkez.parkinglot.client.kakaomap.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class KakaoAddressResponse {
    private List<Document> documents;

    @Getter
    public static class Document {
        private Address address;
    }

    @Getter
    public static class Address {

        @JsonProperty("address_name")
        private String addressName;
    }
}
