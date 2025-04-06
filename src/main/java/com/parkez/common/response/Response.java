package com.parkez.common.response;

import org.springframework.data.domain.Page;

import java.util.List;

public interface Response<T> {

    T getData();

    static <T> Response<T> of(T data) {
        return new DefaultResponse<>(data);
    }

    static <T> Response<T> empty() {
        return new DefaultResponse<>(null);
    }

    static <T> Response<List<T>> fromPage(Page<T> pageData) {
        return new PageResponse<>(
                pageData.getContent(),
                pageData.getPageable().getPageNumber(),
                pageData.getPageable().getPageSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements()
        );
    }
}
