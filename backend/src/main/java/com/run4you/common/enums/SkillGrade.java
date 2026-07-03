package com.run4you.common.enums;

/** 엔지니어 기술 등급 / 코스 등급 (ERD: SKILL_GRADE) */
public enum SkillGrade {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED;

    /** 시험 합격 시 다음 등급으로 상향 (ADVANCED가 최고 등급) */
    public SkillGrade next() {
        return switch (this) {
            case BEGINNER -> INTERMEDIATE;
            case INTERMEDIATE -> ADVANCED;
            case ADVANCED -> ADVANCED;
        };
    }
}
