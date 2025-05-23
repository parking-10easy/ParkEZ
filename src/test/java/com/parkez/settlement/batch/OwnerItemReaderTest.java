package com.parkez.settlement.batch;

import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerItemReaderTest {

    @Mock
    private UserReader userReader;

    private OwnerItemReader ownerItemReader;

    private User mockUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void read_메서드_호출_시_정상적으로_유저를_순차적으로_반환한다() {
        // given
        YearMonth targetMonth = YearMonth.of(2025, 3);
        String targetMonthString = targetMonth.toString();
//        LocalDateTime now = LocalDateTime.now();
//        String targetMonthString = now.toString();
//        YearMonth targetMonth = YearMonth.from(now).minusMonths(1);
        ownerItemReader = new OwnerItemReader(userReader, targetMonthString);

        List<User> page0 = List.of(mockUser(1L), mockUser(2L), mockUser(3L));
        List<User> page1 = List.of(mockUser(4L));
        List<User> emptyPage = List.of();

        given(userReader.findOwnersForSettlementByMonth(targetMonth, 0L, 10)).willReturn(page0);
        given(userReader.findOwnersForSettlementByMonth(targetMonth, 3L, 10)).willReturn(page1);
        given(userReader.findOwnersForSettlementByMonth(targetMonth, 4L, 10)).willReturn(emptyPage);

        // when & then
        assertThat(ownerItemReader.read().getId()).isEqualTo(1L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(2L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(3L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(4L);
        assertThat(ownerItemReader.read()).isNull(); // 더 이상 없음
    }
}