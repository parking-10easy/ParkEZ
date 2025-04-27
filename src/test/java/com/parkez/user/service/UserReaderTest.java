package com.parkez.user.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.LoginType;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.domain.repository.UserRepository;
import com.parkez.user.exception.UserErrorCode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserReaderTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserReader userReader;

    @Nested
    class ExistsUser {

        @Test
        public void 이메일_역할_로그인타입이_일치하는_유저가_존재하면_True를_반환한다() {
            //given
            String email = "test@example.com";
            given(userRepository.existsByEmailAndRoleAndLoginType(anyString(), any(UserRole.class), any(LoginType.class))).willReturn(true);
            //when
            boolean result = userReader.existsUser(email, UserRole.ROLE_OWNER, LoginType.NORMAL);
            //then
            assertThat(result).isTrue();
        }

        @Test
        public void 이메일_역할_로그인타입이_일치하는_유저가_존재하지_않으면_False를_반환한다() {
            //given
            String email = "test@example.com";
            given(userRepository.existsByEmailAndRoleAndLoginType(anyString(), any(UserRole.class), any(LoginType.class))).willReturn(false);
            //when
            boolean result = userReader.existsUser(email, UserRole.ROLE_OWNER, LoginType.NORMAL);
            //then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class GetActiveUser {

        @Test
        public void 이메일_역할_로그인타입이_일치하고_유저가_존재하면_유저를_반환한다() {
            //given
            String email = "test@example.com";
            UserRole role = UserRole.ROLE_USER;
            LoginType loginType = LoginType.NORMAL;
            User user = createActiveUser(email, role, loginType);

            given(userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(anyString(), any(UserRole.class), any(
                    LoginType.class))).willReturn(Optional.of(user));

            //when
            User result = userReader.getActiveUser(email, role, loginType);

            //then
            assertThat(result).extracting(
                    "email", "role", "loginType", "deletedAt"
            ).containsExactly(email, role, loginType, null);
        }

        @Test
        public void 이메일_역할_로그인타입이_일치하고_유저가_존재하지_않으면_EMAIL_NOT_FOUND_예외() {
            //given
            String email = "test@example.com";
            UserRole role = UserRole.ROLE_USER;
            LoginType loginType = LoginType.NORMAL;
            given(userRepository.findByEmailAndRoleAndLoginTypeAndDeletedAtIsNull(anyString(), any(UserRole.class), any(
                    LoginType.class))).willReturn(Optional.empty());


            //when & then
            assertThatThrownBy(() -> userReader.getActiveUser(email, role, loginType))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(UserErrorCode.EMAIL_NOT_FOUND.getDefaultMessage());
        }

        private User createActiveUser(String email, UserRole role, LoginType loginType) {
            return User.builder()
                    .email(email)
                    .nickname("테스트 유저")
                    .phone("010-1234-5678")
                    .role(role)
                    .loginType(loginType)
                    .build()
                    ;
        }
    }

    @Nested
    class GetActiveUserById {

        @Test
        public void 아이디로_유저를_조회하면_유저를_반환한다() {
            //given
            Long id = 1L;
            User user = User.builder()
                    .nickname("테스트 유저")
                    .phone("010-1234-5678")
                    .build();

            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.of(user));
            //when
            User result = userReader.getActiveUserById(id);
            //then
            assertThat(result).isEqualTo(user);
        }

        @Test
        public void 아이디로_유저를_조회_탈퇴했거나_존재하지_않으면_USER_NOT_FOUND_예외발생() {
            //given
            Long id = 1L;
            given(userRepository.findByIdAndDeletedAtIsNull(anyLong())).willReturn(Optional.empty());

            //when & then
            assertThatThrownBy(() -> userReader.getActiveUserById(id))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());
        }

    }

    @Nested
    class getUserByEmailAndRole {
        @Test
        public void 이메일과_역할을_통해_삭제되지_않은_유저를_조회한다() {
            // given
            String email = "admin@parkez.com";
            UserRole role = UserRole.ROLE_ADMIN;

            User user = User.builder()
                    .email(email)
                    .nickname("관리자")
                    .role(role)
                    .build();

            given(userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role))
                    .willReturn(Optional.of(user));

            // when
            User result = userReader.getUserByEmailAndRole(email, role);

            // then
            assertThat(result).isEqualTo(user);

        }

        @Test
        public void 유저가_없으면_USER_NOT_FOUND_예외를_던진다() {
            // given
            String email = "notUser@parkez.com";
            UserRole role = UserRole.ROLE_ADMIN;
            given(userRepository.findByEmailAndRoleAndDeletedAtIsNull(email, role))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userReader.getUserByEmailAndRole(email, role))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getDefaultMessage());

        }
    }

    @Nested
    class findOwnersForSettlementByMonth {
        @Test
        void 해당_월에_정산할_결제_내역이_있는_ROLE_OWNER_권한의_유저를_전체_조회한다() {
            // given
            YearMonth yearMonth = YearMonth.of(2025, 3);
            Long lastId = 1L;
            int size = 10;

            List<User> owners = List.of(mock(User.class), mock(User.class));
            when(userRepository.findAllOwnersForSettlementByMonth(any(YearMonth.class), anyLong(), anyInt())).thenReturn(owners);

            // when
            List<User> result = userReader.findOwnersForSettlementByMonth(yearMonth, lastId, size);

            // then
            assertThat(result).isEqualTo(owners);
        }
    }

}