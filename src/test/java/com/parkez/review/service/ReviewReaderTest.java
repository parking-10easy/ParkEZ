package com.parkez.review.service;

import com.parkez.common.exception.ParkingEasyException;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.review.domain.entity.Review;
import com.parkez.review.domain.repository.ReviewRepository;
import com.parkez.review.dto.response.ReviewResponse;
import com.parkez.review.enums.ReviewSortType;
import com.parkez.review.exception.ReviewErrorCode;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewReaderTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewReader reviewReader;

    private User getUser() {
        User user = User.builder()
                .email("user@test.com")
                .nickname("A유저")
                .role(UserRole.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private User getSecondUser() {
        User user = User.builder()
                .email("user2@test.com")
                .nickname("B유저")
                .role(UserRole.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 2L);
        return user;
    }

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .name("Main Parking Lot")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    private Reservation getReservation() {
        Reservation reservation = Reservation.builder()
                .parkingLotName("A 주차장")
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
        return reservation;
    }

    private Reservation getSecondReservation() {
        Reservation reservation = Reservation.builder()
                .parkingLotName("B 주차장")
                .build();
        ReflectionTestUtils.setField(reservation, "id", 2L);
        return reservation;
    }

    private Review getReview() {
        User user = getUser();
        Reservation reservation = getReservation();
        Review review = Review.builder()
                .rating(5)
                .content("좋아요")
                .reservation(reservation)
                .user(user)
                .build();
        ReflectionTestUtils.setField(review, "id", 1L);
        return review;
    }

    private Review getSecondReview() {
        User user = getSecondUser();
        Reservation reservation = getReservation();
        Review review = Review.builder()
                .rating(3)
                .content("보통이에요")
                .reservation(getSecondReservation())
                .user(user)
                .build();
        ReflectionTestUtils.setField(review, "id", 2L);
        return review;
    }

    @Nested
    class GetReviews {
        @Test
        void 리뷰_다건조회_특정_주차장에_대한_리뷰를_정상적으로_다건조회할_수_있다() {
            // given
            ParkingLot parkingLot = getParkingLot();
            Pageable pageable = PageRequest.of(0,10);
            ReviewSortType sortType = ReviewSortType.LATEST;

            Review firstReview = getReview();
            Review secondReview = getSecondReview();

            Page<Review> mockPage = new PageImpl<>(List.of(firstReview, secondReview), pageable, 2);

            when(reviewRepository.findAllByParkingLotIdWithSort(anyLong(),any(),any())).thenReturn(mockPage);

            // when
            Page<ReviewResponse> result = reviewReader.getReviews(parkingLot.getId(),1,10,sortType);

            // then
            assertThat(result).extracting("id","reservationId","nickName","rating","content")
                    .contains(tuple(1L,1L,"A유저",5,"좋아요"),
                              tuple(2L,2L,"B유저",3,"보통이에요"));
        }
    }

    @Nested
    class GetReview {
        @Test
        void 리뷰_단건조회_특정_리뷰를_정상적으로_단건조회할_수_있다() {
            // given
            Review review = getReview();
            when(reviewRepository.findActiveReviewById(anyLong())).thenReturn(Optional.of(review));

            // when
            Review result = reviewReader.getReviewById(review.getId());

            // then
            assertThat(result).extracting("id","reservationId","rating","content")
                    .containsExactly(1L,1L,5,"좋아요");
        }

        @Test
        void 리뷰_단건조회_존재하지_않는_리뷰를_조회하면_REVIEW_NOT_FOUND_예외가_발생한다() {
            // given
            Long reviewId = -1L;
            when(reviewRepository.findActiveReviewById(anyLong())).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> reviewReader.getReviewById(reviewId))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.REVIEW_NOT_FOUND.getDefaultMessage());
        }
    }

    @Nested
    class IsReviewWritten {
        @Test
        void 리뷰_기존_작성여부_확인_특정_예약에_대한_리뷰가_이미_작성되었는지_확인할_수_있다() {
            // given
            Reservation reservation = getReservation();
            Review review = getReview();
            when(reviewRepository.existsByReservation_Id(anyLong())).thenReturn(true);

            // when
            Boolean result = reviewReader.isReviewWritten(reservation.getId());

            // then
            assertThat(result).isEqualTo(true);
        }
    }
}