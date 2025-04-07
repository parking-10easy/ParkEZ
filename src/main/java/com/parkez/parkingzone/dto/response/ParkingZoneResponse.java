package com.parkez.parkingzone.dto.response;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "주차 구역 응답 DTO")
public class ParkingZoneResponse {

    @Schema(description = "주차 구역 ID", example = "1")
    private final Long id;

    @Schema(description = "주차장 ID", example = "1")
    private final Long parkingLotId;

    @Schema(description = "주차 구역 이름", example = "A구역")
    private final String name;

    @Schema(description = "주차 구역 이미지 URL", example = "http://example.com/image.jpg")
    private final String imageUrl;

    @Schema(description = "주차 구역 상태", example = "AVAILABLE")
    private final ParkingZoneStatus status;

    public static ParkingZoneResponse from(ParkingZone parkingZone) {
        return new ParkingZoneResponse(
                parkingZone.getId(),
                parkingZone.getParkingLot().getId(),
                parkingZone.getName(),
                parkingZone.getImageUrl(),
                parkingZone.getStatus()
        );
    }
}
