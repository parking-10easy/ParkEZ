package com.parkez.parkinglot.web;

import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.response.Response;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.MyParkingLotSearchResponse;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotService;
import com.parkez.user.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주차장 관리 API", description = "주차장 관리 및 조회 API입니다.")
public class ParkingLotController {

    private final ParkingLotService parkingLotService;

    // 주차장 생성
    @PostMapping("/v1/parking-lots")
    @Operation(summary = "주차장 생성")
    @Secured(UserRole.Authority.OWNER)
    public Response<ParkingLotResponse> createParkingLot(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @Valid @RequestBody ParkingLotRequest request
    ) {
        return Response.of(parkingLotService.createParkingLot(authUser, request));
    }

    // 주차장 다건 조회
    @GetMapping("/v1/parking-lots")
    @Operation(summary = "주차장 다건 조회")
    public Response<ParkingLotSearchResponse> searchParkingLots(
            @ModelAttribute ParkingLotSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return Response.fromPage(parkingLotService.searchParkingLotsByConditions(request, pageable));
    }

    // 주차장 단건 조회
    @GetMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 단건 조회")
    public Response<ParkingLotSearchResponse> searchParkingLot(
            @PathVariable Long parkingLotId
    ) {
        return Response.of(parkingLotService.searchParkingLotById(parkingLotId));
    }

    // 본인이 소유한 주차장 조회
    @GetMapping("/v1/parking-lots/me")
    @Operation(summary = "본인이 소유한 주차장 리스트 조회")
    @Secured(UserRole.Authority.OWNER)
    public Response<MyParkingLotSearchResponse> searchParkingLot(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @ParameterObject Pageable pageable
    ) {
        return Response.fromPage(parkingLotService.getMyParkingLots(authUser, pageable));
    }

    // 주차장 수정
    @PutMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 수정")
    @Secured(UserRole.Authority.OWNER)
    public Response<Void> updateParkingLot(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingLotRequest request
    ) {
        parkingLotService.updateParkingLot(authUser, parkingLotId, request);
        return Response.empty();
    }

    // 주차장 상태 변경
    @PutMapping("/v1/parking-lots/{parkingLotId}/status")
    @Operation(summary = "주차장 상태 수정")
    @Secured(UserRole.Authority.OWNER)
    public Response<Void> updatedParkingLotStatus(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingLotStatusRequest statusRequest
    ) {
        parkingLotService.updateParkingLotStatus(authUser, parkingLotId, statusRequest);
        return Response.empty();
    }

    // 주차장 삭제
    @DeleteMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 삭제")
    @Secured(UserRole.Authority.OWNER)
    public Response<Void> deleteParkingLot(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long parkingLotId
    ) {
        parkingLotService.deleteParkingLot(authUser, parkingLotId);
        return Response.empty();
    }

    // 주차장 이미지 수정

    @PutMapping("/v1/parking-lots/{parkingLotId}/images")
    @Operation(summary = "주차장 이미지 수정")
    @Secured(UserRole.Authority.OWNER)
    public Response<Void> updateParkingLotImages(
            @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingLotImagesRequest request
    ) { // 요청 Body로 이미지 URL 리스트를 받음
        parkingLotService.updateParkingLotImages(authUser, parkingLotId, request);
        return Response.empty();
    }

}
