package com.parkez.reservation.distributedlockmanager;

import java.util.concurrent.Callable;

public interface DistributedLockManager {

    <T> T executeWithLock(Long key, Callable<T> task);
}
