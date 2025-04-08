package com.parkez.parkinglot.web;

import com.parkez.common.response.Response;
import com.parkez.parkinglot.dto.request.ParkingLotImagesRequest;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.request.ParkingLotStatusRequest;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotService;
import com.parkez.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "주차장 관리 API", description = "주차장 관리 및 조회 API입니다.")
public class ParkingLotOwnerController {

    private final ParkingLotService parkingLotService;

    // 주차장 생성
    @PostMapping("/v1/parking-lots")
    @Operation(summary = "주차장 생성")
    public Response<ParkingLotResponse> createParkingLot(
            @Parameter User user,
            @Valid @RequestBody ParkingLotRequest request
    ) {
        return Response.of(parkingLotService.createParkingLot(user, request));
    }

    // 주차장 다건 조회
    @GetMapping("/v1/parking-lots")
    @Operation(summary = "주차장 다건 조회")
    public Response<ParkingLotSearchResponse> searchParkingLots(
            @ModelAttribute ParkingLotSearchRequest request,
            @ParameterObject Pageable pageable
    ) {
        return Response.fromPage(parkingLotService.searchParkingLots(request, pageable));
    }

    // 주차장 단건 조회
    @GetMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 단건 조회")
    public Response<ParkingLotSearchResponse> searchParkingLot(
            @PathVariable Long parkingLotId
    ){
        return Response.of(parkingLotService.searchParkingLot(parkingLotId));
    }

    // 주차장 수정
    @PutMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 수정")
    public Response<Void> updateParkingLot(
            @Parameter(hidden = true) User user,
            @PathVariable Long parkingLotId,
            @RequestBody ParkingLotRequest request
    ) {
        parkingLotService.updateParkingLot(user, parkingLotId, request);
        return Response.empty();
    }

    // 주차장 상태 변경
    @PatchMapping("/v1/parking-lots/{parkingLotId}/status")
    @Operation(summary = "주차장 상태 수정")
    public Response<Void> updatedParkingLotStatus(
            @Parameter(hidden = true) User user,
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingLotStatusRequest statusRequest
    ) {
        parkingLotService.updateParkingLotStatus(user, parkingLotId, statusRequest);
        return Response.empty();
    }

    // 주차장 삭제
    @DeleteMapping("/v1/parking-lots/{parkingLotId}")
    @Operation(summary = "주차장 삭제")
    public Response<Void> deleteParkingLot(
            @Parameter(hidden = true) User user,
            @PathVariable Long parkingLotId
    ) {
        parkingLotService.deleteParkingLot(user, parkingLotId);
        return Response.empty();
    }

    // 주차장 이미지 수정

    @PatchMapping("/v1/parking-lots/{parkingLotId}/images")
    @Operation(summary = "주차장 이미지 수정")
    public Response<Void> updateParkingLotImages(
            @Parameter(hidden = true) User user,
            @PathVariable Long parkingLotId,
            @Valid @RequestBody ParkingLotImagesRequest request
    ) { // 요청 Body로 이미지 URL 리스트를 받음
        parkingLotService.updateParkingLotImages(user, parkingLotId, request);
        return Response.empty();
    }

}
