package com.parkez.common.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ImageUploadRequest {

    private String targetType;
    private Long targetId;
}
