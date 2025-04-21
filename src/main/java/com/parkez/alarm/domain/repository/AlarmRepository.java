package com.parkez.alarm.domain.repository;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findAllBySentFalse();

    @Query("""
            SELECT COUNT(a) > 0
            FROM Alarm a
            WHERE a.targetId = :targetId
              AND a.targetType = :targetType
              AND a.notificationType = :notificationType
              AND a.channel = :channel
              AND a.sent = true
""")
    boolean existsAlarm(
            @Param("targetId") Long targetId,
            @Param("targetType") AlarmTargetType targetType,
            @Param("notificationType") NotificationType notificationType,
            @Param("channel") AlarmChannel channel
    );
}
