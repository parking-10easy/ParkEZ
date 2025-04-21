package com.parkez.alarm.service;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.domain.enums.AlarmChannel;
import com.parkez.alarm.domain.repository.AlarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlarmSender {

    private final AlarmRepository alarmRepository;
    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String fromMail;

    @Transactional
    public void processAlarms() {
        List<Alarm> pendingAlarms = alarmRepository.findAllBySentFalse();

        for (Alarm alarm : pendingAlarms) {
            try {
                if (alarm.getChannel() == AlarmChannel.EMAIL) {
                    emailService.sendEmail(
                            alarm.getEmailAddress(),
                            fromMail,
                            alarm.getTitle(),
                            alarm.getMessage()
                    );
                    alarm.updateSent(true);
                    alarm.updateSentAt(LocalDateTime.now());
                }

            } catch (Exception e) {
                e.printStackTrace();
                alarm.updateFailReason(e.getMessage());
            }
        }
    }
}
