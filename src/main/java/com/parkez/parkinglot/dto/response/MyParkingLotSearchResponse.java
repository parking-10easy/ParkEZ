package com.parkez.parkinglot.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "본인이 소유한 주차장 조회 응답 DTO")
public class MyParkingLotSearchResponse {

    @Schema(description = "주차장 ID", example = "1")
    private Long parkingLotId;

    @Schema(description = "주차장 이름", example = "한빛 주차장")
    private String name;

    @Schema(description = "주차장 주소", example = "서울시 강남구 테헤란로 131")
    private String address;

    @Schema(description = "리뷰 수", example = "10")
    private Long reviewCount;

    @Schema(description = "썸네일 이미지", example = "parking-lot-default.jpg")
    private String thumbnailImage;

    @Builder
    private MyParkingLotSearchResponse(Long parkingLotId, String name, String address,
                                       Long reviewCount, String thumbnailImage) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.address = address;
        this.thumbnailImage = thumbnailImage;
        this.reviewCount = reviewCount;
    }

    // 소유한 주차장 조회용 생성자
    public MyParkingLotSearchResponse(Long parkingLotId, String name, String address) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.address = address;
    }

    // 이미지
    public void updateImage(String thumbnailImage) {
        this.thumbnailImage = thumbnailImage;
    }

    // 집계 관련
    public void updateAggregation(Long reviewCount) {
        this.reviewCount = reviewCount;
    }
}
