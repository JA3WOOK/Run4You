package com.run4you.education.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.education.dto.*;
import com.run4you.education.service.EngineerEducationService;
import com.run4you.lms.entity.CourseLevel;
import com.run4you.lms.entity.ManualType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 엔지니어 교육(LMS) API
 *
 * GET  /api/engineer/courses                      — 코스 목록 (등급 필터, 내 진도율 포함)
 * GET  /api/engineer/courses/{courseId}            — 코스 상세 (차시 목록 + 내 진도, 자동 수강신청)
 * POST /api/engineer/lessons/{lessonId}/progress   — 차시 시청 진도 갱신
 * GET  /api/engineer/courses/{courseId}/exam       — 시험 문항 조회 (진도율 100% 게이트)
 * POST /api/engineer/exams/{examId}/submit         — 시험 제출/채점 (합격 시 등급 상향)
 * GET  /api/engineer/manuals                       — 매뉴얼 목록 조회
 *
 * 인증: JwtAuthenticationFilter가 principal에 이메일(String)을 저장하는 기존 컨벤션을 따른다.
 */
@RestController
@RequestMapping("/api/engineer")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ENGINEER')")
public class EngineerEducationController {

    private final EngineerEducationService educationService;

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseListItemResponse>>> getCourses(
            @AuthenticationPrincipal String email,
            @RequestParam(required = false) CourseLevel level
    ) {
        return ResponseEntity.ok(ApiResponse.success(educationService.getCourses(email, level)));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<ApiResponse<CourseDetailResponse>> getCourseDetail(
            @AuthenticationPrincipal String email,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(educationService.getCourseDetail(email, courseId)));
    }

    @PostMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<ApiResponse<LessonProgressResponse>> updateLessonProgress(
            @AuthenticationPrincipal String email,
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonProgressUpdateRequest request
    ) {
        LessonProgressResponse result = educationService.updateLessonProgress(
                email, lessonId, request.getWatchedSeconds());
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/courses/{courseId}/exam")
    public ResponseEntity<ApiResponse<ExamResponse>> getExam(
            @AuthenticationPrincipal String email,
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(ApiResponse.success(educationService.getExam(email, courseId)));
    }

    @PostMapping("/exams/{examId}/submit")
    public ResponseEntity<ApiResponse<ExamResultResponse>> submitExam(
            @AuthenticationPrincipal String email,
            @PathVariable Long examId,
            @Valid @RequestBody ExamSubmitRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(educationService.submitExam(email, examId, request)));
    }

    @GetMapping("/manuals")
    public ResponseEntity<ApiResponse<List<ManualResponse>>> getManuals(
            @RequestParam(required = false) ManualType manualType,
            @RequestParam(required = false) String faultCategory
    ) {
        return ResponseEntity.ok(ApiResponse.success(educationService.getManuals(manualType, faultCategory)));
    }
}
