package com.run4you.dispatch.port;

import com.run4you.dispatch.dto.AssignmentEngineerResponse;

/**
 * 배정 → 엔지니어(users) + 프로필(engineer_profiles) 정보 조회 포트.
 * 기존 게이트웨이를 건드리지 않도록 별도 어댑터로 구현한다.
 */
public interface EngineerInfoPort {

    /** 배정 ID로 담당 엔지니어의 표시용 정보(이름/전화/평점/등급)를 조회. 없으면 null. */
    AssignmentEngineerResponse findByAssignment(Long assignmentId);
}
