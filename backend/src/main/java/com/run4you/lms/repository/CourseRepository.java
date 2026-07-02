package com.run4you.lms.repository;

import com.run4you.lms.entity.Course;
import com.run4you.lms.entity.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByLevelAscCreatedAtDesc();
    List<Course> findAllByLevel(CourseLevel level);
}
