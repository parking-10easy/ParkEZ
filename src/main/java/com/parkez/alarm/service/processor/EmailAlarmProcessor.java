package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmTargetType;
import com.parkez.alarm.domain.enums.NotificationType;
import com.parkez.alarm.service.email.SesEmailService;
import com.parkez.alarm.service.email.SmtpEmailService;
import com.parkez.alarm.service.templete.EmailTemplateService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.dto.request.ReservationCancelRequest;
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

    public void process(Alarm alarm) {
        String templateName = resolveTemplateName(alarm);
        Map<String, String> data = prepareTemplateData(alarm);

        String content = emailTemplateService.generateEmailContent(templateName, data);

        // SES
        sesEmailService.sendEmail(
                alarm.getEmailAddress(),
                alarm.getTitle(),
                content
        );

        // SMTP 방식
        /*
        smtpEmailService.sendEmail(
                alarm.getEmailAddress(),
                fromMail,
                alarm.getTitle(),
                alarm.getMessage()
        );
        */
    }

    private String resolveTemplateName(Alarm alarm) {
        if (alarm.getTargetType() == AlarmTargetType.RESERVATION) {
            return "reservation_notification.html";
        } else if (alarm.getTargetType() == AlarmTargetType.PAYMENT) {
            return "payment_notification.html";
        }
        throw new IllegalArgumentException("Unknown alarm type");
    }

    private Map<String, String> prepareTemplateData(Alarm alarm) {
        Reservation reservation = reservationReader.findReservation(alarm.getTargetId());

        if (alarm.getTargetType() == AlarmTargetType.RESERVATION) {
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

        if (alarm.getTargetType() == AlarmTargetType.PAYMENT) {
            return Map.of(
                    "userName", reservation.getUserNickName(),
                    "reservationId", reservation.getId().toString(),
                    "parkingLotName", reservation.getParkingLotName(),
                    "parkingZoneName", reservation.getParkingZoneName(),
                    "startTime", reservation.getStartDateTime().toString(),
                    "endTime", reservation.getEndDateTime().toString(),
                    "now", LocalDateTime.now().toString(),
                    "message", alarm.getNotificationType() == NotificationType.CANCELED ? "결제가 취소되었습니다." : "결제가 실패했습니다.",
                    "additionalGuide", "다른 예약이 필요하시면 ParkEZ를 다시 이용해주세요."
            );
        }

        throw new IllegalArgumentException("Unsupported alarm type");
    }
}
