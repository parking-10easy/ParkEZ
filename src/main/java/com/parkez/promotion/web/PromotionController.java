package com.parkez.promotion.web;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.promotion.dto.response.PromotionDetailResponse;
import com.parkez.promotion.dto.request.PromotionCreateRequest;
import com.parkez.promotion.dto.response.ActivePromotionResponse;
import com.parkez.promotion.dto.response.PromotionCreateResponse;
import com.parkez.promotion.dto.response.PromotionIssueResponse;
import com.parkez.promotion.service.PromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Tag(name = "15. 프로모션 API", description = "프로모션 API")
public class PromotionController {

	private final PromotionService promotionService;

	// @Secured(ADMIN)
	@PostMapping("/v1/promotions")
	@Operation(summary = "프로모션 등록", description = "프로모션을 등록한다")
	public Response<PromotionCreateResponse> createPromotion(
		@Valid @RequestBody PromotionCreateRequest request) {

		return Response.of(promotionService.createPromotion(request));
	}

	@GetMapping("/v1/promotions")
	@Operation(summary = "진행중인 프로모션 목록 조회", description = "진행중인 프로모션을 조회합니다.")
	public Response<ActivePromotionResponse> getActivePromotions(@Valid @ParameterObject PageRequest pageRequest) {
		return Response.fromPage(promotionService.getActivePromotions(pageRequest.getPage(), pageRequest.getSize()));
	}

	@GetMapping("/v1/promotions/{promotionId}")
	@Operation(summary = "진행중인 프로모션 상세 조회", description = "진행중인 프로모션의 상세 정보를 조회합니다.")
	public Response<PromotionDetailResponse> getActivePromotion(@AuthenticatedUser @Parameter(hidden = true)  AuthUser authUser, @PathVariable Long promotionId) {
		return Response.of(promotionService.getActivePromotion(authUser.getId(), promotionId));
	}

	@PostMapping("/v1/promotions/{promotionId}/issue")
	@Operation(summary = "프로모션 쿠폰 발급", description = "지정된 프로모션에 대해 쿠폰을 발급받습니다.")
	public Response<PromotionIssueResponse> issuePromotionCoupon(@PathVariable Long promotionId, @AuthenticatedUser @Parameter(hidden = true)  AuthUser authUser) {
		return Response.of(promotionService.issueCoupon(authUser, promotionId));
	}
}
