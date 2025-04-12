package com.parkez.review.service;

import com.parkez.common.principal.AuthUser;
import com.parkez.parkinglot.domain.entity.ParkingLot;
import com.parkez.parkingzone.domain.entity.ParkingZone;
import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.review.domain.entity.Review;
import com.parkez.review.domain.repository.ReviewRepository;
import com.parkez.review.dto.request.ReviewCreateRequest;
import com.parkez.user.domain.entity.User;
import com.parkez.user.domain.enums.UserRole;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewWriterTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewWriter reviewWriter;

    private AuthUser getAuthUser() {
        return AuthUser.builder()
                .id(1L)
                .email("user@test.com")
                .roleName(UserRole.ROLE_USER.name())
                .nickname("테스트 유저")
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

    private User getOwner() {
        User owner = User.builder()
                .email("owner@test.com")
                .nickname("테스트 소유자")
                .role(UserRole.ROLE_OWNER)
                .build();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return owner;
    }

    private ParkingLot getParkingLot() {
        ParkingLot parkingLot = ParkingLot.builder()
                .owner(getOwner())
                .name("Main Parking Lot")
                .build();
        ReflectionTestUtils.setField(parkingLot, "id", 1L);
        return parkingLot;
    }

    private ParkingZone getParkingZone() {
        ParkingLot parkingLot = getParkingLot();
        ParkingZone parkingZone = ParkingZone.builder()
                .name("A구역")
                .imageUrl("http://example.com/image.jpg")
                .parkingLot(parkingLot)
                .build();
        ReflectionTestUtils.setField(parkingZone, "id", 1L);
        return parkingZone;
    }

    private Reservation getReservation() {
        Reservation reservation = Reservation.builder()
                .parkingZone(getParkingZone())
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

    @Nested
    class CreateReview {
        @Test
        void 리뷰_생성_특정_예약에_대한_리뷰를_정상적으로_생성할_수_있다() {
            // given
            Reservation reservation = getReservation();
            Review review = getReview();
            ReviewCreateRequest createRequest = getCreateRequest();

            when(reviewRepository.save(any(Review.class))).thenReturn(review);

            // when
            Review result = reviewWriter.createReview(reservation,createRequest.getRating(),createRequest.getContent());

            // then
            assertThat(result).extracting("id","reservationId","rating","content")
                    .containsExactly(1L,1L,5,"좋아요");
        }
    }

    @Nested
    class DeleteReview {
        @Test
        void 리뷰_삭제_특정_리뷰를_정상적으로_삭제할_수_있다() {
            // given
            Reservation reservation = getReservation();
            Review review = getReview();

            doNothing().when(reviewRepository).deleteById(anyLong());

            // when
            reviewWriter.deleteReview(review);

            // then
            verify(reviewRepository).deleteById(eq(review.getId()));
        }
    }
}