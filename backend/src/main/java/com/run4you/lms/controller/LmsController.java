package com.run4you.lms.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.lms.dto.*;
import com.run4you.lms.service.LmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lms")
@RequiredArgsConstructor
public class LmsController {

    private final LmsService lmsService;

    // ── 코스 ──

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<CourseResponse>>> getCourses() {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getCourses()));
    }

    @GetMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<CourseResponse>> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getCourse(id)));
    }

    @PostMapping("/courses")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.createCourse(request)));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id, @Valid @RequestBody CourseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.updateCourse(id, request)));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        lmsService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── 차시 ──

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<ApiResponse<List<LessonResponse>>> getLessons(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getLessons(courseId)));
    }

    @PostMapping("/courses/{courseId}/lessons")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> createLesson(
            @PathVariable Long courseId, @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.createLesson(courseId, request)));
    }

    @PutMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<LessonResponse>> updateLesson(
            @PathVariable Long lessonId, @Valid @RequestBody LessonRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.updateLesson(lessonId, request)));
    }

    @DeleteMapping("/lessons/{lessonId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteLesson(@PathVariable Long lessonId) {
        lmsService.deleteLesson(lessonId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── 매뉴얼 ──

    @GetMapping("/manuals")
    public ResponseEntity<ApiResponse<List<ManualResponse>>> getManuals() {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getManuals()));
    }

    @GetMapping("/manuals/{id}")
    public ResponseEntity<ApiResponse<ManualResponse>> getManual(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getManual(id)));
    }

    @PostMapping("/manuals")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ManualResponse>> createManual(@Valid @RequestBody ManualRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.createManual(request)));
    }

    @PutMapping("/manuals/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ManualResponse>> updateManual(
            @PathVariable Long id, @Valid @RequestBody ManualRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.updateManual(id, request)));
    }

    @DeleteMapping("/manuals/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteManual(@PathVariable Long id) {
        lmsService.deleteManual(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // ── 시험 ──
    // 코스 1개당 시험 1개(1:1), 시험 하나에 문항 여러 개.
    // 정답(answer)을 포함해서 내려주므로 이 API들은 전부 관리자 권한으로 제한한다.

    @GetMapping("/courses/{courseId}/exam")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.getExamByCourse(courseId)));
    }

    @PostMapping("/courses/{courseId}/exam")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(
            @PathVariable Long courseId, @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.createExam(courseId, request)));
    }

    @PutMapping("/exams/{examId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(
            @PathVariable Long examId, @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.updateExam(examId, request)));
    }

    @DeleteMapping("/exams/{examId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long examId) {
        lmsService.deleteExam(examId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/exams/{examId}/questions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ExamQuestionResponse>> addQuestion(
            @PathVariable Long examId, @Valid @RequestBody ExamQuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.addQuestion(examId, request)));
    }

    @PutMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<ExamQuestionResponse>> updateQuestion(
            @PathVariable Long questionId, @Valid @RequestBody ExamQuestionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lmsService.updateQuestion(questionId, request)));
    }

    @DeleteMapping("/questions/{questionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRAND_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(@PathVariable Long questionId) {
        lmsService.deleteQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
