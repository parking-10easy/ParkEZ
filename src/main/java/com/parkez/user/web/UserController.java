package com.parkez.user.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parkez.common.aop.CheckMemberStatus;
import com.parkez.common.principal.AuthUser;
import com.parkez.common.resolver.AuthenticatedUser;
import com.parkez.common.dto.response.Response;
import com.parkez.user.dto.request.UserChangePasswordRequest;
import com.parkez.user.dto.request.UserProfileImageUpdateRequest;
import com.parkez.user.dto.request.UserProfileUpdateRequest;
import com.parkez.user.dto.response.MyProfileResponse;
import com.parkez.user.dto.response.UserResponse;
import com.parkez.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CheckMemberStatus
@Tag(name = "04. 유저 API", description = "유저 프로필 조회, 수정, 탈퇴 관련 API")
public class UserController {

	private final UserService userService;


	@GetMapping("/v1/users/me")
	@Operation(summary = "내 프로필 조회", description = "내 프로필 정보를 조회한다")
	public Response<MyProfileResponse> getMyProfile(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser) {
		return Response.of(userService.getMyProfile(authUser));
	}

	@Operation(summary = "유저 프로필 조회", description = "유저의 기본정보를 조회한다.")
	@GetMapping("/v1/users/{id}")
	public Response<UserResponse> getUser(@PathVariable Long id) {
		return Response.of(userService.getUser(id));
	}


	@Operation(summary = "내 프로필 수정", description = "내 프로필 정보를 수정한다.")
	@PutMapping("/v1/users/me")
	public Response<Void> updateProfile(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,@Valid @RequestBody UserProfileUpdateRequest request) {

		userService.updateProfile(authUser, request);

		return Response.empty();
	}

	@Operation(summary = "내 프로필 사진 수정", description = "내 프로필 사진을 수정한다.")
	@PatchMapping("/v1/users/profile-image")
	public Response<Void> updateProfileImage(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser,
		@RequestBody UserProfileImageUpdateRequest request) {
		userService.updateProfileImage(authUser.getId(),request);
		return Response.empty();
	}

	@Operation(summary = "비밀번호 변경", description = "비밀번호를 변경한다.")
	@PatchMapping("/v1/users/password")
	public Response<Void> changePassword(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser, @Valid @RequestBody UserChangePasswordRequest request) {
		userService.changePassword(authUser.getId(), request);
		return Response.empty();
	}

	@Operation(summary = "회원 탈퇴", description = "회원 탈퇴한다.")
	@DeleteMapping("/v1/users/me")
	public Response<Void> deleteUser(@AuthenticatedUser @Parameter(hidden = true) AuthUser authUser) {
		userService.deleteUser(authUser.getId());
		return Response.empty();
	}



}
