package com.parkez.review.domain.repository;

import com.parkez.review.domain.entity.Review;
import com.parkez.review.enums.ReviewSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewQueryDslRepository {

    Page<Review> findAllByParkingLotIdWithSort(Long parkingLotId, Pageable pageable, ReviewSortType sortType);
}
