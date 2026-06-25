package com.run4you.certificate.entity;

/**
 * 기기 건강 등급 (§20).
 *  A 80~100 양호 / B 60~79 주의 / C 40~59 경고 / D 0~39 교체 권장
 */
public enum HealthGrade {
    A, B, C, D;

    public static HealthGrade fromScore(int score) {
        if (score >= 80) return A;
        if (score >= 60) return B;
        if (score >= 40) return C;
        return D;
    }
}
