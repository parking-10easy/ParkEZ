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
import com.parkez.common.response.Response;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "일반 사용자 인증 API", description = "일반 사용자의 회원가입 및 로그인을 처리합니다.")
public class UserAuthController {

	private final AuthService authService;

	@Operation(summary = "일반 사용자 회원가입", description = "일반 사용자의 회원가입을 처리합니다.")
	@PostMapping("/v1/auth/signup/user")
	public Response<SignupResponse> signup(@Valid @RequestBody SignupUserRequest request) {
		return Response.of(authService.signupUser(request));
	}

	@Operation(summary = "일반 사용자 로그인", description = "일반 사용자의 로그인을 처리합니다.")
	@PostMapping("/v1/auth/signin/user")
	public Response<TokenResponse> signin(@Valid @RequestBody SigninRequest request) {
		return Response.of(authService.signinUser(request.getEmail(), request.getPassword()));
	}
}
