package com.parkez.parkinglot.web;

import com.parkez.common.response.Response;
import com.parkez.parkinglot.dto.response.ParkingLotSearchResponse;
import com.parkez.parkinglot.service.ParkingLotService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "User용 주차장 조회 API", description = "Userr가 사용하는 가게 조회 API입니다.")
public class ParkingLotUserController {

    private final ParkingLotService parkingLotService;

    // 주차장 다건 조회
    @GetMapping("/v1/parking-lots")
    public Response<ParkingLotSearchResponse> searchParkingLots(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            Pageable pageable
    ) {
        return Response.fromPage(parkingLotService.searchParkingLots(name, address, pageable));
    }

}
