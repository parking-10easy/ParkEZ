package com.parkez.image.dto.request;

import com.parkez.image.enums.ImageTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "이미지 업로드/수정 시 요청 DTO")
public class ImageRequest {

    @NotNull
    @Schema(description = "이미지 업로드할 도메인 입력", example = "USER_PROFILE")
    private ImageTargetType targetType;

    @NotNull
    @Schema(description = "이미지 업로드할 도메인의 개별 Id 입력", example = "1")
    private Long targetId;


    @Builder
    private ImageRequest(ImageTargetType targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
