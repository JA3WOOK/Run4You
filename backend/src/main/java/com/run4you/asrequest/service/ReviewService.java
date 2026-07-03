package com.run4you.asrequest.service;

import com.run4you.asrequest.dto.ReviewCreateRequest;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.entity.Review;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.asrequest.repository.ReviewRepository;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AsRequestRepository asRequestRepository;
    private final AssignmentRepository assignmentRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final UserRepository userRepository;

    // 현재 로그인한 유저 조회 (AsRequestService와 동일 패턴)
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public Long createReview(ReviewCreateRequest request) {

        User reviewer = getCurrentUser();

        AsRequest asRequest = asRequestRepository.findById(request.getAsRequestId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 A/S 접수입니다."));

        // 본인 접수인지 확인
        if (!asRequest.getRequester().getId().equals(reviewer.getId())) {
            throw new IllegalStateException("이 A/S 건에 대한 평가 권한이 없습니다.");
        }

        // 중복 작성 방지
        if (reviewRepository.existsByAsRequestId(request.getAsRequestId())) {
            throw new IllegalStateException("이미 평가를 작성한 A/S 건입니다.");
        }

        // 배정된 엔지니어 조회 (프론트 입력 아님 — 서버가 직접 확정)
        Assignment assignment = assignmentRepository
                .findByAsRequestIdWithEngineer(request.getAsRequestId())
                .orElseThrow(() -> new IllegalStateException("배정된 엔지니어가 없습니다."));
        User engineer = assignment.getEngineer();

        Review review = Review.builder()
                .asRequest(asRequest)
                .reviewer(reviewer)
                .engineer(engineer)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // engineer_profiles.rating 갱신
        EngineerProfile profile = engineerProfileRepository.findByUserId(engineer.getId())
                .orElseThrow(() -> new IllegalArgumentException("엔지니어 프로필이 없습니다."));
        profile.updateRating(request.getRating());

        return review.getId();
    }
}