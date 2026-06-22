package com.run4you.matching.repository;

import com.run4you.matching.entity.EngineerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EngineerProfileRepository extends JpaRepository<EngineerProfile, Long> {

    Optional<EngineerProfile> findByUserId(Long userId);
}
