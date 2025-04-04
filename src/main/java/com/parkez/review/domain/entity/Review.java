package com.parkez.review.domain.entity;

import com.parkez.reservation.domain.entity.Reservation;
import com.parkez.review.domain.enums.Rating;
import com.parkez.user.domain.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name="review")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Rating rating;

    private String content;

    @Builder
    private Review(Rating rating, String content, User user, Reservation reservation) {
        this.rating = rating;
        this.content = content;
        this.user = user;
        this.reservation = reservation;
    }
}
