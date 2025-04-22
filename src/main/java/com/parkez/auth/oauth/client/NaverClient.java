package com.parkez.auth.oauth.client;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.config.NaverProperties;
import com.parkez.auth.oauth.config.NaverProviderProperties;
import com.parkez.auth.oauth.dto.response.NaverTokenResponse;
import com.parkez.auth.oauth.dto.response.NaverUserInfoResponse;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static com.parkez.auth.authentication.jwt.JwtProvider.BEARER_PREFIX;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverClient implements OAuthClient {

    private final WebClient webClient;
    private final NaverProperties naverProperties;
    private final NaverProviderProperties naverProviderProperties;

    @Override
    public OAuthUserInfo requestUserInfo(String code) {
        String accessToken = getAccessToken(code);
        return getUserInfo(accessToken);
    }

    @Override
    public boolean supports(OAuthProvider provider) {
        return provider == OAuthProvider.NAVER;
    }

    private String getAccessToken(String code) {
        return webClient.post()
                .uri(naverProviderProperties.getTokenUri())
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .body(
                        BodyInserters.fromFormData("grant_type", naverProperties.getAuthorizationGrantType())
                                .with("client_id", naverProperties.getClientId())
                                .with("client_secret", naverProperties.getClientSecret())
                                .with("redirect_uri", naverProperties.getRedirectUri())
                                .with("code", code)
                                .with("state", "naver")
                )
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("❎네이버 OAuth 토큰 발급 실패. 응답: {}", errorBody);
                                    return Mono.error(new ParkingEasyException(AuthErrorCode.OAUTH_ACCESS_TOKEN_FAILED));
                                })
                )
                .bodyToMono(NaverTokenResponse.class)
                .blockOptional()
                .map(NaverTokenResponse::getAccessToken)
                .orElseThrow(() -> new ParkingEasyException(AuthErrorCode.OAUTH_ACCESS_TOKEN_FAILED));
    }

    private OAuthUserInfo getUserInfo(String accessToken) {
        String authorizationHeader = BEARER_PREFIX + accessToken;

        NaverUserInfoResponse naverUserInfoResponse = webClient.post()
                .uri(naverProviderProperties.getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .header(HttpHeaders.CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("네이버 OAuth 유저 정보 요청 실패. 서버 응답: {}", errorBody);
                                    return Mono.error(new ParkingEasyException(AuthErrorCode.OAUTH_USERINFO_FAILED));
                                })
                )
                .bodyToMono(NaverUserInfoResponse.class)
                .blockOptional()
                .orElseThrow(() -> new ParkingEasyException(AuthErrorCode.OAUTH_USERINFO_FAILED));

        return OAuthUserInfo.ofNaverResponse(naverUserInfoResponse, OAuthProvider.NAVER);
    }
}