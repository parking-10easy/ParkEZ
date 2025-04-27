package com.parkez.user.domain.repository;

import com.parkez.user.domain.entity.User;

import java.time.YearMonth;
import java.util.List;

public interface UserQueryDslRepository {

    List<User> findAllOwnersForSettlementByMonth(YearMonth yearMonth, Long lastId, int size);
}
