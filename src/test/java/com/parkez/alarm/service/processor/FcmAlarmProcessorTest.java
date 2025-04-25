package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.fcm.PushService;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmAlarmProcessorTest {

    @Mock
    private PushService pushService;

    @InjectMocks
    private FcmAlarmProcessor fcmAlarmProcessor;

    private Alarm createTestAlarm() {
        return Alarm.builder()
                .userId(1L)
                .targetId(100L)
                .targetType(AlarmTargetType.RESERVATION)
                .channel(AlarmChannel.FCM)
                .title("테스트 예약 알림")
                .message("예약 알림 메시지")
                .deviceToken("test-device-token")
                .build();
    }

    private User getOwner() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("테스트 소유자")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;
    }

    private User createTestUser() {
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스트 유저")
                .role(UserRole.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 2L);
        return user;
    }

    private ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(getOwner())
                .name("테스트 주차장")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    private ParkingZone createTestParkingZone() {
        ParkingLot parkingLot = createTestParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
    }

    private Reservation createTestReservation() {
        Reservation reservation = Reservation.builder()
                .user(createTestUser())
                .parkingLotName("테스트 주차장")
                .parkingZone(createTestParkingZone())
                .startDateTime(LocalDateTime.now())
                .endDateTime(LocalDateTime.now().plusHours(2))
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        return reservation;
    }

    private ReservationAlarmInfo createTestReservationAlarmInfo() {
        Reservation reservation = createTestReservation();
        return ReservationAlarmInfo.from(reservation);
    }

    @Test
    void 예약_FCM_알림_처리_성공() {
        // given
        Alarm alarm = createTestAlarm();

        // when
        fcmAlarmProcessor.processReservation(alarm);

        // then
        verify(pushService).sendReservationPush(
                eq(alarm),
                eq(alarm.getDeviceToken()),
                eq(alarm.getTitle()),
                eq(alarm.getMessage())
        );
    }

    @Test
    void 결제_FCM_알림_처리_취소() {
        // given
        ReservationAlarmInfo info = createTestReservationAlarmInfo();

        // when
        fcmAlarmProcessor.processPayment(info, NotificationType.CANCELED);

        // then
        verify(pushService).sendPaymentPush(
                eq(info.getUserId()),
                contains("[결제]"),
                eq("결제가 취소되었습니다.")
        );
    }

    @Test
    void 결제_FCM_알림_처리_실패() {
        // given
        ReservationAlarmInfo info = createTestReservationAlarmInfo();

        // when
        fcmAlarmProcessor.processPayment(info, NotificationType.FAILED);

        // then
        verify(pushService).sendPaymentPush(
                eq(info.getUserId()),
                contains("[결제]"),
                eq("결제가 실패했습니다.")
        );
    }
}
