package com.run4you.matching.repository;

import com.run4you.matching.entity.EngineerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EngineerProfileRepository extends JpaRepository<EngineerProfile, Long> {

    @Query("SELECT ep FROM EngineerProfile ep WHERE ep.user.id = :userId")
    Optional<EngineerProfile> findByUserId(@Param("userId") Long userId);
}
