package com.run4you.education.repository;

import com.run4you.education.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    @Query("select e from Exam e left join fetch e.questions where e.course.id = :courseId")
    Optional<Exam> findWithQuestionsByCourseId(@Param("courseId") Long courseId);

    @Query("select e from Exam e left join fetch e.questions where e.id = :examId")
    Optional<Exam> findWithQuestionsById(@Param("examId") Long examId);
}
