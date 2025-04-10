package com.parkez.common.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PageRequest {

    @Parameter(description = "페이지 번호 (default: 1)", example = "1")
    private final int page;

    @Parameter(description = "페이지 크기 (default: 10)", example = "10")
    private final int size;
}
