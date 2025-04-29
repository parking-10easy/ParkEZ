package com.parkez.alarm.util;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class AlarmTestFactory {

    public static Alarm createPushTestAlarm() {
        return Alarm.builder()
                .userId(1L)
                .targetId(100L)
                .targetType(AlarmTargetType.RESERVATION)
                .channel(AlarmChannel.FCM)
                .title("Test Title")
                .message("Test Message")
                .deviceToken("test-token")
                .notificationType(NotificationType.UPCOMING)
                .sent(false)
                .build();
    }

    public static Alarm createEmailTestAlarm() {
        return Alarm.builder()
                .userId(2L)
                .targetId(100L)
                .targetType(AlarmTargetType.RESERVATION)
                .channel(AlarmChannel.EMAIL)
                .title("예약 알림")
                .message("테스트 메시지")
                .emailAddress("test@example.com")
                .notificationType(NotificationType.UPCOMING)
                .build();
    }

    public static Alarm createEmailAlarm() {
        return Alarm.builder()
                .id(1L)
                .channel(AlarmChannel.EMAIL)
                .targetType(AlarmTargetType.RESERVATION)
                .sent(false)
                .build();
    }

    public static Alarm createFcmAlarm() {
        return Alarm.builder()
                .id(2L)
                .channel(AlarmChannel.FCM)
                .targetType(AlarmTargetType.RESERVATION)
                .sent(false)
                .build();
    }

    public static User getOwner() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("테스트 소유자")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;
    }

    public static User createTestUser() {
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스트 유저")
                .role(UserRole.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 2L);
        return user;
    }

    public static ParkingLot createTestParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(getOwner())
                .name("테스트 주차장")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    public static ParkingZone createTestParkingZone() {
        ParkingLot parkingLot = createTestParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
    }

    public static Reservation createTestReservation() {
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
}
