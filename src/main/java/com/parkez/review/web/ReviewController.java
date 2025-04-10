package com.parkez.review.web;

import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.response.Response;
import com.parkez.review.dto.request.ReviewCreateRequest;
import com.parkez.review.dto.response.ReviewResponse;
import com.parkez.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/v1/reviews")
    @Operation(summary = "리뷰 생성", description = "리뷰 생성 기능입니다.")
    public Response<ReviewResponse> createReview(
            @Parameter(hidden = true) @AuthenticatedUser AuthUser authUser,
            @Valid @RequestBody ReviewCreateRequest request) {
        return Response.of(reviewService.createReview(authUser, request));
    }

}
