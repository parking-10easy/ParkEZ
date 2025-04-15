package com.parkez.auth.oauth.client;

import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.*;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.config.KakaoProperties;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@SpringBootTest
@ActiveProfiles("test")
class KakaoClientTest {

	@Autowired
	private KakaoProperties kakaoProperties;

	private KakaoClient kakaoClient;

	private MockWebServer mockWebServer;

	@BeforeEach
	void setUp() throws IOException {
		mockWebServer = new MockWebServer();
		mockWebServer.start(8081);

		String baseUrl = mockWebServer.url("/").toString();

		WebClient webClient = WebClient.builder()
			.baseUrl(baseUrl)
			.build();
		kakaoClient = new KakaoClient(kakaoProperties, webClient);
	}

	@AfterEach
	void tearDown() throws IOException {
		mockWebServer.shutdown();
	}

	@Nested
	class RequestUserInfo {

		@Test
		public void 정상적으로_토큰_발급_후_사용자_정보를_조회하여_OAuthUserInfo를_반환한다() {
			//given
			String code = "code";
			Long expectedId = 1L;
			String expectedEmail = "test@kakao.com";
			String expectedNickname = "test";
			OAuthProvider provider = OAuthProvider.KAKAO;

			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{
					  "token_type": "Bearer",
					  "access_token": "mock-access-token",
					  "id_token": "mock-id-token",
					  "expires_in": 3600,
					  "refresh_token": "mock-refresh-token",
					  "refresh_token_expires_in": "86400",
					  "scope": "profile account_email"
					}
					""")
				.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			);

			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{
						"id":1,
						"kakao_account":{
							"email":"test@kakao.com",
							"profile":{
								"nickname":"test"
							}
						}
					}
					""")
				.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

			//when
			OAuthUserInfo oAuthUserInfo = kakaoClient.requestUserInfo(code);

			//then
			Assertions.assertThat(oAuthUserInfo)
				.extracting(
					"id",
					"email",
					"nickname",
					"provider"
				).containsExactly(
					expectedId,
					expectedEmail,
					expectedNickname,
					provider
				);

		}

		@Test
		void accessToken_발급_실패시_OAUTH_ACCESS_TOKEN_FAILED_예외_발생() {
			// given
			String code = "invalid-code";

			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(401)
				.setBody("실패")
				.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			);

			// when & then
			Assertions.assertThatThrownBy(() -> kakaoClient.requestUserInfo(code))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.OAUTH_ACCESS_TOKEN_FAILED.getDefaultMessage());
		}

		@Test
		void userInfo_조회_실패시_ParkingEasyException_발생() {
			// given
			String code = "valid-code";

			// accessToken은 정상 발급
			mockWebServer.enqueue(new MockResponse()
				.setBody("""
					{
					  "token_type": "Bearer",
					  "access_token": "mock-access-token",
					  "id_token": "mock-id-token",
					  "expires_in": 3600,
					  "refresh_token": "mock-refresh-token",
					  "refresh_token_expires_in": "86400",
					  "scope": "profile account_email"
					}
				""")
				.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			);

			// userInfo 요청 실패 응답
			mockWebServer.enqueue(new MockResponse()
				.setResponseCode(500)
				.setBody("Internal Server Error")
				.addHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
			);

			// when & then
			Assertions.assertThatThrownBy(() -> kakaoClient.requestUserInfo(code))
				.isInstanceOf(ParkingEasyException.class)
				.hasMessageContaining(AuthErrorCode.OAUTH_USERINFO_FAILED.getDefaultMessage());
		}
	}

}