package com.run4you.lms.repository;

import com.run4you.lms.entity.Course;
import com.run4you.lms.entity.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByLevelAscCreatedAtDesc();
    List<Course> findAllByLevel(CourseLevel level);

    /** 엔지니어 교육 */
    List<Course> findByStatusOrderByIdAsc(String status);

    List<Course> findByStatusAndLevelOrderByIdAsc(String status, CourseLevel level);

    @Query("select c from Course c left join fetch c.lessons where c.id = :id")
    Optional<Course> findWithLessonsById(@Param("id") Long id);
}
