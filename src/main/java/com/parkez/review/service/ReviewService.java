package com.parkez.review.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.review.dto.request.ReviewCreateRequest;
import com.parkez.review.dto.response.ReviewResponse;
import com.parkez.review.enums.ReviewSortType;
import com.parkez.review.exception.ReviewErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewWriter reviewWriter;
    private final ReviewReader reviewReader;
    private final ReservationReader reservationReader;
    private final ParkingLotReader parkingLotReader;

    public ReviewResponse createReview(AuthUser authUser, ReviewCreateRequest request) {
        Reservation reservation = reservationReader.findReservation(authUser.getId(), request.getReservationId());

        boolean reviewWritten = reviewReader.isReviewWritten(request.getReservationId());
        if (reviewWritten){
            throw new ParkingEasyException(ReviewErrorCode.ALREADY_REVIEWED);
        }
        return ReviewResponse.from(reviewWriter.createReview(reservation, request.getRating(), request.getContent()));
    }

    public Page<ReviewResponse> getReviews(Long parkingLotId, PageRequest pageRequest, ReviewSortType sortType) {
        parkingLotReader.validateExistence(parkingLotId);
        return reviewReader.getReviews(parkingLotId, pageRequest.getPage(), pageRequest.getSize(), sortType);
    }
}
