package com.parkez.auth.authentication.refresh;

import com.parkez.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Long userId;

	@Column(nullable = false)
	private String token;

	private RefreshToken(Long userId, String token) {
		this.userId = userId;
		this.token = token;
	}

	public static RefreshToken of(Long userId, String token) {
		return new RefreshToken(userId, token);
	}
}
