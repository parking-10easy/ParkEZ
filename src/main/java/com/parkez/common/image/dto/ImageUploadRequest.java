package com.parkez.common.image.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@NoArgsConstructor
public class ImageUploadRequest {

    @NotBlank(message = "targetType은 필수 입력값입니다.")
    @Schema(description = "이미지 업로드할 도메인 입력", example = "UserProfile")
    private String targetType;

    @NotBlank(message = "targetId는 필수 입력값입니다.")
    @Schema(description = "이미지 업로드할 도메인의 개별 Id 입력", example = "1")
    private Long targetId;


    @Builder
    public ImageUploadRequest(String targetType, Long targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }
}
