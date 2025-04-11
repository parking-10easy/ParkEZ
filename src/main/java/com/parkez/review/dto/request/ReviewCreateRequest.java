package com.parkez.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "리뷰 생성 요청 DTO")
public class ReviewCreateRequest {

    @NotNull(message = "예약 아이디 값 입력은 필수입니다")
    @Schema(description = "예약 아이디", example = "1")
    private Long reservationId;

    @NotNull(message = "평점 값 입력은 필수입니다")
    @Schema(description = "평점", example = "5")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 최대 5점까지 입력할 수 있습니다")
    private Integer rating;

    @Schema(description = "리뷰 내용", example = "자리도 넓고 쾌적해요.")
    @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력할 수 있습니다")
    private String content;

    @Builder
    public ReviewCreateRequest(Long reservationId, Integer rating, String content) {
        this.reservationId = reservationId;
        this.rating = rating;
        this.content = content;
    }
}
