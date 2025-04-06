package com.parkez.parkingzone.web;

import com.parkez.common.response.Response;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.service.ParkingZoneService;
import io.swagger.v3.oas.annotations.Operation;
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long parkingLotId) {
        return Response.fromPage(parkingZoneService.getParkingZones(page, size, parkingLotId));
    }

    @GetMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 단건 조회", description = "주차공간 단건 조회 기능입니다.")
    public Response<ParkingZoneResponse> getParkingZone(@PathVariable Long parkingZoneId) {
        return Response.of(parkingZoneService.getParkingZone(parkingZoneId));
    }
}
