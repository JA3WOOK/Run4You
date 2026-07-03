package com.run4you.asrequest.controller;

import com.run4you.asrequest.dto.ReviewCreateRequest;
import com.run4you.asrequest.service.ReviewService;
import com.run4you.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createReview(
            @RequestBody @Valid ReviewCreateRequest request) {

        Long reviewId = reviewService.createReview(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(reviewId, "success"));
    }
}