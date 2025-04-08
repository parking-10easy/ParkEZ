package com.parkez.parkingzone.web;

import com.parkez.common.response.Response;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.service.ParkingZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주차공간 API")
public class ParkingZoneController {

    private final ParkingZoneService parkingZoneService;

    @PostMapping("/v1/parking-zones")
    @Operation(summary = "주차공간 생성", description = "주차공간 생성 기능입니다.")
    public Response<ParkingZoneCreateResponse> createParkingZone(
            @Valid @RequestBody ParkingZoneCreateRequest request) {
        return Response.of(parkingZoneService.createParkingZone(request));
    }

    @GetMapping("/v1/parking-zones")
    @Operation(summary = "주차공간 다건 조회", description = "주차공간 다건 조회 기능입니다.")
    public Response<ParkingZoneResponse> getParkingZones(
            @Parameter(description = "페이지 번호 (default: 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기 (default: 10)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "주차장 ID (필수)", example = "1")
            @RequestParam Long parkingLotId
    ) {
        return Response.fromPage(parkingZoneService.getParkingZones(page, size, parkingLotId));
    }

    @GetMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 단건 조회", description = "주차공간 단건 조회 기능입니다.")
    public Response<ParkingZoneResponse> getParkingZone(@PathVariable Long parkingZoneId) {
        return Response.of(parkingZoneService.getParkingZone(parkingZoneId));
    }

    @PutMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 수정", description = "주차공간 수정 기능입니다.")
    public Response<Void> updateParkingZone(
//            @AuthUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateRequest request) {
        parkingZoneService.updateParkingZone(parkingZoneId, request);
        return Response.empty();
    }

    @PatchMapping("/v1/parking-zones/{parkingZoneId}/status")
    @Operation(summary = "주차공간 상태 변경", description = "주차공간 상태 변경 기능입니다.")
    public Response<Void> updateParkingZoneStatus(
//            @AuthUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateStatusRequest request) {
        parkingZoneService.updateParkingZoneStatus(parkingZoneId, request);
        return Response.empty();
    }

    @PatchMapping("/v1/parking-zones/{parkingZoneId}/image")
    @Operation(summary = "주차공간 이미지 수정", description = "주차공간 이미지 수정 기능입니다.")
    public Response<Void> updateParkingZoneImage(
//            @AuthUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateImageRequest request) {
        parkingZoneService.updateParkingZoneImage(parkingZoneId, request);
        return Response.empty();
    }

    @DeleteMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 삭제", description = "주차공간 삭제 기능입니다.")
    public Response<Void> deleteParkingZone(
//            @AuthUser AuthUser authUser,
            @PathVariable Long parkingZoneId) {
        parkingZoneService.deleteParkingZone(parkingZoneId);
        return Response.empty();
    }
}
