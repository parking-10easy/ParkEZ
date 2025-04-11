package com.parkez.review.service;

import com.parkez.review.domain.entity.Review;
import com.parkez.review.domain.repository.ReviewRepository;
import com.parkez.review.dto.response.ReviewResponse;
import com.parkez.review.enums.ReviewSortType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewReader {

    private final ReviewRepository reviewRepository;

    public boolean isReviewWritten(Long reservationId) {
        return reviewRepository.existsByReservation_Id(reservationId);
    }

    public Page<ReviewResponse> getReviews(Long parkingLotId, int page, int size, ReviewSortType sortType) {
        Pageable pageable = PageRequest.of(page - 1, size);

        Page<Review> reviews = reviewRepository.findAllByParkingLotIdWithSort(parkingLotId, pageable, sortType);

        return reviews.map(ReviewResponse::from);
    }
}
