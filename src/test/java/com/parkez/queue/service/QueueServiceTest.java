package com.parkez.queue.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.domain.repository.QueueRepository;
import com.parkez.queue.dto.WaitingUserDto;
import com.parkez.queue.dto.response.MyWaitingQueueDetailResponse;
import com.parkez.queue.dto.response.MyWaitingQueueListResponse;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.user.domain.enums.UserRole;
import com.parkez.user.service.UserReader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @InjectMocks
    private QueueService queueService;

    @Mock
    private QueueRepository queueRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private UserReader userReader;

    @Mock
    private ParkingZoneReader parkingZoneReader;

    private static ReservationRequest createReservationRequest() {
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        return new ReservationRequest(1L, start, end);
    }

    private static AuthUser createAuthUser(Long id) {
        return AuthUser.builder()
                .id(id)
                .email("test@example.com")
                .roleName(UserRole.Authority.USER)
                .nickname("test")
                .build();
    }

    private static Reservation createMockReservation(Long id, Long parkingZoneId) {
        Reservation reservation = Reservation.builder().build();
        ReflectionTestUtils.setField(reservation, "id", id);
        ReflectionTestUtils.setField(reservation, "startDateTime", LocalDateTime.now().plusHours(1));
        ReflectionTestUtils.setField(reservation, "endDateTime", LocalDateTime.now().plusHours(2));

        ParkingZone parkingZone = ParkingZone.builder().build();
        ReflectionTestUtils.setField(parkingZone, "id", parkingZoneId);

        ReflectionTestUtils.setField(reservation, "parkingZone", parkingZone);

        return reservation;
    }


    @Nested
    class JoinWaitingQueueTest {

        @Test
        void 대기열_이미_등록되어있으면_ALREADY_JOINED_반환() {
            // given
            Long userId = 1L;
            ReservationRequest request = createReservationRequest();

            given(queueRepository.isAlreadyInQueue(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(userId))).willReturn(true);

            // when
            JoinQueueResult result = queueService.joinWaitingQueue(userId, request);

            // then
            assertThat(result).isEqualTo(JoinQueueResult.ALREADY_JOINED);
        }

        @Test
        void 대기열_등록_성공하면_JOINED_반환() {
            // given
            Long userId = 1L;
            ReservationRequest request = createReservationRequest();

            given(queueRepository.isAlreadyInQueue(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(userId))).willReturn(false);

            // when
            JoinQueueResult result = queueService.joinWaitingQueue(userId, request);

            // then
            assertThat(result).isEqualTo(JoinQueueResult.JOINED);
            verify(queueRepository).enqueue(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(WaitingUserDto.class));
        }
    }

    @Nested
    class DequeueConvertToDtoTest {

        @Test
        void 정상적으로_DTO_변환() {
            // given
            String queueKey = "queueKey";
            Map<String, Object> waitingUserMap = new HashMap<>();
            waitingUserMap.put("userId", 1L);
            waitingUserMap.put("parkingZoneId", 1L);
            waitingUserMap.put("reservationStartDateTime", LocalDateTime.now().toString());
            waitingUserMap.put("reservationEndDateTime", LocalDateTime.now().plusHours(1).toString());

            given(queueRepository.dequeue(queueKey)).willReturn(waitingUserMap);

            // when
            WaitingUserDto dto = queueService.dequeueConvertToDto(queueKey);

            // then
            assertThat(dto).isNotNull();
            assertThat(dto.getUserId()).isEqualTo(1L);
            assertThat(dto.getParkingZoneId()).isEqualTo(1L);
        }

        @Test
        void 타입이_다르면_DTO_CONVERT_FAIL_TYPE_예외발생() {
            // given
            String queueKey = "queueKey";
            String invalidObject = "invalid-type";

            given(queueRepository.dequeue(queueKey)).willReturn(invalidObject);

            // when & then
            assertThatThrownBy(() -> queueService.dequeueConvertToDto(queueKey))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(QueueErrorCode.DTO_CONVERT_FAIL_TYPE.getDefaultMessage());

        }
    }

    @Nested
    class ConvertToDtoTest {

        @Test
        void 정상적으로_DTO_변환된다() {
            // given
            Map<String, Object> validMap = Map.of(
                    "userId", "1",
                    "parkingZoneId", "1",
                    "reservationStartDateTime", LocalDateTime.now().toString(),
                    "reservationEndDateTime", LocalDateTime.now().plusHours(1).toString()
            );

            // when
            WaitingUserDto result = ReflectionTestUtils.invokeMethod(queueService, "convertToDto", validMap);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getParkingZoneId()).isEqualTo(1L);
        }

        @Test
        void 숫자변환_실패시_DTO_CONVERT_FAIL_NUMBER_예외발생() {
            // given
            Map<String, Object> invalidNumberMap = Map.of(
                    "userId", "invalid-number",
                    "parkingZoneId", "1",
                    "reservationStartDateTime", LocalDateTime.now().toString(),
                    "reservationEndDateTime", LocalDateTime.now().plusHours(1).toString()
            );

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(queueService, "convertToDto", invalidNumberMap))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(QueueErrorCode.DTO_CONVERT_FAIL_NUMBER.getDefaultMessage());
        }

        @Test
        void 날짜변환_실패시_DTO_CONVERT_FAIL_TIME_예외발생() {
            // given
            Map<String, Object> invalidDateMap = Map.of(
                    "userId", "1",
                    "parkingZoneId", "1",
                    "reservationStartDateTime", "invalid-date",
                    "reservationEndDateTime", LocalDateTime.now().plusHours(1).toString()
            );

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(queueService, "convertToDto", invalidDateMap))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(QueueErrorCode.DTO_CONVERT_FAIL_TIME.getDefaultMessage());
        }

        @Test
        void Null값_존재시_DTO_CONVERT_FAIL_NULL_예외발생() {
            // given
            Map<String, Object> nullFieldMap = new HashMap<>();
            nullFieldMap.put("userId", null);

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(queueService, "convertToDto", nullFieldMap))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(QueueErrorCode.DTO_CONVERT_FAIL_NULL.getDefaultMessage());
        }

        @Test
        void Map이_아닌_타입이면_DTO_CONVERT_FAIL_TYPE_예외발생() {
            // given
            String invalidType = "invalid-object";

            // when & then
            assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(queueService, "convertToDto", invalidType))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining(QueueErrorCode.DTO_CONVERT_FAIL_TYPE.getDefaultMessage());
        }
    }

    @Nested
    class DeleteExpiredQueuesTest {

        @Test
        void 대기열_키가_없으면_아무것도_하지_않는다() {
            // given
            given(redisTemplate.keys(anyString())).willReturn(Collections.emptySet());

            // when
            queueService.deleteExpiredQueues();

            // then
            verify(queueRepository, never()).deleteQueue(anyString());
        }

        @Test
        void 대기열_키가_있지만_만료되지_않으면_삭제되지_않는다() {
            // given
            String queueKey = "reservation:queue:1:209912311200-209912311300"; // 미래 시점
            given(redisTemplate.keys(anyString())).willReturn(Set.of(queueKey));

            // when
            queueService.deleteExpiredQueues();

            // then
            verify(queueRepository, never()).deleteQueue(anyString());
        }

        @Test
        void 대기열_키가_있고_만료되었으면_대기열_삭제된다() {
            // given
            LocalDateTime pastStartTime = LocalDateTime.now().minusHours(2);
            String formattedStart = pastStartTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String queueKey = "reservation:queue:1:" + formattedStart + "-" + formattedStart;

            given(redisTemplate.keys(anyString())).willReturn(Set.of(queueKey));
            given(queueRepository.getAll(queueKey)).willReturn(List.of(
                    Map.of(
                            "userId", "1",
                            "parkingZoneId", "1",
                            "reservationStartDateTime", pastStartTime.toString(),
                            "reservationEndDateTime", pastStartTime.plusHours(1).toString()
                    )
            ));

            // when
            queueService.deleteExpiredQueues();

            // then
            verify(queueRepository).deleteQueue(queueKey);
        }

        @Test
        void 키파싱_실패하면_에러로그_남기고_정상종료() {
            // given
            String invalidKey = "reservation:queue:invalidkey"; // 정상 포맷 아님
            given(redisTemplate.keys(anyString())).willReturn(Set.of(invalidKey));

            // when & then
            assertDoesNotThrow(() -> queueService.deleteExpiredQueues());
        }
    }

    @Nested
    class FindMyWaitingQueuesTest {

        @Test
        void 대기열이_없으면_빈리스트를_반환한다() {
            // given
            AuthUser authUser = createAuthUser(1L);
            given(queueRepository.findAllQueueKeys()).willReturn(Collections.emptySet());

            // when
            List<MyWaitingQueueListResponse> result = queueService.findMyWaitingQueues(authUser);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 대기열은_있지만_리스트가_비어있으면_결과_없음() {
            // given
            AuthUser authUser = createAuthUser(1L);
            given(queueRepository.findAllQueueKeys()).willReturn(Set.of("queueKey"));
            given(queueRepository.getWaitingList("queueKey")).willReturn(Collections.emptyList());

            // when
            List<MyWaitingQueueListResponse> result = queueService.findMyWaitingQueues(authUser);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        void 내가_대기열에_있으면_정상적으로_리스트에_포함된다() {
            // given
            AuthUser authUser = createAuthUser(1L);
            Reservation reservation = createMockReservation(1L, 1L);

            Map<String, Object> map = Map.of(
                    "userId", 1L,
                    "parkingZoneId", 1L,
                    "reservationStartDateTime", reservation.getStartDateTime().toString(),
                    "reservationEndDateTime", reservation.getEndDateTime().toString()
            );

            given(queueRepository.findAllQueueKeys()).willReturn(Set.of("queueKey"));
            given(queueRepository.getWaitingList("queueKey")).willReturn(List.of(map));
            given(reservationReader.findReservationByQueueKey(1L, reservation.getStartDateTime(), reservation.getEndDateTime())).willReturn(reservation);
            given(parkingZoneReader.getActiveByParkingZoneId(1L)).willReturn(reservation.getParkingZone());

            // when
            List<MyWaitingQueueListResponse> result = queueService.findMyWaitingQueues(authUser);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getParkingZoneId()).isEqualTo(1L);
        }

        @Test
        void 내가_대기열에_없으면_리스트에_포함되지_않는다() {
            // given
            AuthUser authUser = createAuthUser(2L); // 다른 사용자
            Reservation reservation = createMockReservation(1L, 1L);

            Map<String, Object> map = Map.of(
                    "userId", 1L,
                    "parkingZoneId", 1L,
                    "reservationStartDateTime", reservation.getStartDateTime().toString(),
                    "reservationEndDateTime", reservation.getEndDateTime().toString()
            );

            given(queueRepository.findAllQueueKeys()).willReturn(Set.of("queueKey"));
            given(queueRepository.getWaitingList("queueKey")).willReturn(List.of(map));

            // when
            List<MyWaitingQueueListResponse> result = queueService.findMyWaitingQueues(authUser);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindMyQueueTest {

        @Test
        void 내_대기열_상세조회_성공() {
            // given
            AuthUser authUser = createAuthUser(1L);
            Reservation reservation = createMockReservation(1L, 1L);
            ParkingZone parkingZone = ParkingZone.builder().build();
            ReflectionTestUtils.setField(parkingZone, "id", 1L);
            ReflectionTestUtils.setField(parkingZone, "name", "테스트존");

            given(reservationReader.findById(anyLong())).willReturn(reservation);
            given(queueRepository.getWaitingList(anyString())).willReturn(List.of(
                    Map.of(
                            "userId", 1L,
                            "parkingZoneId", 1L,
                            "reservationStartDateTime", reservation.getStartDateTime().toString(),
                            "reservationEndDateTime", reservation.getEndDateTime().toString()
                    )
            ));
            given(parkingZoneReader.getActiveByParkingZoneId(1L)).willReturn(parkingZone);

            // when
            MyWaitingQueueDetailResponse result = queueService.findMyQueue(authUser, 1L);

            // then
            assertThat(result.getParkingZoneId()).isEqualTo(1L);
            assertThat(result.getParkingZoneName()).isEqualTo("테스트존");
            assertThat(result.getMyQueue()).isEqualTo(1);
        }

        @Test
        void 대기열에_없으면_NOT_IN_QUEUE_예외발생() {
            // given
            AuthUser authUser = createAuthUser(1L);
            Reservation reservation = createMockReservation(1L, 1L);

            given(reservationReader.findById(anyLong())).willReturn(reservation);
            given(queueRepository.getWaitingList(anyString())).willReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> queueService.findMyQueue(authUser, 1L))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining("대기열에 존재하지 않습니다");
        }
    }

    @Nested
    class CancelMyQueueTest {

        @Test
        void 대기열_취소_성공() {
            // given
            AuthUser authUser = createAuthUser(1L);
            Reservation reservation = createMockReservation(1L, 1L);

            given(reservationReader.findById(anyLong())).willReturn(reservation);
            given(queueRepository.getWaitingList(anyString())).willReturn(List.of(
                    Map.of(
                            "userId", 1L,
                            "parkingZoneId", 1L,
                            "reservationStartDateTime", reservation.getStartDateTime().toString(),
                            "reservationEndDateTime", reservation.getEndDateTime().toString()
                    )
            ));

            // when
            queueService.cancelMyQueue(authUser, 1L);

            // then
            verify(queueRepository).removeFromQueue(anyString(), any());
        }

        @Test
        void 대기열에_내가없으면_NOT_IN_QUEUE_예외발생() {
            // given
            AuthUser authUser = createAuthUser(1L);
            Reservation reservation = createMockReservation(1L, 1L);

            given(reservationReader.findById(anyLong())).willReturn(reservation);
            given(queueRepository.getWaitingList(anyString())).willReturn(Collections.emptyList());

            // when & then
            assertThatThrownBy(() -> queueService.cancelMyQueue(authUser, 1L))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessageContaining("대기열에 존재하지 않습니다");
        }
    }


}
