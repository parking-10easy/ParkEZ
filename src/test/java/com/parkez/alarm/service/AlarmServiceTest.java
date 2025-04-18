package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.domain.repository.AlarmRepository;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlarmServiceTest {

    @Mock
    private AlarmRepository alarmRepository;

    @InjectMocks
    private AlarmService alarmService;

    private static User getUser() {
        User user = User.builder()
                .email("user@email.com")
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private static ParkingZone getParkingZone() {
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
    }

    private static Reservation getReservation() {
        Reservation reservation = Reservation.builder()
                .user(getUser())
                .parkingZone(getParkingZone())
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        return reservation;
    }

    @Test
    void 알람_생성_특정_예약에_대한_알람이_존재하지_않으면_생성된다() {
        // given
        Reservation reservation = getReservation();
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), any())).thenReturn(false);

        // when
        alarmService.createReservationAlarms(reservation, NotificationType.EXPIRED);

        // then
        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmRepository).save(captor.capture());
        Alarm result = captor.getValue();

        assertThat(result).extracting("emailAddress","targetType","channel","notificationType")
                .containsExactly("user@email.com",AlarmTargetType.RESERVATION, AlarmChannel.EMAIL, NotificationType.EXPIRED );
    }

    @Test
    void 알람_생성_특정_예약에_대한_알람이_이미_존재하면_생성되지_않는다() {
        Reservation reservation = getReservation();
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), any())).thenReturn(true);

        alarmService.createReservationAlarms(reservation, NotificationType.UPCOMING);

        verify(alarmRepository, never()).save(any());
    }

    @Test
    void 알람_생성_특정_결제에_대한_알람이_존재하지_않으면_생성된다() {
        Reservation reservation = getReservation();
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), any())).thenReturn(false);

        alarmService.createPaymentAlarms(reservation, NotificationType.FAILED);

        ArgumentCaptor<Alarm> captor = ArgumentCaptor.forClass(Alarm.class);
        verify(alarmRepository).save(captor.capture());
        Alarm result = captor.getValue();

        assertThat(result).extracting("emailAddress","targetType","channel","notificationType")
                .containsExactly("user@email.com",AlarmTargetType.PAYMENT, AlarmChannel.EMAIL, NotificationType.FAILED );
    }

    @Test
    void 알람_생성_특정_결제에_대한_알람이_이미_존재하면_생성되지_않는다() {
        Reservation reservation = getReservation();
        when(alarmRepository.existsAlarm(anyLong(), any(), any(), any())).thenReturn(true);

        alarmService.createPaymentAlarms(reservation, NotificationType.FAILED);

        verify(alarmRepository, never()).save(any());
    }
}