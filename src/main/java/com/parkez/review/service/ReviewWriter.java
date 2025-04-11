package com.parkez.review.service;

import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.review.domain.entity.Review;
import com.parkez.review.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewWriter {

    private final ReviewRepository reviewRepository;

    public Review createReview(Reservation reservation, Integer rating, String content) {
        Review review = Review.builder()
                .user(reservation.getUser())
                .reservation(reservation)
                .rating(rating)
                .content(content)
                .build();

        // 리뷰 수 증가
        ParkingZone parkingZone = reservation.getParkingZone();
        parkingZone.incrementReviewCount();

        return reviewRepository.save(review);
    }

    public void deleteReviewById(Review review) {
        // 리뷰 수 감소
        Reservation reservation = review.getReservation();
        ParkingZone parkingZone = reservation.getParkingZone();
        parkingZone.decrementReviewCount();

        reviewRepository.deleteById(review.getId());
    }
}
