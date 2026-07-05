package com.run4you.education.service;

import com.run4you.common.enums.EnrollmentStatus;
import com.run4you.common.enums.SkillGrade;
import com.run4you.common.exception.*;
import com.run4you.education.dto.*;
import com.run4you.lms.entity.*;
import com.run4you.lms.repository.*;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  엔지니어 교육(LMS) 서비스                                          │
 * │  - Course/Lesson/Manual 은 com.run4you.lms.entity의 기존 엔티티를    │
 * │    그대로 사용한다 (관리자 등록 기능과 동일 테이블).                 │
 * │  - Enrollment/LessonProgress/Exam/ExamQuestion/ExamAttempt 는        │
 * │    엔지니어 수강 기능을 위해 이번에 새로 추가한 엔티티.              │
 * └──────────────────────────────────────────────────────────────────────┘
 *
 * 필드 매핑 메모:
 *  - Course.grade   : String, 표시용 등급명 (예: "초급") — 그대로 응답에 노출
 *  - Course.level   : CourseLevel enum(BEGINNER/INTERMEDIATE/ADVANCED) — 필터링 & 등급 상향 판단
 *  - Course.passScore : Integer — 시험 합격 기준 점수 (Exam에는 별도로 두지 않음)
 *  - Manual.manualType : ManualType enum — 매뉴얼 종류 구분 (레거시 필드 type은 미사용)
 *  - Manual에는 videoUrl이 없음 — 응답에서 제외
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EngineerEducationService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ExamRepository examRepository;
    private final ExamAttemptRepository examAttemptRepository;
    private final ManualRepository manualRepository;

    private final UserRepository userRepository;
    private final EngineerProfileRepository engineerProfileRepository;

    // ─────────────────────────────────────────────────────────────────
    // 1. 코스 목록
    // ─────────────────────────────────────────────────────────────────

    public List<CourseListItemResponse> getCourses(String email, CourseLevel levelFilter) {
        User engineer = getUserByEmail(email);

        List<Course> courses = levelFilter == null
                ? courseRepository.findByStatusOrderByIdAsc("ACTIVE")
                : courseRepository.findByStatusAndLevelOrderByIdAsc("ACTIVE", levelFilter);

        List<Enrollment> myEnrollments = enrollmentRepository.findByEngineerId(engineer.getId());
        Map<Long, Enrollment> byCourseId = myEnrollments.stream()
                .collect(Collectors.toMap(e -> e.getCourse().getId(), e -> e));

        return courses.stream()
                .map(course -> {
                    Enrollment my = byCourseId.get(course.getId());
                    boolean hasExam = examRepository.findWithQuestionsByCourseId(course.getId()).isPresent();
                    return CourseListItemResponse.builder()
                            .courseId(course.getId())
                            .title(course.getTitle())
                            .gradeLabel(course.getGrade())
                            .level(course.getLevel())
                            .category(course.getCategory())
                            .description(course.getDescription())
                            .lessonCount(course.getLessons().size())
                            .myProgressRate(my == null ? BigDecimal.ZERO : my.getProgressRate())
                            .myStatus(my == null ? EnrollmentStatus.NOT_STARTED : my.getStatus())
                            .hasExam(hasExam)
                            .build();
                })
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. 코스 상세 (없으면 자동 수강신청 생성)
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public CourseDetailResponse getCourseDetail(String email, Long courseId) {
        User engineer = getUserByEmail(email);
        Course course = courseRepository.findWithLessonsById(courseId)
                .orElseThrow(() -> new CourseNotFoundException("코스를 찾을 수 없습니다. id=" + courseId));

        Enrollment enrollment = getOrCreateEnrollment(engineer, course);

        List<LessonProgress> progressList = lessonProgressRepository.findByEnrollmentId(enrollment.getId());
        Map<Long, LessonProgress> byLessonId = progressList.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));

        List<LessonResponse> lessons = course.getLessons().stream()
                .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                .map(lesson -> {
                    LessonProgress p = byLessonId.get(lesson.getId());
                    return LessonResponse.builder()
                            .lessonId(lesson.getId())
                            .title(lesson.getTitle())
                            .videoUrl(lesson.getVideoUrl())
                            .durationSeconds(lesson.getDurationSeconds())
                            .sortOrder(lesson.getSortOrder())
                            .content(lesson.getContent())
                            .watchedSeconds(p == null ? 0 : p.getWatchedSeconds())
                            .progressRate(p == null ? BigDecimal.ZERO : p.getProgressRate())
                            .completed(p != null && Boolean.TRUE.equals(p.getIsCompleted()))
                            .build();
                })
                .toList();

        boolean hasExam = examRepository.findWithQuestionsByCourseId(courseId).isPresent();

        return CourseDetailResponse.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .gradeLabel(course.getGrade())
                .level(course.getLevel())
                .category(course.getCategory())
                .description(course.getDescription())
                .myProgressRate(enrollment.getProgressRate())
                .myStatus(enrollment.getStatus())
                .hasExam(hasExam)
                .lessons(lessons)
                .build();
    }

    private Enrollment getOrCreateEnrollment(User engineer, Course course) {
        return enrollmentRepository.findByEngineerIdAndCourseId(engineer.getId(), course.getId())
                .orElseGet(() -> enrollmentRepository.save(
                        Enrollment.builder()
                                .engineer(engineer)
                                .course(course)
                                .progressRate(BigDecimal.ZERO)
                                .status(EnrollmentStatus.NOT_STARTED)
                                .build()));
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. 차시 진도 갱신 → 코스 진도율 재계산 (차시 진도율의 평균)
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public LessonProgressResponse updateLessonProgress(String email, Long lessonId, int watchedSeconds) {
        User engineer = getUserByEmail(email);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("차시를 찾을 수 없습니다. id=" + lessonId));

        Course course = lesson.getCourse();
        Enrollment enrollment = getOrCreateEnrollment(engineer, course);

        LessonProgress progress = lessonProgressRepository
                .findByEnrollmentIdAndLessonId(enrollment.getId(), lessonId)
                .orElseGet(() -> LessonProgress.builder()
                        .enrollment(enrollment)
                        .lesson(lesson)
                        .watchedSeconds(0)
                        .progressRate(BigDecimal.ZERO)
                        .isCompleted(false)
                        .build());

        progress.applyWatchedSeconds(watchedSeconds, lesson.getDurationSeconds());
        lessonProgressRepository.save(progress);

        BigDecimal courseRate = recalcCourseProgress(enrollment, course);

        return LessonProgressResponse.builder()
                .lessonId(lessonId)
                .lessonProgressRate(progress.getProgressRate())
                .lessonCompleted(progress.getIsCompleted())
                .courseProgressRate(courseRate)
                .courseStatus(enrollment.getStatus())
                .build();
    }

    private BigDecimal recalcCourseProgress(Enrollment enrollment, Course course) {
        List<Lesson> allLessons = lessonRepository.findByCourseIdOrderBySortOrderAsc(course.getId());
        if (allLessons.isEmpty()) {
            enrollment.updateProgress(BigDecimal.ZERO);
            return BigDecimal.ZERO;
        }

        List<LessonProgress> progressList = lessonProgressRepository.findByEnrollmentId(enrollment.getId());
        Map<Long, LessonProgress> byLessonId = progressList.stream()
                .collect(Collectors.toMap(p -> p.getLesson().getId(), p -> p));

        BigDecimal sum = BigDecimal.ZERO;
        for (Lesson l : allLessons) {
            LessonProgress p = byLessonId.get(l.getId());
            sum = sum.add(p == null ? BigDecimal.ZERO : p.getProgressRate());
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(allLessons.size()), 2, RoundingMode.HALF_UP);
        enrollment.updateProgress(avg);
        return avg;
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. 시험 문항 조회 (정답 비노출) — 코스 진도율 100% 미달 시 응시 차단
    // ─────────────────────────────────────────────────────────────────

    public ExamResponse getExam(String email, Long courseId) {
        User engineer = getUserByEmail(email);

        Enrollment enrollment = enrollmentRepository.findByEngineerIdAndCourseId(engineer.getId(), courseId)
                .orElseThrow(() -> new CourseGateNotSatisfiedException("먼저 코스를 수강해야 합니다."));

        if (enrollment.getProgressRate().compareTo(BigDecimal.valueOf(100)) < 0) {
            throw new CourseGateNotSatisfiedException(
                    "코스 진도율 100% 달성 후 시험에 응시할 수 있습니다. 현재 진도율=" + enrollment.getProgressRate());
        }

        Exam exam = examRepository.findWithQuestionsByCourseId(courseId)
                .orElseThrow(() -> new ExamNotFoundException("등록된 시험이 없습니다. courseId=" + courseId));

        Course course = exam.getCourse();
        if (course.getPassScore() == null) {
            throw new IllegalStateException("코스에 합격 기준 점수(passScore)가 설정되지 않았습니다. courseId=" + courseId);
        }

        int totalScore = exam.getQuestions().stream().mapToInt(ExamQuestion::getScore).sum();

        List<ExamQuestionResponse> questions = exam.getQuestions().stream()
                .map(q -> ExamQuestionResponse.builder()
                        .questionId(q.getId())
                        .question(q.getQuestion())
                        .choices(q.getChoices())
                        .score(q.getScore())
                        .build())
                .toList();

        return ExamResponse.builder()
                .examId(exam.getId())
                .courseId(courseId)
                .title(exam.getTitle())
                .passScore(course.getPassScore())
                .totalScore(totalScore)
                .questions(questions)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. 시험 채점 + 합격 시 기술 등급 상향
    //    Course.level(CourseLevel) 과 EngineerProfile.skillGrade(SkillGrade)는
    //    상수 이름이 동일(BEGINNER/INTERMEDIATE/ADVANCED)하므로 이름으로 그대로 매핑한다.
    // ─────────────────────────────────────────────────────────────────

    @Transactional
    public ExamResultResponse submitExam(String email, Long examId, ExamSubmitRequest request) {
        User engineer = getUserByEmail(email);

        Exam exam = examRepository.findWithQuestionsById(examId)
                .orElseThrow(() -> new ExamNotFoundException("시험을 찾을 수 없습니다. id=" + examId));

        Course course = exam.getCourse();
        if (course.getPassScore() == null) {
            throw new IllegalStateException("코스에 합격 기준 점수(passScore)가 설정되지 않았습니다.");
        }

        Map<Long, String> submitted = request.getAnswers().stream()
                .collect(Collectors.toMap(
                        ExamSubmitRequest.AnswerItem::getQuestionId,
                        ExamSubmitRequest.AnswerItem::getAnswer));

        int score = 0;
        for (ExamQuestion q : exam.getQuestions()) {
            String my = submitted.get(q.getId());
            if (my != null && my.trim().equals(q.getAnswer().trim())) {
                score += q.getScore();
            }
        }

        boolean passed = score >= course.getPassScore();

        ExamAttempt attempt = ExamAttempt.builder()
                .engineer(engineer)
                .exam(exam)
                .score(score)
                .passed(passed)
                .build();
        examAttemptRepository.save(attempt);

        boolean gradeUpgraded = false;
        String newGrade = null;

        if (passed) {
            EngineerProfile profile = engineerProfileRepository.findByUserEmail(email)
                    .orElseThrow(() -> new EngineerNotFoundException("엔지니어 프로필을 찾을 수 없습니다. email=" + email));

            SkillGrade courseGrade = SkillGrade.valueOf(course.getLevel().name());
            SkillGrade current = profile.getSkillGrade();

            if (isHigher(courseGrade, current)) {
                profile.upgradeSkillGrade(courseGrade);
                gradeUpgraded = true;
                newGrade = courseGrade.name();
            }
        }

        return ExamResultResponse.builder()
                .attemptId(attempt.getId())
                .score(score)
                .passScore(course.getPassScore())
                .passed(passed)
                .gradeUpgraded(gradeUpgraded)
                .newGrade(newGrade)
                .build();
    }

    private boolean isHigher(SkillGrade target, SkillGrade current) {
        return target.ordinal() > current.ordinal();
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. 매뉴얼 조회
    // ─────────────────────────────────────────────────────────────────

    public List<ManualResponse> getManuals(ManualType manualType, String faultCategory) {
        List<Manual> manuals;
        if (manualType == null) {
            manuals = manualRepository.findAll();
        } else if (faultCategory == null || faultCategory.isBlank()) {
            manuals = manualRepository.findByManualType(manualType);
        } else {
            manuals = manualRepository.findByManualTypeAndFaultCategory(manualType, faultCategory);
        }

        return manuals.stream()
                .map(m -> ManualResponse.builder()
                        .manualId(m.getId())
                        .manualType(m.getManualType())
                        .title(m.getTitle())
                        .faultCategory(m.getFaultCategory())
                        .content(m.getContent())
                        .build())
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. email=" + email));
    }
}
