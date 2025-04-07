package com.parkez.parkingzone.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springdoc.core.annotations.ParameterObject;

@Getter
@ParameterObject
public class PageRequest {

    @Schema(description = "페이지 번호 (기본값: 1)", example = "1")
    private int page = 1;

    @Schema(description = "페이지 크기 (기본값: 10)", example = "10")
    private int size = 10;
}
