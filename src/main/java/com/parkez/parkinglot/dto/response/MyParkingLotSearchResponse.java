package com.parkez.parkinglot.dto.response;

import com.parkez.parkinglot.domain.entity.ParkingLot;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "본인이 소유한 주차장 조회 응답 DTO")
public class MyParkingLotSearchResponse {

    @Schema(description = "주차장 ID", example = "1")
    private final Long parkingLotId;

    @Schema(description = "주차장 이름", example = "한빛 주차장")
    private final String name;

    @Schema(description = "주차장 주소", example = "서울시 강남구 테헤란로 1111")
    private final String address;

    @Schema(description = "리뷰 수", example = "10")
    private final Integer reviewCount;

    @Schema(description = "썸네일 이미지", example = "https://example.com/image1.jpg")
    private final String thumbnailImage;

    @Builder
    private MyParkingLotSearchResponse(Long parkingLotId, String name, String address,
                                    Integer reviewCount, String images) {
        this.parkingLotId = parkingLotId;
        this.name = name;
        this.address = address;
        this.reviewCount = reviewCount;
        this.thumbnailImage = images;
    }

    public static MyParkingLotSearchResponse from(ParkingLot parkingLot) {
        return MyParkingLotSearchResponse.builder()
                .parkingLotId(parkingLot.getId())
                .name(parkingLot.getName())
                .address(parkingLot.getAddress())
                //.reviewCount(reviewCount) // 수정 필요
                //.thumbnailImage(images) // 수정 필요
                .build();
    }
}
