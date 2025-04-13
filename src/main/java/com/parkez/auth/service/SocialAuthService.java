package com.parkez.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.parkez.auth.dto.request.SocialOwnerProfileCompleteRequest;
import com.parkez.auth.dto.request.SocialUserProfileCompleteRequest;
import com.parkez.auth.dto.response.SocialSignupCompleteResponse;
import com.parkez.auth.dto.response.TokenResponse;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.client.OAuthClient;
import com.parkez.auth.oauth.client.OauthClientAdapter;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
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
	private final UserWriter userWriter;
	private final TokenManager tokenManager;

	@Transactional
	public TokenResponse login(OAuthProvider provider, String code, String state) {

		UserRole role = UserRole.fromState(state);
		OAuthClient oauthClient = oauthClientAdapter.getClient(provider);
		OAuthUserInfo oAuthUserInfo = oauthClient.requestUserInfo(code);
		LoginType loginType = LoginType.from(provider.name());
		User socialUser = userReader.findActiveUser(oAuthUserInfo.getEmail(), role, loginType)
			.orElseGet(
				() -> userWriter.createSocialUser(oAuthUserInfo.getEmail(), oAuthUserInfo.getNickname(), loginType,
					role)
			);

		return tokenManager.issueTokens(socialUser);
	}

	@Transactional
	public SocialSignupCompleteResponse completeUserSignup(AuthUser authUser,
		SocialUserProfileCompleteRequest request) {
		User user = userReader.getActiveUserById(authUser.getId());

		validateSignupNotCompleted(user);

		userWriter.completeSocialUserProfile(user, request.getPhone());

		return SocialSignupCompleteResponse.from(tokenManager.reissueAccessToken(user));
	}

	@Transactional
	public SocialSignupCompleteResponse completeOwnerSignup(AuthUser authUser,
		SocialOwnerProfileCompleteRequest request) {
		User user = userReader.getActiveUserById(authUser.getId());

		validateSignupNotCompleted(user);

		userWriter.completeSocialOwnerProfile(user, request.getPhone(), request.getBusinessNumber(),
			request.getDepositorName(), request.getBankName(), request.getBankAccount());

		return SocialSignupCompleteResponse.from(tokenManager.reissueAccessToken(user));
	}

	private void validateSignupNotCompleted(User user) {
		if (user.isSignupCompleted()) {
			throw new ParkingEasyException(AuthErrorCode.ALREADY_COMPLETED);
		}
	}

}
