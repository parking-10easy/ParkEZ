package com.parkez.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.authentication.refresh.RefreshTokenRedisStore;
import com.parkez.auth.dto.request.SocialOwnerProfileCompleteRequest;
import com.parkez.auth.dto.request.SocialUserProfileCompleteRequest;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.client.OAuthClient;
import com.parkez.auth.oauth.client.OauthClientAdapter;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import com.parkez.user.service.UserWriter;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SocialAuthService {

	private final OauthClientAdapter oauthClientAdapter;
	private final UserReader userReader;
	private final JwtProvider jwtProvider;
	private final UserWriter userWriter;
	private final RefreshTokenRedisStore refreshTokenStore;

	@Transactional
	public TokenResponse login(String providerName, String code, String state) {
		UserRole role = UserRole.fromState(state);
		OAuthClient oauthClient = oauthClientAdapter.getClient(providerName);
		OAuthUserInfo oAuthUserInfo = oauthClient.requestUserInfo(code);
		if (!userReader.existsUser(oAuthUserInfo.getEmail(), role, LoginType.NORMAL)) {
			LoginType loginType = LoginType.of(providerName);
			User socialUser = userWriter.createSocialUser(
				oAuthUserInfo.getEmail(),
				oAuthUserInfo.getNickname(),
				loginType,
				role
			);
			String accessToken = jwtProvider.createAccessToken(socialUser.getId(), socialUser.getEmail(), socialUser.getRoleName(),
				socialUser.getNickname(), socialUser.isSignupCompleted());
			String refreshToken = jwtProvider.createRefreshToken(socialUser.getId());
			refreshTokenStore.set(socialUser.getId(), refreshToken);
			return TokenResponse.of(accessToken, refreshToken);
		}
		User user = userReader.getActiveUser(oAuthUserInfo.getEmail(), role, LoginType.NORMAL);

		String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRoleName(),
			user.getNickname(), user.isSignupCompleted());
		String refreshToken = jwtProvider.createRefreshToken(user.getId());

		refreshTokenStore.set(user.getId(), refreshToken);
		return TokenResponse.of(accessToken, refreshToken);
	}

	@Transactional
	public TokenResponse completeUserSignup(AuthUser authUser, SocialUserProfileCompleteRequest request) {
		User user = userReader.getActiveUserById(authUser.getId());

		if (user.isSignupCompleted()) {
			throw new ParkingEasyException(AuthErrorCode.ALREADY_COMPLETED);
		}

		userWriter.completeSocialUserProfile(user, request.getPhone());

		String accessToken = jwtProvider.createAccessToken(
			user.getId(),
			user.getEmail(),
			user.getRoleName(),
			user.getNickname(),
			user.isSignupCompleted()
		);
		String refreshToken = refreshTokenStore.get(user.getId());
		return TokenResponse.of(accessToken,refreshToken);
	}

	@Transactional
	public TokenResponse completeOwnerSignup(AuthUser authUser, SocialOwnerProfileCompleteRequest request) {
		User user = userReader.getActiveUserById(authUser.getId());

		if (user.isSignupCompleted()) {
			throw new ParkingEasyException(AuthErrorCode.ALREADY_COMPLETED);
		}

		userWriter.completeSocialOwnerProfile(user, request.getPhone(), request.getBusinessNumber(),request.getDepositorName(),request.getBankName(),request.getBankAccount());

		String accessToken = jwtProvider.createAccessToken(
			user.getId(),
			user.getEmail(),
			user.getRoleName(),
			user.getNickname(),
			user.isSignupCompleted()
		);

		String refreshToken = refreshTokenStore.get(user.getId());
		return TokenResponse.of(accessToken,refreshToken);
	}

}
