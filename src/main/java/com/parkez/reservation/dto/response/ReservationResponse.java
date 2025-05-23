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
@Schema(description = "사용자 마이페이지 내 예약 내역 응답 DTO")
public class ReservationResponse {

    @Schema(description = "예약 id", example = "1")
    private final Long reservationId;

    @Schema(description = "유저 id", example = "1")
    private final Long userId;

    @Schema(description = "주차공간 id", example = "1")
    private final Long parkingZoneId;

    @Schema(description = "주차장 이름", example = "누리에뜰 주차장")
    private final String parkingLotName;

    @Schema(description = "리뷰 작성 여부", example = "false")
    private final boolean reviewWritten;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    @Schema(description = "예약 시작 날짜 및 시간", example = "2025.04.07 10:00")
    private final LocalDateTime startDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    @Schema(description = "예약 종료 날짜 및 시간", example = "2025.04.07 10:00")
    private final LocalDateTime endDateTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm")
    @Schema(description = "예약 사용 완료 날짜 및 시간", example = "2025.04.07 10:00")
    private final LocalDateTime useCompletionTime;

    @Schema(description = "결제 금액", example = "2000.00")
    private final BigDecimal price;

    @Schema(description = "할인 금액", example = "1000.00")
    private final BigDecimal discountAmount;

    @Schema(description = "예약 금액", example = "3000.00")
    private final BigDecimal originalPrice;

    @Schema(description = "예약 상태", example = "COMPLETED")
    private final ReservationStatus status;

    @Schema(description = "생성 일시", example = "2025.04.07T10:00:00")
    private final LocalDateTime createdAt;

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getParkingZoneId(),
                reservation.getParkingLotName(),
                reservation.isReviewWritten(),
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getUseCompletionTime(),
                reservation.getPrice(),
                reservation.getDiscountAmount(),
                reservation.getOriginalPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }

    public static ReservationResponse of(Reservation reservation, boolean reviewWritten) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getParkingZoneId(),
                reservation.getParkingLotName(),
                reviewWritten,
                reservation.getStartDateTime(),
                reservation.getEndDateTime(),
                reservation.getUseCompletionTime(),
                reservation.getPrice(),
                reservation.getDiscountAmount(),
                reservation.getOriginalPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
