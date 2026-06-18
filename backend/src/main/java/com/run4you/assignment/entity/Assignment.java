package com.run4you.assignment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "assignments")
public class Assignment { // 테스트용 임시 구현

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 접수 ID
    @Column(name = "as_request_id", nullable = false)
    private Long asRequestId;

    // 수리 완료 시각
    @Column
    private LocalDateTime completedAt;
}