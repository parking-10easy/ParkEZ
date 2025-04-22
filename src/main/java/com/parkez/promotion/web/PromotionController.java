package com.parkez.promotion.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.common.dto.response.Response;
import com.parkez.promotion.dto.request.PromotionCreateRequest;
import com.parkez.promotion.dto.response.PromotionCreateResponse;
import com.parkez.promotion.service.PromotionService;

import io.swagger.v3.oas.annotations.Operation;
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
}
