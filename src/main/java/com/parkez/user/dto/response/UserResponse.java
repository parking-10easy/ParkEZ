package com.parkez.user.dto.response;

import com.parkez.user.domain.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Schema(description = "유저 프로필 응답 DTO")
public class UserResponse {

	@Schema(description = "유저 ID", example = "1")
	private final long id;

	@Schema(description = "닉네임", example = "홍길동")
	private final String nickname;

	@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
	private final String profileImageUrl;

	@Builder
	private UserResponse(long id, String nickname, String profileImageUrl) {
		this.id = id;
		this.nickname = nickname;
		this.profileImageUrl = profileImageUrl;
	}

	public static UserResponse from(User user) {
		return UserResponse.builder()
			.id(user.getId())
			.nickname(user.getNickname())
			.profileImageUrl(user.getProfileImageUrl())
			.build();
	}
}
