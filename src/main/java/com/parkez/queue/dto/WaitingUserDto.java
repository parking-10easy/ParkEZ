package com.parkez.queue.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WaitingUserDto{

    private Long userId; // 대기자 ID

    private Long parkingZoneId;

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public WaitingUserDto(
            Long userId,
            Long parkingZoneId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        this.userId = userId;
        this.parkingZoneId = parkingZoneId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }

}
