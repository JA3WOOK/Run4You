package com.run4you.education.repository;

import com.run4you.education.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByEngineerIdAndCourseId(Long engineerId, Long courseId);

    List<Enrollment> findByEngineerId(Long engineerId);
}
