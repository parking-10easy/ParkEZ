package com.parkez.queue.redis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QueueKey {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    public static String generateKey(Long parkingZoneId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        String start = startDateTime.format(FORMATTER);
        String end = endDateTime.format(FORMATTER);
        return String.format("reservation:queue:%d:%s-%s", parkingZoneId, start, end);
    }


}
