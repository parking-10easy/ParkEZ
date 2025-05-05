package com.parkez.reservation.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.parkingzone.domain.enums.ParkingZoneStatus;
import com.parkez.parkingzone.service.ParkingZoneReader;
import com.parkez.promotion.domain.entity.Coupon;
import com.parkez.promotion.domain.entity.PromotionIssue;
import com.parkez.promotion.excption.PromotionIssueErrorCode;
import com.parkez.promotion.service.PromotionIssueReader;
import com.parkez.promotion.service.PromotionIssueValidator;
import com.parkez.promotion.service.PromotionIssueWriter;
import com.parkez.queue.domain.enums.JoinQueueResult;
import com.parkez.queue.exception.QueueErrorCode;
import com.parkez.queue.service.QueueService;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.domain.enums.ReservationStatus;
import com.parkez.reservation.dto.request.ReservationRequest;
import com.parkez.reservation.dto.response.ReservationResponse;
import com.parkez.reservation.exception.ReservationErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.service.UserReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationProcessor {

    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;
    private final UserReader userReader;
    private final ParkingZoneReader parkingZoneReader;
    private final PromotionIssueReader promotionIssueReader;
    private final PromotionIssueValidator promotionIssueValidator;
    private final PromotionIssueWriter promotionIssueWriter;
    private final QueueService queueService;

    @Transactional
    public ReservationResponse create(AuthUser authUser, ReservationRequest request, LocalDateTime now) {
        User user = userReader.getActiveUserById(authUser.getId());
        ParkingZone parkingZone = parkingZoneReader.getActiveByParkingZoneId(request.getParkingZoneId());

        if (!validateRequestTime(request)) {
            throw new ParkingEasyException(ReservationErrorCode.NOT_VALID_REQUEST_TIME);
        }

        if (!parkingZone.getStatus().equals(ParkingZoneStatus.AVAILABLE)) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_RESERVE_UNAVAILABLE_PARKING_ZONE);
        }

        if (!parkingZone.isOpened(request.getStartDateTime(), request.getEndDateTime())) {
            throw new ParkingEasyException(ReservationErrorCode.CANT_RESERVE_AT_CLOSE_TIME);
        }

        List<ReservationStatus> statusList = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

        if (reservationReader.existsReservationByConditionsForUser(parkingZone, request.getStartDateTime(), request.getEndDateTime(), user.getId(), statusList)) {
            throw new ParkingEasyException(ReservationErrorCode.ALREADY_RESERVED_BY_YOURSELF);
        }

        boolean existed = reservationReader.existsReservationByConditions(parkingZone, request.getStartDateTime(), request.getEndDateTime(), statusList);

        if (existed) {
            handleJoinQueue(user, request);
            return null;
        }

        long hours = calculateUsedHour(request.getStartDateTime(), request.getEndDateTime());

        BigDecimal originalPrice = parkingZone.getParkingLotPricePerHour().multiply(BigDecimal.valueOf(hours));
        BigDecimal discountAmount = BigDecimal.ZERO;

        Long promotionIssueId = null;
        if (request.getPromotionIssueId() != null) {
            PromotionIssue promotionIssue = promotionIssueReader.getWithPromotionAndCouponById(request.getPromotionIssueId());

            if (!promotionIssue.isOwnedBy(authUser.getId())) {
                throw new ParkingEasyException(PromotionIssueErrorCode.NOT_YOUR_COUPON);
            }

            promotionIssueId = promotionIssue.getId();
            promotionIssueValidator.validateCanBeUsed(promotionIssue, now);
            Coupon coupon = promotionIssue.getCoupon();
            discountAmount = coupon.calculateDiscount(originalPrice);

            promotionIssueWriter.use(promotionIssue, now);
        }

        BigDecimal finalPrice = originalPrice.subtract(discountAmount);

        Reservation reservation = reservationWriter.create(
                user,
                parkingZone,
                request.getStartDateTime(),
                request.getEndDateTime(),
                originalPrice,
                discountAmount,
                finalPrice,
                promotionIssueId
        );

        return ReservationResponse.from(reservation);
    }

    private boolean validateRequestTime(ReservationRequest request) {
        LocalDateTime startDateTime = request.getStartDateTime();
        LocalDateTime endDateTime = request.getEndDateTime();

        return startDateTime.isBefore(endDateTime)
                && startDateTime.isAfter(LocalDateTime.now())
                && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());
    }

    private long calculateUsedHour(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    private void handleJoinQueue(User user, ReservationRequest request) {
        JoinQueueResult result = queueService.joinWaitingQueue(user.getId(), request);

        if (result == JoinQueueResult.ALREADY_JOINED) {
            throw new ParkingEasyException(QueueErrorCode.ALREADY_IN_QUEUE);
        }
    }

}
