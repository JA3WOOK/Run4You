package com.run4you.matching.entity;

import com.run4you.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id")
    private User engineer;

    // 수리 시작 시간
    @Column
    private LocalDateTime acceptedAt;

    // 수리 완료 시각
    @Column
    private LocalDateTime completedAt;
}