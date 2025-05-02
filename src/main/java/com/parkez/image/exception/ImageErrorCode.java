package com.parkez.image.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    // 404 NOT FOUND
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_001", "해당 폴더에 이미지가 존재하지 않습니다."),

    // 500 internet server error
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_002", "서버 오류로 인해 이미지 업로드가 실패되었습니다."),

    // 400 bad request
    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_003", "유효하지 않는 타입입니다."),
    IMAGE_IS_NULL(HttpStatus.BAD_REQUEST, "IMAGE_004", "이미지 파일이 비어있습니다."),
    INVALID_EXTENSION_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_005", "허용되지 않는 파일 확장자입니다."),
    FILENAME_IS_NULL(HttpStatus.BAD_REQUEST, "IMAGE_006", "파일이름이 존재하지 않습니다."),
    EXCEED_IMAGE_UPLOAD_LIMIT(HttpStatus.BAD_REQUEST, "IMAGE_007", "이미지는 최대 5개까지 업로드 가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;


}
