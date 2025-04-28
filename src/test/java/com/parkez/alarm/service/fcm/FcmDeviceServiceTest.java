package com.parkez.alarm.service.fcm;

import com.parkez.alarm.domain.entity.FcmDevice;
import com.parkez.alarm.domain.repository.FcmDeviceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FcmDeviceServiceTest {

    @Mock
    private FcmDeviceRepository deviceRepository;

    @InjectMocks
    private FcmDeviceService fcmDeviceService;

    private final Long userId = 1L;
    private final String token = "test-token";

    @Test
    void 기존_토큰이_있을때_기존을_false_로_만들고_새로저장() {
        // given
        FcmDevice existingDevice = FcmDevice.of(2L, token);
        when(deviceRepository.findByTokenAndStatusTrue(token)).thenReturn(Optional.of(existingDevice));
        when(deviceRepository.save(any(FcmDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FcmDevice result = fcmDeviceService.registerDevice(userId, token);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(deviceRepository).save(existingDevice);
        verify(deviceRepository, times(2)).save(any());
    }

    @Test
    void 기존_토큰이_없을때_새로저장만_진행() {
        // given
        when(deviceRepository.findByTokenAndStatusTrue(token)).thenReturn(Optional.empty());
        when(deviceRepository.save(any(FcmDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        FcmDevice result = fcmDeviceService.registerDevice(userId, token);

        // then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(deviceRepository, times(1)).save(any());
    }

    @Test
    void 토큰이_null_또는_빈값일때_예외발생() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> fcmDeviceService.registerDevice(userId, null));
        assertThrows(IllegalArgumentException.class, () -> fcmDeviceService.registerDevice(userId, ""));
        assertThrows(IllegalArgumentException.class, () -> fcmDeviceService.registerDevice(userId, "   "));

        // 저장소 호출 없어야 함
        verifyNoInteractions(deviceRepository);
    }
}