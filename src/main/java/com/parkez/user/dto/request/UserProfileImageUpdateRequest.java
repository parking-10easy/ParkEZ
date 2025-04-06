package com.parkez.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "사용자 프로필 사진 수정 요청")
public class UserProfileImageUpdateRequest {

	@Schema(description = "사용자 프로필 사진 주소", example = "https://test.jpg")
	private String profileImageUrl;
}
