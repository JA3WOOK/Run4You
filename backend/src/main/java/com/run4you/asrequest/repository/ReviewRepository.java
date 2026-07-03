package com.run4you.asrequest.repository;

import com.run4you.asrequest.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByAsRequestId(Long asRequestId);
}