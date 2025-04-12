package com.parkez.parkinglot.client.publicData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ParkingLotData {

    @JsonProperty("주차장명")
    private String name;

    @JsonProperty("주차장도로명주소")
    private String address;

    @JsonProperty("위도")
    private String latitude;

    @JsonProperty("경도")
    private String longitude;

    @JsonProperty("평일운영시작시각")
    private String openedAt;

    @JsonProperty("평일운영종료시각")
    private String closedAt;

    @JsonProperty("주차구획수")
    private String quantity;

    @JsonProperty("요금정보")
    private String chargeType;

}
