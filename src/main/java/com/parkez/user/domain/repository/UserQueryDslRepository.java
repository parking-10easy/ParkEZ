package com.parkez.user.domain.repository;

import com.parkez.user.domain.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;
import java.util.List;

public interface UserQueryDslRepository {

    List<User> findOwnersForSettlementByMonth(YearMonth yearMonth, Pageable pageable);
}
