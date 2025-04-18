package com.parkez.alarm.domain.entity;

import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Alarm extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long targetId;

    @Enumerated(EnumType.STRING)
    private AlarmTargetType targetType;

    @Enumerated(EnumType.STRING)
    private AlarmChannel channel;

    private String title;
    private String message;

    private String deviceToken;    // FCM
    private String emailAddress;   // 이메일

    private boolean sent;          // 전송 여부

    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;

    private LocalDateTime sentAt;  // 전송된 시간

    private String failReason;     // 실패 시 로그용

    public static Alarm createEmailAlarm(Long userId, Long reservationId, AlarmTargetType alarmTargetType, String email, String title, String message, NotificationType notificationType) {
        return Alarm.builder()
                .userId(userId)
                .targetId(reservationId)
                .targetType(alarmTargetType)
                .channel(AlarmChannel.EMAIL)
                .title(title)
                .message(message)
                .emailAddress(email)
                .sent(false)
                .notificationType(notificationType)
                .build();
    }

    public static Alarm createFcmAlarm(Long userId, Long reservationId, AlarmTargetType alarmTargetType, String token, String title, String message, NotificationType notificationType) {
        return Alarm.builder()
                .userId(userId)
                .targetId(reservationId)
                .targetType(alarmTargetType)
                .channel(AlarmChannel.FCM)
                .title(title)
                .message(message)
                .deviceToken(token)
                .sent(false)
                .notificationType(notificationType)
                .build();
    }

    public void updateSent(boolean sent) {
        this.sent = sent;
    }

    public void updateSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public void updateFailReason(String failReason) {
        this.failReason= failReason;
    }
}
