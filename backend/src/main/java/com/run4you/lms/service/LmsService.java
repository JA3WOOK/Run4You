package com.run4you.lms.service;

import com.run4you.lms.dto.*;
import com.run4you.lms.entity.Course;
import com.run4you.lms.entity.Exam;
import com.run4you.lms.entity.ExamQuestion;
import com.run4you.lms.entity.Lesson;
import com.run4you.lms.entity.Manual;
import com.run4you.lms.repository.CourseRepository;
import com.run4you.lms.repository.ExamQuestionRepository;
import com.run4you.lms.repository.ExamRepository;
import com.run4you.lms.repository.LessonRepository;
import com.run4you.lms.repository.ManualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LmsService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final ManualRepository manualRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;

    // ── 코스 ──

    @Transactional(readOnly = true)
    public List<CourseResponse> getCourses() {
        return courseRepository.findAllByOrderByLevelAscCreatedAtDesc().stream()
                .map(CourseResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long id) {
        return new CourseResponse(findCourse(id));
    }

    @Transactional
    public CourseResponse createCourse(CourseRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .grade(gradeLabel(request.getLevel()))
                .category(request.getCategory())
                .status("ACTIVE")
                .level(request.getLevel())
                .targetSpecialty(request.getTargetSpecialty())
                .passScore(request.getPassScore())
                .build();
        return new CourseResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse updateCourse(Long id, CourseRequest request) {
        Course course = findCourse(id);
        course.update(request.getTitle(), request.getDescription(), gradeLabel(request.getLevel()),
                request.getCategory(), request.getLevel(),
                request.getTargetSpecialty(), request.getPassScore());
        return new CourseResponse(course);
    }

    /** CourseLevel(BEGINNER/INTERMEDIATE/ADVANCED) → 표시용 등급명(Course.grade) 자동 생성 */
    private String gradeLabel(com.run4you.lms.entity.CourseLevel level) {
        if (level == null) return null;
        return switch (level) {
            case BEGINNER -> "초급";
            case INTERMEDIATE -> "중급";
            case ADVANCED -> "고급";
        };
    }

    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.delete(findCourse(id));
    }

    // ── 차시 ──

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessons(Long courseId) {
        return lessonRepository.findAllByCourseIdOrderByOrderIndex(courseId).stream()
                .map(LessonResponse::new).toList();
    }

    @Transactional
    public LessonResponse createLesson(Long courseId, LessonRequest request) {
        Course course = findCourse(courseId);
        int idx = request.getOrderIndex();
        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.getTitle())
                .videoUrl(request.getVideoUrl() != null ? request.getVideoUrl() : "")
                .durationSeconds(request.getDurationSeconds() != null ? request.getDurationSeconds() : 0)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : idx)
                .content(request.getContent())
                .orderIndex(idx)
                .build();
        return new LessonResponse(lessonRepository.save(lesson));
    }

    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonRequest request) {
        Lesson lesson = findLesson(lessonId);
        lesson.update(request.getTitle(), request.getVideoUrl(),
                request.getDurationSeconds(), request.getSortOrder(),
                request.getContent(), request.getOrderIndex());
        return new LessonResponse(lesson);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.delete(findLesson(lessonId));
    }

    // ── 매뉴얼 ──

    @Transactional(readOnly = true)
    public List<ManualResponse> getManuals() {
        return manualRepository.findAllByOrderByManualTypeAscCreatedAtDesc().stream()
                .map(ManualResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public ManualResponse getManual(Long id) {
        return new ManualResponse(findManual(id));
    }

    @Transactional
    public ManualResponse createManual(ManualRequest request) {
        Manual manual = Manual.builder()
                .type(request.getManualType().name())
                .title(request.getTitle())
                .content(request.getContent())
                .manualType(request.getManualType())
                .faultCategory(request.getFaultCategory())
                .build();
        return new ManualResponse(manualRepository.save(manual));
    }

    @Transactional
    public ManualResponse updateManual(Long id, ManualRequest request) {
        Manual manual = findManual(id);
        manual.update(request.getTitle(), request.getContent(),
                request.getManualType(), request.getFaultCategory());
        return new ManualResponse(manual);
    }

    @Transactional
    public void deleteManual(Long id) {
        manualRepository.delete(findManual(id));
    }

    // ── 시험 (코스 1개당 시험 1개, 문항 여러 개) ──

    @Transactional(readOnly = true)
    public ExamResponse getExamByCourse(Long courseId) {
        return examRepository.findWithQuestionsByCourseId(courseId)
                .map(ExamResponse::new)
                .orElse(null);
    }

    @Transactional
    public ExamResponse createExam(Long courseId, ExamRequest request) {
        Course course = findCourse(courseId);
        if (examRepository.findWithQuestionsByCourseId(courseId).isPresent()) {
            throw new IllegalStateException("이미 이 코스에 등록된 시험이 있습니다. 기존 시험을 수정해주세요.");
        }
        Exam exam = Exam.builder()
                .course(course)
                .title(request.getTitle())
                .build();
        return new ExamResponse(examRepository.save(exam));
    }

    @Transactional
    public ExamResponse updateExam(Long examId, ExamRequest request) {
        Exam exam = findExam(examId);
        exam.updateTitle(request.getTitle());
        return new ExamResponse(exam);
    }

    @Transactional
    public void deleteExam(Long examId) {
        examRepository.delete(findExam(examId));
    }

    @Transactional
    public ExamQuestionResponse addQuestion(Long examId, ExamQuestionRequest request) {
        Exam exam = findExam(examId);
        ExamQuestion question = ExamQuestion.builder()
                .exam(exam)
                .question(request.getQuestion())
                .choices(request.getChoices())
                .answer(request.getAnswer())
                .score(request.getScore())
                .build();
        return new ExamQuestionResponse(examQuestionRepository.save(question));
    }

    @Transactional
    public ExamQuestionResponse updateQuestion(Long questionId, ExamQuestionRequest request) {
        ExamQuestion question = findQuestion(questionId);
        question.update(request.getQuestion(), request.getChoices(), request.getAnswer(), request.getScore());
        return new ExamQuestionResponse(question);
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        examQuestionRepository.delete(findQuestion(questionId));
    }

    // ── 조회 헬퍼 ──

    private Course findCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코스입니다."));
    }

    private Lesson findLesson(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 차시입니다."));
    }

    private Manual findManual(Long id) {
        return manualRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 매뉴얼입니다."));
    }

    private Exam findExam(Long id) {
        return examRepository.findWithQuestionsById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 시험입니다."));
    }

    private ExamQuestion findQuestion(Long id) {
        return examQuestionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 문항입니다."));
    }
}