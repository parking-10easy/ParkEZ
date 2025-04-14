package com.parkez.review.service;

import com.parkez.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
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
}
