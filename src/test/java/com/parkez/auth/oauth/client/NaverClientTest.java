package com.parkez.auth.oauth.client;


import com.parkez.auth.exception.AuthErrorCode;
import com.parkez.auth.oauth.config.NaverProperties;
import com.parkez.auth.oauth.config.NaverProviderProperties;
import com.parkez.auth.oauth.dto.response.OAuthUserInfo;
import com.parkez.auth.oauth.enums.OAuthProvider;
import com.parkez.common.exception.ParkingEasyException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest
@ActiveProfiles("test")
public class NaverClientTest {


    private NaverClient naverClient;
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        NaverProperties naverProperties = new NaverProperties(
                "test-client-id",
                "test-client-secret",
                "http://localhost:8080/api/v1/auth/naver/callback",
                "authorization_code",
                "client_secret_basic",
                "Naver",
                new String[]{"profile", "email"}
        );

        NaverProviderProperties naverProviderProperties = new NaverProviderProperties(
                "/oauth2.0/authorize",
                "/oauth2.0/token",
                "/v1/nid/me",
                "id"
        );

        naverClient = new NaverClient(webClient, naverProperties, naverProviderProperties);
    }


    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void 정상적으로_토큰_발급_후_사용자_정보를_조회하여_OAuthUserInfo를_반환한다() {
        // given
        String code = "code";

        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "access_token": "mock-access-token",
                          "refresh_token": "mock-refresh-token",
                          "token_type": "Bearer",
                          "expires_in": "3600"
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        mockWebServer.enqueue(new MockResponse()
                .setBody("""
                        {
                          "id": 12345,
                          "response": {
                            "email": "test@naver.com",
                            "nickname": "tester"
                          }
                        }
                        """)
                .addHeader("Content-Type", "application/json"));

        // when
        OAuthUserInfo userInfo = naverClient.requestUserInfo(code);

        // then
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getId()).isEqualTo(12345L);
        assertThat(userInfo.getEmail()).isEqualTo("test@naver.com");
        assertThat(userInfo.getNickname()).isEqualTo("tester");
        assertThat(userInfo.getProvider()).isEqualTo(OAuthProvider.NAVER);
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
        Assertions.assertThatThrownBy(() -> naverClient.requestUserInfo(code))
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
        Assertions.assertThatThrownBy(() -> naverClient.requestUserInfo(code))
                .isInstanceOf(ParkingEasyException.class)
                .hasMessageContaining(AuthErrorCode.OAUTH_USERINFO_FAILED.getDefaultMessage());
    }

}

