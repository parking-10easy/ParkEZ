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

        // 예약 리뷰 작성 여부 수정
        reservation.writeReview();

        return reviewRepository.save(review);
    }

    public void updateReview(Review review, Integer rating, String content) {
        review.update(rating, content);
    }

    public void deleteReview(Review review) {
        // 리뷰 수 감소
        Reservation reservation = review.getReservation();
        ParkingZone parkingZone = reservation.getParkingZone();
        parkingZone.decrementReviewCount();

        reviewRepository.deleteById(review.getId());
    }
}
