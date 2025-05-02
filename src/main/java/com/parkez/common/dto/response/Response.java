package com.parkez.common.dto.response;

import org.springframework.data.domain.Page;

public interface Response<T> {

    T getData();

    static <T> Response<T> of(T data) {
        return new DefaultResponse<>(data);
    }

    static <T> Response<T> empty() {
        return new DefaultResponse<>(null);
    }

    static <T> Response<T> fromPage(Page<T> pageData) {
        return new PageResponse<>(
                pageData.getContent(),
                pageData.getPageable().getPageNumber(),
                pageData.getPageable().getPageSize(),
                pageData.getTotalPages(),
                pageData.getTotalElements()
        );
    }
}
