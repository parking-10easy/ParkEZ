package com.parkez.auth.oauth.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.oauth.config.KakaoProperties;
import com.parkez.auth.oauth.dto.response.KakaoTokenResponse;
import com.parkez.auth.oauth.dto.response.KakaoUserInfoResponse;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoClient implements OAuthClient {

	private final KakaoProperties kakaoProperties;
	private final WebClient webClient;

	public OAuthUserInfo requestUserInfo(String code) {
		String accessToken = getAccessToken(code);
		return getUserInfo(accessToken);
	}


	@Override
	public boolean supports(String providerName) {
		return OAuthProvider.from(providerName) == OAuthProvider.KAKAO;
	}

	private String getAccessToken(String code) {
		// TODO 리팩토링 예정
		return webClient.post()
			.uri("https://kauth.kakao.com/oauth/token")
			.header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8")
			.body(
				BodyInserters.fromFormData("grant_type", kakaoProperties.getAuthorizationGrantType())
					.with("client_id", kakaoProperties.getClientId())
					.with("client_secret", kakaoProperties.getClientSecret())
					.with("redirect_uri", kakaoProperties.getRedirectUri())
					.with("code", code)
			)
			.retrieve()
			.onStatus(
				httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.map(errorBody -> new IllegalStateException("카카오 토큰 발급 실패: " + errorBody))
			)
			.bodyToMono(KakaoTokenResponse.class)
			.blockOptional()
			.map(KakaoTokenResponse::getAccessToken)
			.orElseThrow(() -> new IllegalStateException("카카오 토큰 발급 실패"));
	}

	private OAuthUserInfo getUserInfo(String accessToken) {

		KakaoUserInfoResponse kakaoUserInfoResponse = webClient.post()
			.uri("https://kapi.kakao.com/v2/user/me")
			.header(HttpHeaders.AUTHORIZATION, JwtProvider.BEARER_PREFIX + accessToken)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.retrieve()
			.onStatus(
				httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.map(errorBody -> new IllegalStateException("카카오 토큰 발급 실패: " + errorBody))
			)
			.bodyToMono(KakaoUserInfoResponse.class)
			.blockOptional()
			.orElseThrow(() -> new IllegalStateException("카카오 사용자 정보 요청 실패"));

		return OAuthUserInfo.of(kakaoUserInfoResponse, kakaoProperties.getClientName());
	}

}
