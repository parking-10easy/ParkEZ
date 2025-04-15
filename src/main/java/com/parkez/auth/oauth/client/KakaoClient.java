package com.parkez.auth.oauth.client;

import static com.parkez.auth.authentication.jwt.JwtProvider.*;
import static org.springframework.http.MediaType.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.parkez.auth.authentication.jwt.JwtProvider;
import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.config.KakaoProperties;
import com.parkez.auth.oauth.dto.response.KakaoTokenResponse;
import com.parkez.auth.oauth.dto.response.KakaoUserInfoResponse;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
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
	public boolean supports(OAuthProvider provider) {
		return provider == OAuthProvider.KAKAO;
	}

	private String getAccessToken(String code) {
		return webClient.post()
			.uri(kakaoProperties.getTokenUri())
			.header(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
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
					.flatMap(errorBody ->  {
						log.error("카카오 OAuth 토큰 발급 실패. 서버 응답: {}", errorBody);
						return Mono.error(new ParkingEasyException(AuthErrorCode.OAUTH_ACCESS_TOKEN_FAILED));
					})
			)
			.bodyToMono(KakaoTokenResponse.class)
			.blockOptional()
			.map(KakaoTokenResponse::getAccessToken)
			.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.OAUTH_ACCESS_TOKEN_FAILED));
	}

	private OAuthUserInfo getUserInfo(String accessToken) {
		String authorizationHeader = String.format("%s%s", BEARER_PREFIX, accessToken);

		KakaoUserInfoResponse kakaoUserInfoResponse = webClient.post()
			.uri(kakaoProperties.getUserInfoUri())
			.header(HttpHeaders.AUTHORIZATION, authorizationHeader)
			.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
			.retrieve()
			.onStatus(
				httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
				clientResponse -> clientResponse.bodyToMono(String.class)
					.flatMap(errorBody -> {
						log.error("카카오 OAuth 유저 정보 요청 실패. 서버 응답: {}", errorBody);
						return Mono.error(new ParkingEasyException(AuthErrorCode.OAUTH_USERINFO_FAILED));
					})
			)
			.bodyToMono(KakaoUserInfoResponse.class)
			.blockOptional()
			.orElseThrow(() -> new ParkingEasyException(AuthErrorCode.OAUTH_USERINFO_FAILED));

		return OAuthUserInfo.of(kakaoUserInfoResponse,OAuthProvider.KAKAO);
	}

}
