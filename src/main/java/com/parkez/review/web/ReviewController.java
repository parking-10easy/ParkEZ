package com.parkez.review.web;

import com.parkez.common.aop.CheckMemberStatus;
import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.review.dto.request.ReviewCreateRequest;
import com.parkez.review.dto.request.ReviewUpdateRequest;
import com.parkez.review.dto.response.ReviewResponse;
import com.parkez.review.enums.ReviewSortType;
import com.parkez.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "10. 리뷰 API")
@CheckMemberStatus
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/v1/reviews")
    @Operation(summary = "리뷰 생성", description = "리뷰 생성 기능입니다.")
    public Response<ReviewResponse> createReview(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody ReviewCreateRequest request) {
        return Response.of(reviewService.createReview(authUser, request));
    }

    @GetMapping("/v1/parking-lots/{parkingLotId}/reviews")
    @Operation(summary = "리뷰 다건 조회", description = "리뷰 다건조회 기능입니다.")
    public Response<ReviewResponse> getReviews(
            @Parameter(description = "주차장 ID (필수)", example = "1") @PathVariable Long parkingLotId,
            @ParameterObject PageRequest pageRequest,
            @Parameter(description = "정렬 타입 (default: LATEST)", example = "LATEST") @RequestParam(required = false) ReviewSortType sortType
            ) {
        return Response.fromPage(reviewService.getReviews(parkingLotId, pageRequest, sortType));
    }

    @GetMapping("/v1/reviews/{reviewId}")
    @Operation(summary = "리뷰 단건 조회", description = "리뷰 단건 조회 기능입니다.")
    public Response<ReviewResponse> getReview(
            @Parameter(description = "리뷰 ID (필수)", example = "1") @PathVariable Long reviewId
    ) {
        return Response.of(reviewService.getReview(reviewId));
    }

    @PutMapping("/v1/reviews/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "리뷰 수정 기능입니다.")
    public Response<Void> updateReview(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Parameter(description = "리뷰 ID (필수)", example = "1") @PathVariable Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        reviewService.updateReview(authUser, reviewId, request);
        return Response.empty();
    }

    @DeleteMapping("/v1/reviews/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "리뷰 삭제 기능입니다.")
    public Response<Void> deleteReview(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Parameter(description = "리뷰 ID (필수)", example = "1") @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(authUser, reviewId);
        return Response.empty();
    }
}
