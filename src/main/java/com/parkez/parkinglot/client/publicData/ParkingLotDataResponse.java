package com.parkez.parkinglot.client.publicData;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class ParkingLotDataResponse {

    @JsonProperty("page")
    private int page;

    @JsonProperty("perPage")
    private int perPage;

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("currentCount")
    private int currentCount;

    @JsonProperty("matchCount")
    private int matchCount;

    @JsonProperty("data")
    private List<ParkingLotData> data;

    public ParkingLotDataResponse() {
    }

    @Builder
    private ParkingLotDataResponse(int page, int perPage, int totalCount, int currentCount, int matchCount, List<ParkingLotData> data) {
        this.page = page;
        this.perPage = perPage;
        this.totalCount = totalCount;
        this.currentCount = currentCount;
        this.matchCount = matchCount;
        this.data = data;
    }
}
