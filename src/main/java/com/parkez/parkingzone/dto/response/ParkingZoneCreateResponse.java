package com.parkez.parkingzone.dto.response;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Schema(description = "주차 공간 생성 응답")
public class ParkingZoneCreateResponse {

    @Schema(description = "주차 공간 ID", example = "1")
    private final Long id;

    @Schema(description = "주차장 ID", example = "1")
    private final Long parkingLotId;

    @Schema(description = "주차 공간 이름", example = "A구역")
    private final String name;

    @Schema(description = "이미지 URL", example = "http://example.com/image.jpg")
    private final String imageUrl;

    @Schema(description = "주차 공간 상태", example = "AVAILABLE")
    private final ParkingZoneStatus status;

    public static ParkingZoneCreateResponse from(ParkingZone parkingZone) {
        return new ParkingZoneCreateResponse(
                parkingZone.getId(),
                parkingZone.getParkingLotId(),
                parkingZone.getName(),
                parkingZone.getImageUrl(),
                parkingZone.getStatus()
        );
    }
}
