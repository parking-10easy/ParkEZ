package com.parkez.settlement.batch;

import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OwnerItemReaderTest {

    @Mock
    private UserReader userReader;
    @InjectMocks
    private OwnerItemReader ownerItemReader;

    private User mockUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void read_정상적으로_유저를_순차적으로_반환한다() {
        // given
        List<User> page0 = List.of(mockUser(1L), mockUser(2L), mockUser(3L));
        List<User> page1 = List.of(mockUser(4L));
        List<User> emptyPage = List.of();

        given(userReader.findAllOwnersByPage(0, 10)).willReturn(page0);
        given(userReader.findAllOwnersByPage(1, 10)).willReturn(page1);
        given(userReader.findAllOwnersByPage(2, 10)).willReturn(emptyPage);

        // when & then
        assertThat(ownerItemReader.read().getId()).isEqualTo(1L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(2L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(3L);
        assertThat(ownerItemReader.read().getId()).isEqualTo(4L);
        assertThat(ownerItemReader.read()).isNull(); // 더 이상 없음
    }
}