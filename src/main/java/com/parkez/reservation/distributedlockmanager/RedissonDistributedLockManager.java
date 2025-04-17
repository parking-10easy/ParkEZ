package com.parkez.reservation.distributedlockmanager;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.reservation.exception.ReservationErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedissonDistributedLockManager implements DistributedLockManager {

    private final RedissonClient redissonClient;
    private static final String LOCK_KEY_PREFIX = "distributed-lock:";
    private static final long WAIT_TIME = 5L;
    private static final long LEASE_TIME = -1L;

    @Override
    public <T> T executeWithLock(Long key, Callable<T> task) {
        String lockKey = LOCK_KEY_PREFIX + key;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 5초 내로 락 획득 시도, -1초 후 자동으로 락 해제(수동으로만 락 해제 가능하도록 설정)
            if (!lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS)) {
                throw new ParkingEasyException(ReservationErrorCode.RESERVATION_LOCK_FAILED);
            }
            return task.call();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ParkingEasyException(ReservationErrorCode.RESERVATION_LOCK_INTERRUPTED);

        } catch (Exception e) {
            if (e instanceof ParkingEasyException pe) {
                throw pe;
            }
            log.error("executeWithLock error : {}", e.getMessage());
            throw new ParkingEasyException(ReservationErrorCode.UNKNOWN_ERROR);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 작업 완료 후 락 해제
            }
        }
    }
}
