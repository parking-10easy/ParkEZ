package com.parkez.reservation.distributedlockmanager;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.reservation.exception.ReservationErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RedissonDistributedLockManagerTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    @InjectMocks
    private RedissonDistributedLockManager redissonDistributedLockManager;

    @BeforeEach
    void setUp() {
        redissonDistributedLockManager = new RedissonDistributedLockManager(redissonClient);
    }

    @Test
    void 락을_획득하고_정상적으로_실행되면_결과_반환() throws InterruptedException {
        // given
        Long key = 1L;
        String expectedResult = "Success";
        Callable<String> task = () -> expectedResult;

        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);

        // when
        String result = redissonDistributedLockManager.executeWithLock(key, task);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(lock).unlock();
    }

    @Test
    void 락을_획득하지_못하면_RESERVATION_LOCK_FAILED_예외() throws InterruptedException {
        // given
        Long key = 1L;
        String expectedResult = "Success";
        Callable<String> task = () -> expectedResult;

        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> redissonDistributedLockManager.executeWithLock(key, task));
        assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_LOCK_FAILED);
    }

    @Test
    void Interrupt_발생시_RESERVATION_LOCK_INTERRUPTED_예외() throws InterruptedException {
        // given
        Long key = 1L;
        String expectedResult = "Success";
        Callable<String> task = () -> expectedResult;

        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willThrow(new InterruptedException());

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> redissonDistributedLockManager.executeWithLock(key, task));
        assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_LOCK_INTERRUPTED);
    }

    @Test
    void 예외_발생_시_해당_예외가_ParkingEasyException에서_처리하는_예외라면_ParkingEasyException_반환() throws InterruptedException {
        // given
        Long key = 1L;
        ParkingEasyException expectedException = new ParkingEasyException(ReservationErrorCode.RESERVATION_LOCK_FAILED);
        Callable<String> task = () -> {
            throw expectedException;
        };

        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> redissonDistributedLockManager.executeWithLock(key, task));
        assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.RESERVATION_LOCK_FAILED);
    }

    @Test
    void Custom_Error_Response로_처리되지_않은_예외_발생_시_UNKNOWN_ERROR_예외() throws Exception {
        // given
        Long key = 1L;
        Callable<String> task = () -> {
            throw new RuntimeException();
        };

        given(redissonClient.getLock(anyString())).willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);

        // when & then
        ParkingEasyException exception = assertThrows(ParkingEasyException.class,
                () -> redissonDistributedLockManager.executeWithLock(key, task));
        assertThat(exception.getErrorCode()).isEqualTo(ReservationErrorCode.UNKNOWN_ERROR);
    }
}