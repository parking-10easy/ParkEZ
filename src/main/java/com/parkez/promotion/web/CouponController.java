package com.parkez.promotion.web;

import static com.parkez.user.domain.enums.UserRole.*;
import static com.parkez.user.domain.enums.UserRole.Authority.*;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.promotion.dto.request.CouponCreateRequest;
import com.parkez.promotion.dto.response.CouponResponse;
import com.parkez.promotion.service.CouponService;
import com.parkez.user.domain.enums.UserRole;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
@Tag(name = "14. 쿠폰 API", description = "쿠폰 API")
public class CouponController {

	private final CouponService couponService;

	// @Secured(ADMIN)
	@PostMapping("/v1/coupons")
	@Operation(summary = "쿠폰 등록", description = "프로모션 쿠폰을 등록한다")
	public Response<CouponResponse> createCoupon(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,@Valid @RequestBody CouponCreateRequest request) {
		return Response.of(couponService.createCoupon(request));
	}
}
