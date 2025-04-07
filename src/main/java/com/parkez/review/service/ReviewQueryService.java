package com.parkez.review.service;

import com.parkez.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    public Set<Long> findReviewedReservationIds(List<Long> reservationIds) {
        return new HashSet<>(reviewRepository.findReviewedReservationIds(reservationIds));
    }

    public boolean isReviewWritten(Long reservationId) {
        return reviewRepository.existsByReservationId(reservationId);
    }
}
