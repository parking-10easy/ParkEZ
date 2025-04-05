package com.parkez.auth.refresh;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
class RefreshTokenJpaStoreTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private RefreshTokenJpaStore refreshTokenJpaStore;

	@Nested
	class Save{

		@Test
		public void 리프레시_토큰_저장_새토큰저장() {
			//given
			Long userId = 1L;
			String refreshTokenValue = "newRefreshToken";
			//when
			refreshTokenJpaStore.save(userId, refreshTokenValue);
			//then
			verify(refreshTokenRepository,times(1)).save(any(RefreshToken.class));

		}
	}

	@Nested
	class Replace {

		@Test
		public void 리프레시_토큰_저장_기존토큰_존재하면_삭제후_새토큰저장() {
			//given
			Long userId = 1L;
			String refreshTokenValue = "newRefreshToken";
			given(refreshTokenRepository.existsByUserId(anyLong())).willReturn(true);
			//when
			refreshTokenJpaStore.replace(userId, refreshTokenValue);
			//then
			verify(refreshTokenRepository,times(1)).existsByUserId(anyLong());
			verify(refreshTokenRepository,times(1)).deleteByUserId(anyLong());
			verify(refreshTokenRepository,times(1)).save(any(RefreshToken.class));

		}

		@Test
		public void 리프레시_토큰_저장_기존토큰_없으면_새토큰저장() {
			//given
			Long userId = 1L;
			String refreshTokenValue = "newRefreshToken";
			given(refreshTokenRepository.existsByUserId(anyLong())).willReturn(false);
			//when
			refreshTokenJpaStore.replace(userId, refreshTokenValue);
			//then
			verify(refreshTokenRepository,times(1)).existsByUserId(anyLong());
			verify(refreshTokenRepository,never()).deleteByUserId(anyLong());
			verify(refreshTokenRepository,times(1)).save(any(RefreshToken.class));

		}
	}
	
}