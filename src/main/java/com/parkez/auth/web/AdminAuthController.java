package com.parkez.auth.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.auth.dto.request.SigninRequest;
import com.parkez.auth.dto.request.SignupUserRequest;
import com.parkez.auth.dto.response.SignupResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.service.AuthService;
import com.parkez.common.dto.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "17. 관리자 인증 API", description = "관리자의 로그인을 처리합니다.")
public class AdminAuthController {

	private final AuthService authService;


	@Operation(summary = "관리자 로그인", description = "관리자의 로그인을 처리합니다.")
	@PostMapping("/v1/auth/signin/admin")
	public Response<TokenResponse> signin(@Valid @RequestBody SigninRequest request) {
		return Response.of(authService.signinAdmin(request.getEmail(), request.getPassword()));
	}
}
