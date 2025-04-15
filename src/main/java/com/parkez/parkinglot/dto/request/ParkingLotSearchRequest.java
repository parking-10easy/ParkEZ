package com.parkez.parkinglot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "주차장 다건 검색 조건 DTO")
public class ParkingLotSearchRequest {

    @Schema(description = "주차장 이름", example = "한빛")
    private String name;

    @Schema(description = "주차장 주소", example = "강남")
    private String address;

    @Schema(description = "사용자 현재 위도", example = "35.4821524")
    private Double userLatitude;

    @Schema(description = "사용자 현재 경도", example = "129.4083465")
    private Double userLongitude;

    @Schema(description = "검색 범위(미터)", example = "10000")
    private Integer radiusInMeters;

    @Builder
    private ParkingLotSearchRequest(String name, String address, Double userLatitude, Double userLongitude, Integer radiusInMeters) {
        this.name = name;
        this.address = address;
        this.userLatitude = userLatitude;
        this.userLongitude = userLongitude;
        this.radiusInMeters = radiusInMeters;
    }
}
