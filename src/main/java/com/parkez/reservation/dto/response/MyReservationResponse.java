package com.parkez.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class MyReservationResponse {

    @Schema(description = "예약 id", example = "1")
    private final Long ReservationId;

    @Schema(description = "유저 id", example = "1")
    private final Long userId;

    @Schema(description = "주차공간 id", example = "1")
    private final Long parkingZoneId;

    @Schema(description = "주차장 이름", example = "누리에뜰 주차장")
    private final String parkingLotName;

    @Schema(description = "리뷰 작성 여부", example = "false")
    private final boolean isReviewWritten;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    @Schema(description = "예약 시작 날짜 및 시간", example = "2025.04.07 10:00")
    private final LocalDateTime startDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    @Schema(description = "예약 종료 날짜 및 시간", example = "2025.04.07 10:00")
    private final LocalDateTime endDateTime;

    @Schema(description = "예약 총 가격", example = "3000.00")
    private final BigDecimal price;

    @Schema(description = "예약 상태", example = "COMPLETED")
    private final ReservationStatus status;

    @Schema(description = "생성 일시", example = "2025.04.07T10:00:00")
    private final LocalDateTime createdAt;

    public static MyReservationResponse of(Reservation reservation, boolean isReviewWritten) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getParkingZone().getId(),
                reservation.getParkingLotName(),
                isReviewWritten,
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
