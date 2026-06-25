package com.run4you.matching.service;

import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.common.exception.*;
import com.run4you.matching.dto.AssignmentDetailResponse;
import com.run4you.matching.dto.CandidateScoreResponse;
import com.run4you.matching.dto.MatchingQueueItemResponse;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.entity.AssignmentCandidate;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.lock.AssignmentLockManager;
import com.run4you.matching.repository.AssignmentCandidateRepository;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.matching.service.ScoringEngine.ScoreResult;
import com.run4you.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  매칭 서비스 — 엔지니어 배정 엔진 핵심 비즈니스 로직               │
 * │                                                                      │
 * │  1. getMatchingQueue()   — 반경 내 AS 요청을 점수순으로 반환        │
 * │  2. getRequestDetail()   — 특정 요청의 상세 + 내 점수 조회          │
 * │  3. acceptAssignment()   — Redisson 분산 락으로 단 1명 배정 확정    │
 * │  4. getCandidateScores() — 관리자용 후보 스코어 로그 조회           │
 * └──────────────────────────────────────────────────────────────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MatchingService {

    private final EngineerProfileRepository     engineerProfileRepository;
    private final AsRequestRepository           asRequestRepository;
    private final AssignmentRepository          assignmentRepository;
    private final AssignmentCandidateRepository candidateRepository;

    private final ScoringEngine         scoringEngine;
    private final AssignmentLockManager lockManager;

    // ─────────────────────────────────────────────────────────────────
    //  내부 홀더: AS 요청 + 스코어 결과 묶음
    // ─────────────────────────────────────────────────────────────────
    private record ScoredRequest(AsRequest request, ScoreResult score) {}

    // ─────────────────────────────────────────────────────────────────
    //  1. 출동 요청 대기열 조회 (엔지니어 화면)
    // ─────────────────────────────────────────────────────────────────

    /**
     * 로그인 엔지니어의 반경 내 AS 요청을 스코어 내림차순으로 반환한다.
     *
     * 흐름:
     *  ① 엔지니어 프로필 조회 (위치·가용성 확인)
     *  ② RECEIVED 상태 AS 요청 전체 조회
     *  ③ 각 요청에 대해 개인 스코어 계산 → 반경 외 제외
     *  ④ 종합 점수 내림차순 정렬
     *  ⑤ DTO 변환 (순위 1부터)
     */
    public List<MatchingQueueItemResponse> getMatchingQueue(Long engineerUserId) {
        EngineerProfile engineer = getEngineerProfile(engineerUserId);

        if (!engineer.isDispatchable()) {
            log.info("[매칭 대기열] 비가용 상태 — userId={}, status={}",
                    engineerUserId, engineer.getAvailabilityStatus());
            return List.of();
        }

        int activeCount = assignmentRepository.countActiveByEngineerId(engineer.getUser().getId());
        List<AsRequest> openRequests = asRequestRepository.findAllReceived();

        // 스코어 계산 + 반경 필터
        List<ScoredRequest> scored = new ArrayList<>();
        for (AsRequest req : openRequests) {
            var store = req.getStore();
            if (store.getLatitude() == null || store.getLongitude() == null) continue;

            ScoreResult result = scoringEngine.score(
                    engineer,
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue(),
                    req.getEquipment().getCategory().name(),
                    req.getPriority().name(),
                    activeCount
            );
            if (result.isInRange()) {
                scored.add(new ScoredRequest(req, result));
            }
        }

        // 점수 내림차순 정렬
        scored.sort(Comparator.comparingDouble(sr -> -sr.score().getTotalScore()));

        // 순위 부여 + DTO 변환
        List<MatchingQueueItemResponse> result = new ArrayList<>();
        for (int i = 0; i < scored.size(); i++) {
            result.add(MatchingQueueItemResponse.of(i + 1, scored.get(i).request(), scored.get(i).score()));
        }
        return result;
    }

    // ─────────────────────────────────────────────────────────────────
    //  2. 출동 상세 정보 조회 (엔지니어 화면)
    // ─────────────────────────────────────────────────────────────────

    /**
     * 특정 AS 요청의 상세 정보와 해당 엔지니어의 스코어를 반환한다.
     * "출동 수락하기" 버튼 누르기 전 상세 확인 화면.
     */
    public AssignmentDetailResponse getRequestDetail(Long asRequestId, Long engineerUserId) {
        AsRequest req      = getAsRequest(asRequestId);
        EngineerProfile ep = getEngineerProfile(engineerUserId);
        var store          = req.getStore();

        validateReceived(req);

        int activeCount = assignmentRepository.countActiveByEngineerId(ep.getUser().getId());

        ScoreResult score = scoringEngine.score(
                ep,
                store.getLatitude().doubleValue(),
                store.getLongitude().doubleValue(),
                req.getEquipment().getCategory().name(),
                req.getPriority().name(),
                activeCount
        );
        if (!score.isInRange()) {
            throw new OutOfServiceRadiusException("서비스 반경 밖의 요청입니다.");
        }
        LocalDateTime lastRepairedAt = assignmentRepository
                .findLastRepairAtByEquipmentId(req.getEquipment().getId())
                .orElse(null);
        return AssignmentDetailResponse.of(req, score, lastRepairedAt);
    }

    // ─────────────────────────────────────────────────────────────────
    //  3. 수락 처리 — Redisson 분산 락으로 단 1명 확정
    // ─────────────────────────────────────────────────────────────────

    /**
     * 엔지니어가 AS 요청을 수락한다.
     *
     * [분산 락 범위 내 실행 순서]
     *  ① AS 요청 상태 재확인 (락 획득 후 Double-Check — 경쟁 조건 차단)
     *  ② Assignment 생성 (status = ACCEPTED)
     *  ③ AsRequest 상태 → ASSIGNED
     *  ④ 엔지니어 가용성 → BUSY
     *  ⑤ AssignmentCandidate 스코어 로그 저장
     *
     * @return 생성된 Assignment 엔티티
     */
    @Transactional
    public Assignment acceptAssignment(Long asRequestId, Long engineerUserId) {
        return lockManager.executeWithLock(asRequestId, () -> {
            log.info("[수락] 분산 락 획득 — asRequestId={}, engineerUserId={}",
                    asRequestId, engineerUserId);

            // ① 락 획득 후 상태 재확인 (Double-Check)
            AsRequest req      = getAsRequest(asRequestId);
            validateReceived(req);   // RECEIVED가 아니면 AlreadyAssignedException

            EngineerProfile ep   = getEngineerProfile(engineerUserId);
            User engineerUser    = ep.getUser();
            var  store           = req.getStore();
            int  activeCount     = assignmentRepository.countActiveByEngineerId(engineerUser.getId());

            // ② 스코어 재계산 (수락 시점 기준)
            ScoreResult score = scoringEngine.score(
                    ep,
                    store.getLatitude().doubleValue(),
                    store.getLongitude().doubleValue(),
                    req.getEquipment().getCategory().name(),
                    req.getPriority().name(),
                    activeCount
            );
            if (!score.isInRange()) {
                throw new OutOfServiceRadiusException("서비스 반경 밖의 요청입니다.");
            }

            // ③ Assignment 생성
            Assignment assignment = Assignment.create(
                    req, engineerUser,
                    BigDecimal.valueOf(score.getTotalScore())
            );
            assignmentRepository.save(assignment);

            // ④ AS 요청 상태 → ASSIGNED
            req.assignEngineer();
            asRequestRepository.save(req);

            // ⑤ 엔지니어 가용성 → BUSY
            ep.changeAvailability(AvailabilityStatus.BUSY);
            engineerProfileRepository.save(ep);

            // ⑥ 후보 스코어 로그 저장
            saveScoreLog(req, engineerUser, score, true);

            log.info("[수락] 완료 — assignmentId={}, asRequestId={}, engineerId={}",
                    assignment.getId(), asRequestId, engineerUser.getId());
            return assignment;
        });
    }

    // ─────────────────────────────────────────────────────────────────
    //  4. 후보 스코어 로그 조회 (BRAND_ADMIN 관제)
    // ─────────────────────────────────────────────────────────────────

    /**
     * 특정 AS 요청에 대한 모든 후보 엔지니어의 스코어 로그를 반환한다.
     * GET /api/assignments/{asRequestId}/candidates
     */
    public List<CandidateScoreResponse> getCandidateScores(Long asRequestId) {
        return candidateRepository
                .findByAsRequestIdOrderByTotalScoreDesc(asRequestId)
                .stream()
                .map(CandidateScoreResponse::from)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────
    //  스코어 로그 저장
    // ─────────────────────────────────────────────────────────────────

    private void saveScoreLog(AsRequest req, User engineer, ScoreResult score, boolean selected) {
        AssignmentCandidate candidate = AssignmentCandidate.builder()
                .asRequest(req)
                .engineer(engineer)
                .distanceScore(bd(score.getDistanceScore()))
                .specialtyScore(bd(score.getSpecialtyScore()))
                .ratingScore(bd(score.getRatingScore()))
                .availabilityScore(bd(score.getAvailabilityScore()))
                .urgencyScore(bd(score.getUrgencyScore()))
                .totalScore(bd(score.getTotalScore()))
                .selected(selected)
                .build();
        candidateRepository.save(candidate);
    }

    // ─────────────────────────────────────────────────────────────────
    //  내부 헬퍼
    // ─────────────────────────────────────────────────────────────────

    private EngineerProfile getEngineerProfile(Long userId) {
        return engineerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new EngineerNotFoundException(
                        "엔지니어 프로필을 찾을 수 없습니다. userId=" + userId));
    }

    private AsRequest getAsRequest(Long asRequestId) {
        return asRequestRepository.findById(asRequestId)
                .orElseThrow(() -> new AsRequestNotFoundException(
                        "AS 요청을 찾을 수 없습니다. id=" + asRequestId));
    }

    private void validateReceived(AsRequest req) {
        if (!"RECEIVED".equals(req.getStatus().name())) {
            throw new AlreadyAssignedException(
                    "이미 처리된 요청입니다. 현재 상태: " + req.getStatus());
        }
    }

    private BigDecimal bd(double value) {
        return BigDecimal.valueOf(value);
    }
}
