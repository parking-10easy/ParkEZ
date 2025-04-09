package com.parkez.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "이미지 업로드/수정 후 응답 DTO")
public class ImageUrlResponse {

    @Schema(description = "업로드 된 이미지 URL", example = "")
    private List<String> urls;


    @Builder
    private ImageUrlResponse(List<String> urls) {
        this.urls = urls;
    }

    public static ImageUrlResponse of(List<String> urls) {
        return ImageUrlResponse.builder()
                .urls(urls)
                .build();
    }
}
