package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("""
                SELECT r.reservation.id
                FROM Review r
                WHERE r.reservation.id IN :reservationIds
            """)
    List<Long> findReviewedReservationIds(@Param("reservationIds") List<Long> reservationIds);

    boolean existsByReservationId(Long reservationId);
}
