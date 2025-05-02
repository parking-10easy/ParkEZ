package com.parkez.common.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

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

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        // log.info("AuthorizationDeniedException : {}", e.getMessage(), e);
        CommonErrorCode accessDenied = CommonErrorCode.ACCESS_DENIED;
        return new ResponseEntity<>(ErrorResponse.of(accessDenied),accessDenied.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception e) {
        log.error("Exception : {}", e.getMessage(), e);
        CommonErrorCode internalServerError = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(ErrorResponse.of(internalServerError), internalServerError.getHttpStatus());
    }
}