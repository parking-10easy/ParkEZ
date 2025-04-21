package com.parkez.alarm.web;

import com.parkez.alarm.dto.request.DeviceRegistrationRequest;
import com.parkez.alarm.service.FcmDeviceService;
import com.parkez.common.dto.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "FCM Device 등록 API", description = "FCM Device 등록 API입니다.")
public class FcmDeviceController {

    private final FcmDeviceService fcmDeviceService;

    @PostMapping("/v1/fcm-devices/register")
    @Operation(summary = "FCM Device 등록")
    public Response<Void> registerDevice(@RequestBody DeviceRegistrationRequest request) {
        fcmDeviceService.registerDevice(request.getUserId(), request.getToken());
        return Response.empty();
    }
}
