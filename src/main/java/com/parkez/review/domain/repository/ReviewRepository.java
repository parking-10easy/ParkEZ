package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByReservation_Id(Long reservationId);
}
