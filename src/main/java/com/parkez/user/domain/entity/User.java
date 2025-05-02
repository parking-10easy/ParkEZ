package com.parkez.user.domain.entity;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import com.parkez.common.entity.BaseDeleteEntity;
import com.parkez.common.principal.AuthUser;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_email_role_login_type", columnNames = {"email", "role", "login_type"})
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseDeleteEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String email;

	@Column(nullable = false)
	private String password;

	@Column(nullable = false)
	private String nickname;

	private String phone;

	@Embedded
	private BusinessAccountInfo businessAccountInfo;

	private String profileImageUrl;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private LoginType loginType;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserStatus status;

	private User(Long id, String email, String nickname, UserRole role) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.role = role;
	}

	@Builder
	private User(String email, String password, String nickname, String phone,
		BusinessAccountInfo businessAccountInfo,
		String profileImageUrl,
		UserRole role, LoginType loginType, UserStatus status) {
		this.email = email;
		this.password = password;
		this.nickname = nickname;
		this.phone = phone;
		this.businessAccountInfo = businessAccountInfo;
		this.profileImageUrl = profileImageUrl;
		this.role = role;
		this.loginType = loginType;
		this.status = status;
	}

	public static User createUser(String email, String encodedPassword, String nickname,
		String phone, String profileImageUrl) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.profileImageUrl(profileImageUrl)
			.role(UserRole.ROLE_USER)
			.loginType(LoginType.NORMAL)
			.status(UserStatus.COMPLETED)
			.build();
	}

	public static User createOwner(String email, String encodedPassword, String nickname, String phone,
		String businessNumber, String depositorName,
		String bankName, String bankAccount
		, String profileImageUrl) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.phone(phone)
			.businessAccountInfo(BusinessAccountInfo.create(businessNumber, depositorName, bankName, bankAccount))
			.profileImageUrl(profileImageUrl)
			.role(UserRole.ROLE_OWNER)
			.loginType(LoginType.NORMAL)
			.status(UserStatus.COMPLETED)
			.build();
	}

	public static User createSocialUser(String email,String encodedPassword, String nickname, LoginType loginType, UserRole role) {
		return User.builder()
			.email(email)
			.password(encodedPassword)
			.nickname(nickname)
			.role(role)
			.loginType(loginType)
			.status(UserStatus.PENDING)
			.build();
	}

	public static User ofIdEmailRole(Long id, String email, UserRole role) {
		User user = new User();
		user.id = id;
		user.email = email;
		user.role = role;
		return user;
	}

	public static User from(AuthUser authUser) {
		return new User(authUser.getId(), authUser.getEmail(), authUser.getNickname(), UserRole.of(authUser.getFirstAuthority()));
	}

	public boolean isDeleted() {
		return this.getDeletedAt() != null;
	}

	public void updateProfile(String nickname, String phone, String businessNumber, String depositorName,
		String bankName, String bankAccount) {
		this.nickname = nickname;
		this.phone = phone;
		this.businessAccountInfo = BusinessAccountInfo.create(businessNumber, depositorName, bankName, bankAccount);
	}

	public void updateProfileImage(String profileImageUrl, String defaultProfileImageUrl) {
		this.profileImageUrl = StringUtils.hasText(profileImageUrl) ? profileImageUrl : defaultProfileImageUrl;
	}

	public void updatePassword(String encodedPassword) {
		this.password = encodedPassword;
	}

	public void softDelete(String withdrawalNickname, LocalDateTime deletedAt) {
		this.nickname = withdrawalNickname;
		updateDeletedAt(deletedAt);
	}

	public String getRoleName() {
		return this.role.name();
	}

	public String getBusinessNumber() {
		return businessAccountInfo != null ? businessAccountInfo.getBusinessNumber() : null;
	}

	public String getBankName() {
		return businessAccountInfo != null ? businessAccountInfo.getBankName() : null;
	}

	public String getBankAccount() {
		return businessAccountInfo != null ? businessAccountInfo.getBankAccount() : null;
	}

	public String getDepositorName() {
		return businessAccountInfo != null ? businessAccountInfo.getDepositorName() : null;
	}

	public boolean isSignupCompleted() {
		return this.status == UserStatus.COMPLETED;
	}

	public void completeUserProfile(String phone) {
		this.phone =phone;
		this.status = UserStatus.COMPLETED;
	}

	public void completeOwnerProfile(String phone, String businessNumber, String depositorName, String bankName,
		String bankAccount) {
		this.phone = phone;
		this.businessAccountInfo = BusinessAccountInfo.create(businessNumber, depositorName, bankName, bankAccount);
		this.status = UserStatus.COMPLETED;

	}
}
