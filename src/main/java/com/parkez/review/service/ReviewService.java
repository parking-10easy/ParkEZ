package com.parkez.review.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewCommandService reviewCommandService;
    private final ReviewQueryService reviewQueryService;
}
