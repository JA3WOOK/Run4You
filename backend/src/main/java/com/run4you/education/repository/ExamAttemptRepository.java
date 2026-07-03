package com.run4you.education.repository;

import com.run4you.education.entity.ExamAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExamAttemptRepository extends JpaRepository<ExamAttempt, Long> {
    List<ExamAttempt> findByEngineerIdAndExamIdOrderByAttemptedAtDesc(Long engineerId, Long examId);
}
