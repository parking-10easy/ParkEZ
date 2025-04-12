package com.parkez.auth.authentication.refresh;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.parkez.auth.authentication.jwt.JwtProperties;

@ExtendWith(MockitoExtension.class)
class RefreshTokenStoreTest {

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private RefreshTokenStore refreshTokenStore;

	@Nested
	@DisplayName("set 메서드는")
	class Set {

		@Test
		public void 레디스에_리프레시_토큰을_저장한다() {
			//given
			Long userId = 1L;
			String refreshTokenValue = "refreshToken";
			long expiration = 3600L;

			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(jwtProperties.getRefreshTokenExpiration()).willReturn(expiration);

			//when
			refreshTokenStore.set(userId,refreshTokenValue);

			//then
			verify(valueOperations).set(eq("refresh:" + userId) ,eq(refreshTokenValue),eq(Duration.ofSeconds(expiration)));

		}
	}

	@Nested
	@DisplayName("existsBy 메서드는")
	class ExistsBy {

		@Test
		public void refresh_토큰_키가_존재하면_true를_반환한다() {
			//given
			Long userId = 1L;

			given(redisTemplate.hasKey(anyString())).willReturn(true);

			//when
			boolean exists = refreshTokenStore.existsBy(userId);

			//then
			assertThat(exists).isTrue();
		}

		@Test
		public void refresh_토큰_키가_존재하지_않으면_false를_반환한다() {
			//given
			Long userId = 1L;

			given(redisTemplate.hasKey(anyString())).willReturn(false);

			//when
			boolean exists = refreshTokenStore.existsBy(userId);

			//then
			assertThat(exists).isFalse();
		}
	}

	@Nested
	@DisplayName("get 메서드는")
	class get {

		@Test
		public void userId로_refresh_토큰_값을_조회한다() {
			//given
			Long userId = 1L;
			String expectedToken = "refresh-token";

			given(redisTemplate.opsForValue()).willReturn(valueOperations);
			given(valueOperations.get(anyString())).willReturn(expectedToken);

			//when
			String result = refreshTokenStore.get(userId);

			//then
			assertThat(result).isEqualTo(expectedToken);

		}
	}

}