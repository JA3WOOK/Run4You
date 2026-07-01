package com.run4you.lms.repository;

import com.run4you.lms.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findAllByCourseIdOrderByOrderIndex(Long courseId);
}
