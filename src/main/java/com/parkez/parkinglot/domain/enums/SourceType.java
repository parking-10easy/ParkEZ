package com.parkez.parkinglot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SourceType {
    PUBLIC_DATA("공공 데이터"),
    OWNER_REGISTERED("소유주가 등록한 데이터");

    private final String description;
}
