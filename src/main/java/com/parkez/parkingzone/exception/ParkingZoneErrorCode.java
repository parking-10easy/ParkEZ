package com.parkez.parkingzone.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ParkingZoneErrorCode implements ErrorCode {

    // NOT_FOUND
    PARKING_ZONE_NOT_FOUND(HttpStatus.NOT_FOUND, "PARKING_ZONE_001","해당 주차공간을 찾을 수 없습니다."),

    // FORBIDDEN
    FORBIDDEN_TO_ACTION(HttpStatus.FORBIDDEN, "PARKING_ZONE_002","소유자 본인만 변경할 수 있습니다."),

    // BAD_REQUEST
    INVALID_PARKING_ZONE_STATUS(HttpStatus.BAD_REQUEST, "PARKING_ZONE_003", "잘못된 주차공간 상태입니다."),
    PUBLIC_DATA_CREATION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PARKING_ZONE_004", "공공 데이터는 주차공간 생성이 불가합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;
}
