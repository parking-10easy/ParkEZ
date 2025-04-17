package com.parkez.common.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PageRequest {

    @Positive
    @Parameter(description = "페이지 번호 (default: 1)", example = "1")
    private final int page;

    @Positive
    @Parameter(description = "페이지 크기 (default: 10)", example = "10")
    private final int size;
}