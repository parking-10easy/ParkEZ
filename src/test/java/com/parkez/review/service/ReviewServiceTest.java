package com.parkez.review.service;

import com.parkez.common.dto.request.PageRequest;
import com.parkez.common.exception.ParkingEasyException;
import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.exception.ParkingLotErrorCode;
import com.parkez.parkinglot.service.ParkingLotReader;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.reservation.service.ReservationReader;
import com.parkez.review.domain.entity.Review;
import com.parkez.review.dto.request.ReviewCreateRequest;
import com.parkez.review.dto.request.ReviewUpdateRequest;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.tuple;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewWriter reviewWriter;

    @Mock
    private ReviewReader reviewReader;

    @Mock
    private ReservationReader reservationReader;

    @Mock
    private ParkingLotReader parkingLotReader;

    @InjectMocks
    private ReviewService reviewService;

    private AuthUser getAuthUser() {
        return AuthUser.builder()
                .id(1L)
                .email("user@test.com")
                .roleName(UserRole.ROLE_USER.name())
                .nickname("테스트 유저")
                .build();
    }

    private AuthUser getSecondAuthUser() {
        return AuthUser.builder()
                .id(2L)
                .email("user2@test.com")
                .roleName(UserRole.ROLE_USER.name())
                .nickname("테스트 유저2")
                .build();
    }

    private User getUser() {
        User user = User.builder()
                .email("user@test.com")
                .nickname("테스트 유저")
                .role(UserRole.ROLE_USER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private Reservation getReservation() {
        Reservation reservation = Reservation.builder()
                .parkingLotName("A 주차장")
                .build();
        ReflectionTestUtils.setField(reservation, "id", 1L);
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

    private ReviewCreateRequest getCreateRequest() {
        return ReviewCreateRequest.builder()
                .reservationId(1L)
                .rating(5)
                .content("좋아요")
                .build();
    }

    private Page<ReviewResponse> getReviewResponses() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 4, 11, 22, 6, 14, 571433000);

        List<ReviewResponse> list = List.of(
            new ReviewResponse(1L,1L, "A유저",5, "좋아요",dateTime,dateTime),
            new ReviewResponse(2L,2L, "B유저",3, "보통이에요",dateTime,dateTime)
        );

        return new PageImpl<>(list);
    }

    private ReviewUpdateRequest getUpdateRequest() {
        return ReviewUpdateRequest.builder()
                .rating(4)
                .content("괜찮아요")
                .build();
    }

    @Nested
    class CreateReview {
        @Test
        void 리뷰_생성_특정_예약에_대한_리뷰를_정상적으로_생성할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            Reservation reservation = getReservation();
            Review review = getReview();
            ReviewCreateRequest createRequest = getCreateRequest();

            when(reservationReader.findMyReservation(anyLong(),anyLong())).thenReturn(reservation);
            when(reviewReader.isReviewWritten(anyLong())).thenReturn(false);
            when(reviewWriter.createReview(any(Reservation.class), anyInt(), anyString())).thenReturn(review);

            // when
            ReviewResponse result = reviewService.createReview(authUser, createRequest);

            // then
            assertThat(result).extracting("id","reservationId","rating","content")
                    .containsExactly(1L,1L,5,"좋아요");
        }

        @Test
        void 리뷰_생성_특정_예약에_대해_이미_작성한_리뷰를_생성하면_ReviewErrorCode_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            ReviewCreateRequest createRequest = getCreateRequest();
            Reservation reservation = getReservation();

            given(reservationReader.findMyReservation(anyLong(), anyLong())).willReturn(reservation);
            given(reviewReader.isReviewWritten(anyLong())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> reviewService.createReview(authUser, createRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.ALREADY_REVIEWED.getDefaultMessage());
        }
    }

    @Nested
    class GetReviews {
        @Test
        void 리뷰_다건조회_특정_주차장에_대한_리뷰를_정상적으로_다건조회할_수_있다() {
            // given
            Long parkingLotId = 1L;
            ReviewSortType sortType = ReviewSortType.LATEST;
            Page<ReviewResponse> reviewResponses = getReviewResponses();

            doNothing().when(parkingLotReader).validateExistence(parkingLotId);
            when(reviewReader.getReviews(anyLong(),anyInt(),anyInt(),any())).thenReturn(reviewResponses);

            // when
            PageRequest pageRequest = new PageRequest(1,10);
            Page<ReviewResponse> result = reviewService.getReviews(parkingLotId, pageRequest, sortType);

            // then
            assertThat(result).extracting("id","reservationId","nickName","rating","content")
                    .contains(tuple(1L,1L,"A유저",5,"좋아요"),
                              tuple(2L,2L,"B유저",3,"보통이에요"));
        }

        @Test
        void 주차공간_다건조회_존재하지_않는_주차장_아이디로_조회하면_NOT_FOUND_예외가_발생한다() {
            // given
            Long INVALID_PARKING_LOT_ID = -1L;
            ReviewSortType sortType = ReviewSortType.LATEST;
            doThrow(new ParkingEasyException(ParkingLotErrorCode.NOT_FOUND))
                    .when(parkingLotReader).validateExistence(anyLong());

            // when & then
            PageRequest pageRequest = new PageRequest(1,10);
            assertThatThrownBy(() -> reviewService.getReviews(INVALID_PARKING_LOT_ID,pageRequest,sortType))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ParkingLotErrorCode.NOT_FOUND.getDefaultMessage());
        }
    }

    @Nested
    class GetReview {
        @Test
        void 리뷰_단건조회_특정_리뷰를_정상적으로_단건조회할_수_있다() {
            // given
            Review review = getReview();
            given(reviewReader.getReviewById(anyLong())).willReturn(review);

            // when
            ReviewResponse result = reviewService.getReview(review.getId());

            // then
            assertThat(result).extracting("id","reservationId","rating","content")
                    .containsExactly(1L,1L,5,"좋아요");
        }
    }

    @Nested
    class UpdateReview {
        @Test
        void 리뷰_수정_특정_리뷰를_정상적으로_수정할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            Review review = getReview();
            ReviewUpdateRequest updateRequest = getUpdateRequest();

            given(reviewReader.getReviewById(anyLong())).willReturn(review);

            // when
            reviewService.updateReview(authUser,review.getId(),updateRequest);

            // then
            assertThat(review.getRating()).isEqualTo(updateRequest.getRating());
            assertThat(review.getContent()).isEqualTo(updateRequest.getContent());
        }

        @Test
        void 리뷰_수정_존재하지_않는_리뷰를_수정하면_REVIEW_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            Review review = getReview();
            ReviewUpdateRequest updateRequest = getUpdateRequest();

            given(reviewReader.getReviewById(anyLong())).willThrow(
                    new ParkingEasyException(ReviewErrorCode.REVIEW_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(authUser, review.getId(), updateRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.REVIEW_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 리뷰_수정_소유자_본인이_아니면_NOT_REVIEW_OWNER_예외가_발생한다() {
            // given
            AuthUser nonOwner = getSecondAuthUser();
            Review review = getReview();
            ReviewUpdateRequest updateRequest = getUpdateRequest();

            given(reviewReader.getReviewById(anyLong())).willReturn(review);

            // when & then
            assertThatThrownBy(() -> reviewService.updateReview(nonOwner, review.getId(), updateRequest))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.NOT_REVIEW_OWNER.getDefaultMessage());
        }
    }

    @Nested
    class DeleteReview {
        @Test
        void 리뷰_삭제_특정_리뷰를_정상적으로_삭제할_수_있다() {
            // given
            AuthUser authUser = getAuthUser();
            Review review = getReview();

            given(reviewReader.getReviewById(anyLong())).willReturn(review);

            // when
            reviewService.deleteReview(authUser, review.getId());

            // then
            verify(reviewWriter, times(1)).deleteReview(review);
        }

        @Test
        void 리뷰_삭제_존재하지_않는_리뷰를_삭제하면_REVIEW_NOT_FOUND_예외가_발생한다() {
            // given
            AuthUser authUser = getAuthUser();
            Review review = getReview();

            given(reviewReader.getReviewById(anyLong())).willThrow(
                    new ParkingEasyException(ReviewErrorCode.REVIEW_NOT_FOUND)
            );

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(authUser, review.getId()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.REVIEW_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 리뷰_삭제_소유자_본인이_아니면_NOT_REVIEW_OWNER_예외가_발생한다() {
            // given
            AuthUser nonOwner = getSecondAuthUser();
            Review review = getReview();

            given(reviewReader.getReviewById(anyLong())).willReturn(review);

            // when & then
            assertThatThrownBy(() -> reviewService.deleteReview(nonOwner, review.getId()))
                    .isInstanceOf(ParkingEasyException.class)
                    .hasMessage(ReviewErrorCode.NOT_REVIEW_OWNER.getDefaultMessage());
        }
    }
}