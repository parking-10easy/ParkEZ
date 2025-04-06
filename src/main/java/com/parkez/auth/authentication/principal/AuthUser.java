package com.parkez.auth.authentication.principal;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.enums.UserRole;

import lombok.Builder;
import lombok.Getter;

@Getter
public class AuthUser {
	private final Long id;
	private final String email;
	private final Collection<? extends GrantedAuthority> authorities;
	private final String nickname;

	@Builder
	private AuthUser(Long id, String email, UserRole userRole, String nickname) {
		this.id = id;
		this.email = email;
		this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
		this.nickname = nickname;
	}

	public UserRole getFirstUserRole() {
		return UserRole.of(
			this.authorities.stream()
				.findFirst()
				.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.AUTHORITY_NOT_FOUND))
				.getAuthority()
		);

	}
}
