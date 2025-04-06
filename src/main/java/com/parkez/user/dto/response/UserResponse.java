package com.parkez.user.dto.response;

import com.parkez.user.domain.entity.User;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UserResponse {

	private final long id;
	private final String nickname;
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
