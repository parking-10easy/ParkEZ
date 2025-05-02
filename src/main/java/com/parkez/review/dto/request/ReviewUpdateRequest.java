package com.parkez.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewUpdateRequest {

    @NotNull(message = "평점 값 입력은 필수입니다")
    @Schema(description = "평점", example = "3")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 최대 5점까지 입력할 수 있습니다")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "보통입니다.")
    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력할 수 있습니다")
    private String content;

    @Builder
    private ReviewUpdateRequest(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }
}
