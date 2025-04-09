package com.parkez.parkingzone.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneErrorCode implements ErrorCode {

    PARKING_ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "PARKING_ZONE_001","해당 주차공간을 찾을 수 없습니다."),
    FORBIDDEN_TO_UPDATE(HttpStatus.FORBIDDEN, "PARKING_ZONE_002","소유자만 수정할 수 있습니다."),
    FORBIDDEN_TO_DELETE(HttpStatus.FORBIDDEN, "PARKING_ZONE_003","소유자만 삭제할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
