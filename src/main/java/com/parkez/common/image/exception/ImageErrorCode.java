package com.parkez.common.image.exception;

import com.parkez.common.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ImageErrorCode implements ErrorCode {

    IMAGE_NOT_FOUND("IMAGE_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 폴더에 이미지가 존재하지 않습니다."),
    IMAGE_UPLOAD_FAIL("IMAGE_UPLOAD_FAIL", HttpStatus.INTERNAL_SERVER_ERROR , "서버 오류로 인해 이미지 업로드가 실패되었습니다."),
    INVALID_IMAGE_TYPE("INVALID_IMAGE_TYPE",HttpStatus.BAD_REQUEST , "유효하지 않는 이미지 타입입니다."),
    IMAGE_IS_NULL("IMAGE_IS_NULL", HttpStatus.BAD_REQUEST, "이미지 파일이 비어있습니다."),
    INVALID_EXTENSION_TYPE("INVALID_EXTENSION_TYPE", HttpStatus.BAD_REQUEST, "허용되지 않는 파일 확장자입니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;


}
