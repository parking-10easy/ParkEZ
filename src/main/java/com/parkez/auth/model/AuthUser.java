package com.parkez.auth.model;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.parkez.user.domain.enums.UserRole;

import lombok.Getter;

@Getter
public class AuthUser {
	private final Long id;
	private final String email;
	private final Collection<? extends GrantedAuthority> authorities;
	private final String nickname;

	public AuthUser(Long id, String email, UserRole userRole, String nickname) {
		this.id = id;
		this.email = email;
		this.authorities = List.of(new SimpleGrantedAuthority(userRole.name()));
		this.nickname = nickname;
	}
}
