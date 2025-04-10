package com.parkez.common;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


// 예인님 코드 머지 후 삭제 예정
@Getter
@RequiredArgsConstructor
public class PageRequest {

    @Parameter(description = "페이지 번호 (default: 1)", example = "1")
    private final int page;

    @Parameter(description = "페이지 크기 (default: 10)", example = "10")
    private final int size;
}