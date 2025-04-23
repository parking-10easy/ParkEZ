package com.parkez.alarm.service.processor;

import com.parkez.alarm.domain.entity.Alarm;
import com.parkez.alarm.service.fcm.PushService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FcmAlarmProcessor {

    private final PushService pushService;

    public void process(Alarm alarm) {
        pushService.sendPush(alarm, alarm.getDeviceToken(), alarm.getTitle(), alarm.getMessage());
    }
}
