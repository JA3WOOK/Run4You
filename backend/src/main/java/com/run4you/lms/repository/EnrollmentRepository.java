package com.run4you.lms.repository;

import com.run4you.lms.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByEngineerIdAndCourseId(Long engineerId, Long courseId);

    List<Enrollment> findByEngineerId(Long engineerId);
}
