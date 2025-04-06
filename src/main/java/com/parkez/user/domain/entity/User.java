package com.parkez.user.domain.entity;

import com.parkez.user.domain.enums.UserRole;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private static final String DEFAULT_PROFILE_IMAGE_URL = "default.jpg";

    @Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	private String password;

	@Column(nullable = false)
	private String nickname;

	private String phone;

	@Embedded
	private BusinessAccountInfo businessAccountInfo;

	private String profileImageUrl;

	@Enumerated(EnumType.STRING)
	private UserRole role;

	private LocalDateTime deletedAt;

	@Builder
	private User(String email, String password, String nickname, String phone,
		BusinessAccountInfo businessAccountInfo,
		UserRole role) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.phone = phone;
		this.businessAccountInfo = businessAccountInfo;
		this.profileImageUrl = DEFAULT_PROFILE_IMAGE_URL;
		this.role = role;
	}

	public static User createUser(String email, String encodedPassword, String nickname,
		String phone) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.role(UserRole.ROLE_USER)
			.build();
	}

	public static User createOwner(String email, String encodedPassword, String nickname, String phone,
		BusinessAccountInfo businessAccountInfo) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.businessAccountInfo(businessAccountInfo)
			.role(UserRole.ROLE_OWNER)
			.build();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

}
