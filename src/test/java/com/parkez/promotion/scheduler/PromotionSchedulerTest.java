package com.parkez.promotion.scheduler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkez.promotion.domain.enums.PromotionStatus;
import com.parkez.promotion.service.PromotionService;

@ExtendWith(MockitoExtension.class)
class PromotionSchedulerTest {

	@Mock
	private PromotionService promotionService;

	@InjectMocks
	private PromotionScheduler promotionScheduler;

	@Nested
	class UpdatePromotionStatus {

		@Test
		public void 종료된_프로모션_상태_변경_스케줄러_expireEndedPromotions_메서드_정상적으로_동작한다() {

			//when
			promotionScheduler.expireEndedPromotionStatus();

			//then
			verify(promotionService, times(1)).expireEndedPromotions(any(LocalDateTime.class),any(PromotionStatus.class),any(PromotionStatus.class));

		}

		@Test
		public void 쿠폰_매진된_프로모션_상태_변경_스케줄러_expireSoldOutPromotionStatus_메서드_정상적으로_동작한다() {

			//when
			promotionScheduler.expireSoldOutPromotionStatus();

			//then
			verify(promotionService, times(1)).expireSoldOutPromotions(any(PromotionStatus.class),any(PromotionStatus.class));

		}
	}

}