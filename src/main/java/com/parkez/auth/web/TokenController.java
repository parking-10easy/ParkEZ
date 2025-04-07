package com.parkez.auth.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.auth.dto.request.RefreshTokenRequest;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.service.AuthService;
import com.parkez.common.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Token API", description = "인증 토큰 재발급 API")
public class TokenController {

	private final AuthService authService;

	@Operation(summary = "Access Token 재발급", description = "만료된 Access Token을 Refresh Token을 이용해 재발급합니다.")
	@PostMapping("/v1/auth/token/reissue")
	public Response<TokenResponse> reissue(@Valid @RequestBody RefreshTokenRequest request) {

		return Response.of(authService.reissueToken(request.getRefreshToken()));
	}


}
