package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
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
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @Mock
    private FcmDeviceRepository fcmDeviceRepository;

    @InjectMocks
    private AlarmService alarmService;

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

    private FcmDevice createTestFcmDevice() {
        return FcmDevice.of(1L,"test-fcm-token");
    }

    @Test
    void 예약_알림_이메일_FCM_생성_성공() {
        // given
        Reservation reservation = createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.FCM))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserId(anyLong())).thenReturn(Optional.of(new FcmDevice(reservation.getUserId(), "test-token")));

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        verify(alarmRepository, times(2)).save(any(Alarm.class));
    }

    @Test
    void 이미_존재하는_알림이면_생성하지_않는다() {
        // given
        Reservation reservation = createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(true);
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.FCM))).thenReturn(true);
        when(fcmDeviceRepository.findFirstByUserId(anyLong())).thenReturn(Optional.of(new FcmDevice(reservation.getUserId(), "test-token")));

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        verify(alarmRepository, never()).save(any(Alarm.class));
    }

    @Test
    void FCM_디바이스가_없으면_FCM_알림_생성하지_않는다() {
        // given
        Reservation reservation = createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserId(anyLong()))
                .thenReturn(Optional.empty());

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        // then
        // 이메일 알림만 저장 호출
        verify(alarmRepository, times(1)).save(any(Alarm.class));
    }

    @Test
    void 예약_알림_만료_상태_생성() {
        Reservation reservation = createTestReservation();

        when(alarmRepository.existsAlarm(anyLong(), any(), any(), eq(AlarmChannel.EMAIL))).thenReturn(false);
        when(fcmDeviceRepository.findFirstByUserId(anyLong())).thenReturn(Optional.empty());

        alarmService.createReservationAlarms(reservation, NotificationType.EXPIRED);

        verify(alarmRepository, times(1)).save(any(Alarm.class));
    }
}