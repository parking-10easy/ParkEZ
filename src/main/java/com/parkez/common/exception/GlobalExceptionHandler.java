package com.parkez.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getFieldErrors().stream().
                findFirst().
                map(fieldError -> fieldError.getDefaultMessage()).
                orElseThrow(() -> new IllegalStateException("검증 에러가 반드시 존재해야 합니다."));
        return ResponseEntity.badRequest().body(ErrorResponse.of("ARGUMENT_NOT_VALID", errorMessage));
    }

    @ExceptionHandler(ParkingEasyException.class)
    public ResponseEntity<ErrorResponse> handleParkingEasyException(ParkingEasyException e) {
        log.info("ParkingEasyException : {}", e.getMessage(), e);
        return new ResponseEntity<>(ErrorResponse.of(e.getErrorCode()), e.getStatus());
    }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorResponse handleGlobalException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);
        return ErrorResponse.of("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
    }
}