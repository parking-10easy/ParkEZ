package com.parkez.parkinglot.web;

import com.parkez.common.response.Response;
import com.parkez.parkinglot.dto.request.ParkingLotSearchRequest;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User용 주차장 조회 API", description = "User가 사용하는 가게 조회 API입니다.")
public class ParkingLotUserController {

    private final ParkingLotService parkingLotService;

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

}
