package com.parkez.common.principal;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.parkez.common.exception.CommonErrorCode;
import com.parkez.common.exception.ParkingEasyException;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthUser {
	private final Long id;
	private final String email;
	private final Collection<? extends GrantedAuthority> authorities;
	private final String nickname;
	private final boolean signupCompleted;

	@Builder
	private AuthUser(Long id, String email,String roleName, String nickname, boolean signupCompleted) {
		this.id = id;
		this.email = email;
		this.authorities = List.of(new SimpleGrantedAuthority(roleName));
		this.nickname = nickname;
		this.signupCompleted = signupCompleted;
	}

	public String getFirstAuthority() {
		return this.authorities.stream()
			.findFirst()
			.orElseThrow(() -> new ParkingEasyException(CommonErrorCode.AUTHORITY_NOT_FOUND))
			.getAuthority();
	}

	public boolean isSignupCompleted() {
		return signupCompleted;
	}

	public boolean isMine(Long dtoUserId, Long authUserId) {
		return dtoUserId.equals(authUserId);
	}
}
