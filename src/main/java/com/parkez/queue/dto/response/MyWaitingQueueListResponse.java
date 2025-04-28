package com.parkez.queue.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyWaitingQueueListResponse {

    private Long reservationId;
    private Long parkingZoneId;
    private String parkingZoneName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer myQueue;

}
