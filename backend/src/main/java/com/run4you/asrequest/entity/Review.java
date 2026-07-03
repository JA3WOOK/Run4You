package com.run4you.asrequest.entity;

import com.run4you.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reviews")
public class Review { // 엔지니어 평점 테이블 (as_requests와 1:1)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 대상 A/S 접수 (1:1, UNIQUE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "as_request_id", nullable = false, unique = true)
    private AsRequest asRequest;

    // 작성 점주
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    // 평가 대상 엔지니어
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "engineer_id", nullable = false)
    private User engineer;

    // 별점 (1~5)
    @Column(nullable = false)
    private int rating;

    // 리뷰 코멘트 (선택)
    @Column(length = 255)
    private String comment;

    // 리뷰 작성 시각 (자동)
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}