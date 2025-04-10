package com.parkez.parkingzone.web;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.parkingzone.dto.request.ParkingZoneCreateRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateImageRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateNameRequest;
import com.parkez.parkingzone.dto.request.ParkingZoneUpdateStatusRequest;
import com.parkez.parkingzone.dto.response.ParkingZoneResponse;
import com.parkez.parkingzone.service.ParkingZoneService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Secured(UserRole.Authority.OWNER)
@Tag(name = "주차공간 API")
public class ParkingZoneController {

    private final ParkingZoneService parkingZoneService;

    @PostMapping("/v1/parking-zones")
    @Operation(summary = "주차공간 생성", description = "주차공간 생성 기능입니다.")
    public Response<ParkingZoneResponse> createParkingZone(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody ParkingZoneCreateRequest request) {
        return Response.of(parkingZoneService.createParkingZone(authUser, request));
    }

    @Secured({UserRole.Authority.OWNER, UserRole.Authority.USER})
    @GetMapping("/v1/parking-lot/{parkingLotId}/parking-zones")
    @Operation(summary = "주차공간 다건 조회", description = "주차공간 다건 조회 기능입니다.")
    public Response<ParkingZoneResponse> getParkingZones(
            @ParameterObject PageRequest pageRequest,
            @Parameter(description = "주차장 ID (필수)", example = "1")
            @PathVariable Long parkingLotId
    ) {
        return Response.fromPage(parkingZoneService.getParkingZones(pageRequest, parkingLotId));
    }

    @Secured({UserRole.Authority.OWNER, UserRole.Authority.USER})
    @GetMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 단건 조회", description = "주차공간 단건 조회 기능입니다.")
    public Response<ParkingZoneResponse> getParkingZone(@PathVariable Long parkingZoneId) {
        return Response.of(parkingZoneService.getParkingZone(parkingZoneId));
    }

    @PutMapping("/v1/parking-zones/{parkingZoneId}/name")
    @Operation(summary = "주차공간 이름 수정", description = "주차공간 이름 수정 기능입니다.")
    public Response<Void> updateParkingZoneName(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateNameRequest request) {
        parkingZoneService.updateParkingZoneName(authUser, parkingZoneId, request);
        return Response.empty();
    }

    @PutMapping("/v1/parking-zones/{parkingZoneId}/status")
    @Operation(summary = "주차공간 상태 변경", description = "주차공간 상태 변경 기능입니다.")
    public Response<Void> updateParkingZoneStatus(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateStatusRequest request) {
        parkingZoneService.updateParkingZoneStatus(authUser, parkingZoneId, request);
        return Response.empty();
    }

    @PutMapping("/v1/parking-zones/{parkingZoneId}/image")
    @Operation(summary = "주차공간 이미지 수정", description = "주차공간 이미지 수정 기능입니다.")
    public Response<Void> updateParkingZoneImage(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId,
            @Valid @RequestBody ParkingZoneUpdateImageRequest request) {
        parkingZoneService.updateParkingZoneImage(authUser, parkingZoneId, request);
        return Response.empty();
    }

    @DeleteMapping("/v1/parking-zones/{parkingZoneId}")
    @Operation(summary = "주차공간 삭제", description = "주차공간 삭제 기능입니다.")
    public Response<Void> deleteParkingZone(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @PathVariable Long parkingZoneId) {
        LocalDateTime deletedAt = LocalDateTime.now();
        parkingZoneService.deleteParkingZone(authUser, parkingZoneId, deletedAt);
        return Response.empty();
    }
}
