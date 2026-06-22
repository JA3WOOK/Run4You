package com.run4you.dispatch.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "dispatch_status_history")
public class DispatchStatusHistory { // 테스트를 위한 임시 구현
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assignment_id", nullable = false)
    private Long assignmentId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DispatchStatus status;

    @Column(name = "changed_at")
    private LocalDateTime changedAt;
}
