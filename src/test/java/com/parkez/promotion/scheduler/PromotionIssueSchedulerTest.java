package com.parkez.promotion.scheduler;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.parkez.promotion.domain.enums.PromotionIssueStatus;
import com.parkez.promotion.service.PromotionIssueService;

@ExtendWith(MockitoExtension.class)
class PromotionIssueSchedulerTest {
	
	@Mock
	private PromotionIssueService promotionIssueService;

	@InjectMocks
	private PromotionIssueScheduler promotionIssueScheduler;

	@Nested
	class ExpirePromotionIssues {

		@Test
		public void 쿠폰_만료_처리_스케줄러_expirePromotionIssues_메서드_정상적으로_호출한다() {
			//when
			promotionIssueScheduler.expirePromotionIssues();

			//then
			verify(promotionIssueService).expirePromotionIssues(any(LocalDateTime.class),any(PromotionIssueStatus.class),any(PromotionIssueStatus.class));
		}
	}

}