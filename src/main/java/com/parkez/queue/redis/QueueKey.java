package com.parkez.queue.redis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueueKey {

    private static final String QUEUE_KEY = "reservation:queue:%d:%s-%s";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static String generateKey(Long parkingZoneId, LocalDateTime reservationStartDateTime, LocalDateTime reservationEndDateTime) {
        String start = reservationStartDateTime.format(FORMATTER);
        String end = reservationEndDateTime.format(FORMATTER);
        return String.format(QUEUE_KEY, parkingZoneId, start, end);
    }
}
