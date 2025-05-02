package com.parkez.user.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
	PENDING("미완료"),
	COMPLETED("정상 회원")
	;

	private final String description;

}
