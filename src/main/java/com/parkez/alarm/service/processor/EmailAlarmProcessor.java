package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.dto.ReservationAlarmInfo;
import com.parkez.alarm.service.email.SesEmailService;
import com.parkez.alarm.service.email.SmtpEmailService;
import com.parkez.alarm.service.templete.EmailTemplateService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailAlarmProcessor {

    private final EmailTemplateService emailTemplateService;
    private final SesEmailService sesEmailService;
    private final SmtpEmailService smtpEmailService;
    private final ReservationReader reservationReader;

    // SMTP 방식
    /*
    @Value("${spring.mail.username}")
    private String fromMail;
    */

    public void processReservation(Alarm alarm) {
        String templateName = resolveTemplateName(alarm);
        Map<String, String> data = prepareTemplateData(alarm);

        String content = emailTemplateService.generateEmailContent(templateName, data);

        // SES
        sesEmailService.sendEmail(alarm.getEmailAddress(), alarm.getTitle(), content);

        // SMTP 방식
        // smtpEmailService.sendEmail(alarm.getEmailAddress(),fromMail,alarm.getTitle(),alarm.getMessage());

    }

    public void processPayment(ReservationAlarmInfo reservationAlarmInfo, NotificationType notificationType) {
        String templateName = "payment_notification.html";

        String title = String.format("[결제] %d번 예약 결제 %s",
                reservationAlarmInfo.getReservationId(),
                notificationType == NotificationType.CANCELED ? "취소" : "실패");

        Map<String, String> data = Map.of(
                "userName", reservationAlarmInfo.getUserName(),
                "reservationId", reservationAlarmInfo.getReservationId().toString(),
                "parkingLotName", reservationAlarmInfo.getParkingLotName(),
                "parkingZoneName", reservationAlarmInfo.getParkingZoneName(),
                "startTime", reservationAlarmInfo.getStartDateTime().toString(),
                "endTime", reservationAlarmInfo.getEndDateTime().toString(),
                "now", LocalDateTime.now().toString(),
                "message", notificationType == NotificationType.CANCELED ? "결제가 취소되었습니다." : "결제가 실패했습니다.",
                "additionalGuide", "다른 예약이 필요하시면 ParkEZ를 다시 이용해주세요."
        );

        String content = emailTemplateService.generateEmailContent(templateName, data);

        sesEmailService.sendEmail(reservationAlarmInfo.getUserEmail(), title, content);
    }

    private String resolveTemplateName(Alarm alarm) {
        validateReservationAlarm(alarm);
        return "reservation_notification.html";
    }

    private Map<String, String> prepareTemplateData(Alarm alarm) {
        validateReservationAlarm(alarm);

        Reservation reservation = reservationReader.findReservation(alarm.getTargetId());

        return Map.of(
                "userName", reservation.getUserNickName(),
                "parkingLotName", reservation.getParkingLotName(),
                "parkingZoneName", reservation.getParkingZoneName(),
                "startTime", reservation.getStartDateTime().toString(),
                "endTime", reservation.getEndDateTime().toString(),
                "statusMessage", alarm.getNotificationType() == NotificationType.UPCOMING ? "예약 종료 10분 전" : "예약 만료",
                "customMessage", alarm.getNotificationType() == NotificationType.UPCOMING ? "🚗 출차 준비 부탁드립니다." : "🚗 출차 부탁드립니다."
        );
    }

    private void validateReservationAlarm(Alarm alarm) {
        if (alarm.getTargetType() != AlarmTargetType.RESERVATION) {
            throw new IllegalArgumentException("해당 기능은 예약 알림만 지원합니다.");
        }
    }
}
