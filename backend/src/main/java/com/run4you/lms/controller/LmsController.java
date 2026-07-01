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
}
