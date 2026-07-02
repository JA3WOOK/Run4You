package com.run4you.lms.repository;

import com.run4you.lms.entity.Manual;
import com.run4you.lms.entity.ManualType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ManualRepository extends JpaRepository<Manual, Long> {
    List<Manual> findAllByOrderByManualTypeAscCreatedAtDesc();
    List<Manual> findAllByManualType(ManualType manualType);
}
