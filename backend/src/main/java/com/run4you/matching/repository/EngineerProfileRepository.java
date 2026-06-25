package com.run4you.matching.repository;

import com.run4you.matching.entity.EngineerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EngineerProfileRepository extends JpaRepository<EngineerProfile, Long> {

    Optional<EngineerProfile> findByUserId(Long userId);

    /**
     * OFFLINE이 아닌 엔지니어 전체 조회 (위치 보유 필수)
     * 반경 필터링은 Haversine 계산 후 서비스 레이어에서 수행
     */
    @Query("""
            SELECT ep FROM EngineerProfile ep
            JOIN FETCH ep.user
            LEFT JOIN FETCH ep.specialties
            WHERE ep.availabilityStatus != 'OFFLINE'
              AND ep.currentLatitude IS NOT NULL
              AND ep.currentLongitude IS NOT NULL
            """)
    List<EngineerProfile> findAllDispatchable();

    Optional<EngineerProfile> findByUserEmail(String email);

    boolean existsByUserId(Long userId);
}
