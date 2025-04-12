package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewQueryDslRepository {

    @Query("""
        SELECT rv FROM Review rv
        JOIN FETCH rv.reservation r
        JOIN FETCH r.parkingZone pz
        JOIN FETCH pz.parkingLot pl
        WHERE rv.id = :reviewId
          AND pz.deletedAt IS NULL
          AND pl.deletedAt IS NULL
    """)
    Optional<Review> findActiveReviewById(@Param("reviewId") Long reviewId);

    boolean existsByReservation_Id(Long reservationId);

}
