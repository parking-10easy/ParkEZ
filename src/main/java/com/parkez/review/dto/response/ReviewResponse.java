package com.parkez.review.dto.response;

import com.parkez.review.domain.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Schema(description = "리뷰 응답 DTO")
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private final Long id;

    @Schema(description = "예약 ID", example = "1")
    private final Long reservationId;

    @Schema(description = "별점", example = "5")
    private final int rating;

    @Schema(description = "리뷰 내용", example = "자리도 넓고 쾌적해요.")
    private final String content;

    @Schema(description = "생성일", example = "2025-04-01T10:00:00")
    private final LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2025-04-01T10:00:00")
    private final LocalDateTime modifiedAt;

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getReservationId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getModifiedAt()
        );
    }
}
