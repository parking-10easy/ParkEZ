package com.parkez.common.image.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE_001", "해당 폴더에 이미지가 존재하지 않습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE_002", "서버 오류로 인해 이미지 업로드가 실패되었습니다."),
    INVALID_TARGET_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_003", "유효하지 않는 타입입니다."),
    IMAGE_IS_NULL(HttpStatus.BAD_REQUEST, "IMAGE_004", "이미지 파일이 비어있습니다."),
    INVALID_EXTENSION_TYPE(HttpStatus.BAD_REQUEST, "IMAGE_005", "허용되지 않는 파일 확장자입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;


}
