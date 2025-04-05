package com.parkez.parkinglot.web;

import com.parkez.common.response.Response;
import com.parkez.parkinglot.dto.request.ParkingLotRequest;
import com.parkez.parkinglot.dto.response.ParkingLotResponse;
import com.parkez.parkinglot.service.ParkingLotService;
import com.parkez.user.domain.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Owner용 주차장 관리 API", description = "Owner가 사용하는 가게 관리 API입니다.")
public class ParkingLotOwnerController {

    private final ParkingLotService parkingLotService;

    // 주차장 생성
    @PostMapping("/v1/parking-lots")
    @Operation(summary = "주차장 생성")
    public Response<ParkingLotResponse> createParkingLot(
            @Parameter(hidden = true) User user,
            @Valid @RequestBody ParkingLotRequest request) {
        return Response.of(parkingLotService.createParkingLot(user, request));
    }

}
