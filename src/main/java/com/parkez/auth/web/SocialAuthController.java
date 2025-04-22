package com.parkez.auth.web;

import static com.parkez.user.domain.enums.UserRole.Authority.*;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.auth.dto.request.SocialLoginCallbackRequest;
import com.parkez.auth.dto.request.SocialOwnerProfileCompleteRequest;
import com.parkez.auth.dto.request.SocialUserProfileCompleteRequest;
import com.parkez.auth.dto.response.SocialSignupCompleteResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.auth.service.SocialAuthService;
import com.parkez.common.dto.response.Response;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "03. 소셜 로그인 API", description = " 소셜 로그인을 처리합니다.")
public class SocialAuthController {

	private final SocialAuthService socialAuthService;

	@Secured(USER)
	@Operation(summary = "소셜 일반 사용자 추가정보 입력", description = "소셜 로그인 후 추가정보를 입력하여 일반 사용자 회원가입을 완료합니다.")
	@PostMapping("/v1/auth/signup/social/user/complete")
	public Response<SocialSignupCompleteResponse> completeUserSignup(
		@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
		@Valid @RequestBody SocialUserProfileCompleteRequest request) {
		return Response.of(socialAuthService.completeUserSignup(authUser, request));
	}

	@Secured(OWNER)
	@Operation(summary = "소셜 오너 추가정보 입력", description = "소셜 로그인 후 추가정보를 입력하여 오너 회원가입을 완료합니다.")
	@PostMapping("/v1/auth/signup/social/owner/complete")
	public Response<SocialSignupCompleteResponse> completeOwnerSignup(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
		@Valid @RequestBody SocialOwnerProfileCompleteRequest request) {
		return Response.of(socialAuthService.completeOwnerSignup(authUser, request));
	}

	@Hidden
	@Operation(summary = "소셜 로그인 콜백", description = "소셜 로그인 후 인가 코드를 받아 토큰을 발급받습니다.")
	@GetMapping("/v1/auth/{provider}/callback")
	public Response<TokenResponse> socialLoginUser(
		@PathVariable("provider") OAuthProvider provider,
		@Valid @ParameterObject SocialLoginCallbackRequest request) {
		return Response.of(socialAuthService.login(provider, request.getCode(), request.getState()));
	}

}