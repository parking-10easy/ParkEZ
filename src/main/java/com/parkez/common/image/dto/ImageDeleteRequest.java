package com.parkez.common.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageDeleteRequest {

    private String targetType;
    private Long targetId;

    public ImageDeleteRequest(String targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
