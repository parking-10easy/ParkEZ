package com.parkez.promotion.web;

import static com.parkez.user.domain.enums.UserRole.Authority.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.promotion.dto.request.MyCouponSearchCondition;
import com.parkez.promotion.dto.response.MyCouponResponse;
import com.parkez.promotion.service.PromotionIssueService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "16. 프로모션 발급 API", description = "프로모션 발급 API")
public class PromotionIssueController {

	private final PromotionIssueService promotionIssueService;

	@Secured(USER)
	@GetMapping("/v1/promotion-issues/me")
	@Operation(summary = "내 발급 쿠폰 조회", description = "로그인한 사용자가 발급받은 쿠폰 목록을 조회합니다.")
	public Response<MyCouponResponse> getMyIssuedCoupons(@ParameterObject MyCouponSearchCondition condition,@ParameterObject @Valid  PageRequest pageRequest, @AuthenticatedUser @Parameter(hidden = true) AuthUser authUser) {
		return Response.fromPage(promotionIssueService.getMyIssuedCoupons(authUser.getId(), condition, pageRequest.getPage(),pageRequest.getSize()));
	}
}
