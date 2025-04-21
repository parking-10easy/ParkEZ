package com.parkez.alarm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "FCM Device 등록 요청 DTO")
public class DeviceRegistrationRequest {

    @Schema(description = "유저 아이디", example = "2")
    private Long userId;

    @Schema(description = "디바이스 토큰", example = "token")
    private String token;
}
