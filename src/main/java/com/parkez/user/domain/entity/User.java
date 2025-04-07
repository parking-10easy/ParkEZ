package com.parkez.user.domain.entity;

import com.parkez.common.entity.BaseDeleteEntity;
import com.parkez.user.domain.enums.UserRole;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDeleteEntity {

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
		String profileImageUrl,
		UserRole role) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.phone = phone;
		this.businessAccountInfo = businessAccountInfo;
		this.profileImageUrl = profileImageUrl;
		this.role = role;
	}

	public static User createUser(String email, String encodedPassword, String nickname,
		String phone,String profileImageUrl) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.profileImageUrl(profileImageUrl)
			.role(UserRole.ROLE_USER)
			.build();
	}

	public static User createOwner(String email, String encodedPassword, String nickname, String phone,
		BusinessAccountInfo businessAccountInfo,String profileImageUrl) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.businessAccountInfo(businessAccountInfo)
			.profileImageUrl(profileImageUrl)
			.role(UserRole.ROLE_OWNER)
			.build();
	}

	public boolean isDeleted() {
		return this.deletedAt != null;
	}

	public void updateProfile(String nickname, String phone, BusinessAccountInfo businessAccountInfo) {
		this.nickname = nickname;
		this.phone = phone;
		this.businessAccountInfo = businessAccountInfo;
	}

	public void updateProfileImage(String profileImageUrl, String defaultProfileImageUrl) {
		this.profileImageUrl = StringUtils.hasText(profileImageUrl) ? profileImageUrl : defaultProfileImageUrl;
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void softDelete(String withdrawalNickname, LocalDateTime now) {
		this.nickname = withdrawalNickname;
		this.deletedAt = now;
	}
}
