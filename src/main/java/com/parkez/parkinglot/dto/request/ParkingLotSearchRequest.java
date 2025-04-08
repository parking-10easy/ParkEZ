package com.parkez.parkinglot.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주차장 다건 검색 조건 DTO")
public class ParkingLotSearchRequest {

    @Schema(description = "주차장 이름", example = "한빛")
    private String name;

    @Schema(description = "주차장 이름", example = "강남")
    private String address;
}
