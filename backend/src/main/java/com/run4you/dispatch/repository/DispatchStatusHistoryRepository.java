package com.run4you.dispatch.repository;

import com.run4you.dispatch.entity.DispatchStatus;
import com.run4you.dispatch.entity.DispatchStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DispatchStatusHistoryRepository extends JpaRepository<DispatchStatusHistory, Long> {

    // 상세 - 수리 시작 시각 (REPAIRING 진입 시점)
    @Query("""
        SELECT MIN(dsh.changedAt) FROM DispatchStatusHistory dsh
        WHERE dsh.assignmentId = :assignmentId
          AND dsh.status = :status
        """)
    Optional<LocalDateTime> findStartTime(
            @Param("assignmentId") Long assignmentId,
            @Param("status") DispatchStatus status);
}
