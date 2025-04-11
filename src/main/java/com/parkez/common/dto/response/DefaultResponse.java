package com.parkez.common.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultResponse<T> implements Response<T> {

    private final T data;

    @Override
    public T getData() {
        return data;
    }
}
