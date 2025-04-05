package com.parkez.parkingzone.web;

import com.parkez.common.response.Response;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneCreateResponse;
import com.parkez.parkingzone.service.ParkingZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
