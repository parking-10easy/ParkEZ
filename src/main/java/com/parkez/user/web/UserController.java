package com.parkez.user.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.auth.authentication.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.response.Response;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "유저 API", description = "유저 프로필 조회, 수정, 탈퇴 관련 API")
public class UserController {

	private final UserService userService;

	@GetMapping("/v1/users/me")
	public Response<MyProfileResponse> getMyProfile(@AuthenticatedUser AuthUser authUser) {
		return Response.of(userService.getMyProfile(authUser));
	}

}
