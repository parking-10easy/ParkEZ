package com.parkez.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Schema(description = "예약 생성 요청 DTO")
@RequiredArgsConstructor
public class ReservationRequest {

    @NotNull(message = "주차공간 id는 필수값입니다.")
    @Schema(description = "주차공간 id", example = "1")
    private Long parkingZoneId;

    @NotNull(message = "시작 날짜 및 시간은 필수값입니다.")
    @Schema(description = "시작 날짜 및 시간", example = "2025.04.07 10:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime startDateTime;

    @NotNull(message = "종료 날짜 및 시간은 필수값입니다.")
    @Schema(description = "종료 날짜 및 시간", example = "2025.04.07 10:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    private LocalDateTime endDateTime;

    @Schema(description = "발급된 쿠폰 ID (선택 값, 없으면 할인 미적용)", example = "1")
    private Long promotionIssueId;

    @Builder
    public ReservationRequest(Long parkingZoneId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        this.parkingZoneId = parkingZoneId;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
    }
}
