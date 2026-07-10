<div align="center">

# 🚨 Run4You

### 기자재 긴급 A/S 관제 플랫폼

> 프랜차이즈 카페·무인 매장의 키오스크, 에스프레소 머신 등 기자재가 고장 났을 때  
> **긴급 접수 → 엔지니어 자동 매칭 → 실시간 출동 관제 → 정비 리포트 → 정산**까지  
> 하나의 플랫폼에서 처리하는 **B2B SaaS 관제 시스템**입니다.

<br>

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![JPA](https://img.shields.io/badge/JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![SSE](https://img.shields.io/badge/SSE-FF6B35?style=for-the-badge&logoColor=white)
![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)

</div>

<hr>

## 📌 프로젝트 소개

Run4You는 프랜차이즈 카페·무인 매장 기자재의 긴급 A/S를 **자동화·지능화**하는 B2B SaaS 관제 플랫폼입니다.

기존 A/S 처리 방식(전화 접수 → 수동 배정 → 현황 파악 불가)의 한계를 극복하고,  
**가중치 기반 자동 매칭 엔진 + Redis 분산 락 + SSE 실시간 관제**로 운영 효율을 극대화합니다.

| 항목 | 내용 |
|---|---|
| 서비스 유형 | B2B SaaS A/S 관제 플랫폼 |
| 대상 고객 | 프랜차이즈 카페·무인 매장 운영사, 기자재 A/S 업체 |
| 핵심 가치 | 긴급 상황 자동 대응 · 중복 배정 제로 · 실시간 현황 투명화 |

### 3대 핵심 처리 흐름

| 흐름 | 설명 |
|---|---|
| ⚖️ **가중치 기반 자동 배정** | 거리·전문분야·평점·가용성·긴급도 5요소 종합 스코어링으로 최적 엔지니어 자동 매칭, Redis 분산 락으로 중복 배정 차단 |
| 📡 **SSE 실시간 출동 관제** | 출동→도착→수리 개시→완료 단계 변경 시 점주에게 즉시 SSE 알림 발송, 대기 불확실성 제거 |
| 🎓 **LMS 연계 수리 게이트** | 필수 교육 진도율 100% 달성 전에는 REPAIRING 상태로 전이 불가 |

<hr>

## 📅 개발 기간

```
2026.06.10 (수) ~ 2026.07.08 (수) — 총 6주 프로젝트
```

<hr>

## 🛠️ 기술 스택

### Backend

| 기술 | 버전 | 용도 |
|---|---|---|
| Java | 21 | 메인 언어 |
| Spring Boot | 3.x | 웹 애플리케이션 프레임워크 |
| Spring Security | - | 4단계 RBAC 인증·인가 |
| Spring Data JPA | - | ORM, DB 접근 |
| MySQL | 8.0 | 관계형 데이터베이스 |
| Redis (Redisson) | - | 분산 락 기반 동시성 제어 |
| SSE (SseEmitter) | - | 서버→클라이언트 실시간 알림 |
| JWT | - | Stateless 인증 토큰 |

### Frontend

| 기술 | 용도 |
|---|---|
| React | UI 컴포넌트 기반 프론트엔드 |
| Axios | API 통신 및 interceptor 토큰 처리 |

### 협업 툴

| 도구 | 용도 |
|---|---|
| GitHub | 코드 버전 관리, PR 기반 코드 리뷰 |
| Notion | API 명세서, ERD, 기획 문서, 일정 관리 |
| Postman | API 테스트 |

<hr>

## ✨ 주요 기능

<details>
<summary><b>🔐 1. 인증 / 권한 (4단계 RBAC)</b></summary>
<br>

- JWT 기반 인증 (Access Token + Refresh Token)
- 4단계 역할 분리: `SUPER_ADMIN` / `BRAND_ADMIN` / `STORE_OWNER` / `ENGINEER`
- 역할별 API 접근 제어 (Spring Security)
- 인증 만료 시 자동 갱신

</details>

<details>
<summary><b>⚖️ 2. 엔지니어 자동 매칭·배정 엔진</b></summary>
<br>

- **5요소 가중치 스코어링:** 거리 · 전문분야 일치 · 평점 · 가용성 · 긴급도
- 종합 점수 기반 최적 엔지니어 자동 선정
- Redis 분산 락(Redisson `tryLock`)으로 동시 다중 수락 시 중복 배정 원천 차단
- TTL 설정으로 데드락 방지

</details>

<details>
<summary><b>📋 3. 출동요청 대기열 & 상태 관제</b></summary>
<br>

- 엔지니어 반경 내 요청을 종합 점수 내림차순으로 정렬 조회
- 출동 상태 전이: `DISPATCHED → ARRIVED → REPAIRING → COMPLETED`
- 각 상태 변경 시 점주에게 SSE 실시간 알림 자동 발송

</details>

<details>
<summary><b>🎓 4. LMS (학습 관리 시스템)</b></summary>
<br>

**엔지니어 파트**
- 기술 등급별 코스 목록 조회 및 수강 신청
- 차시별 동영상 시청 진도율 관리 (0~100%)
- 온라인 필기시험 응시 및 채점

**본사 관리자 파트**
- 코스·차시 동영상 URL 등록 및 관리
- 엔지니어별 이수율 통계 조회
- 시험 문제 출제·수정·삭제 관리

**수리 게이트 연동**
- 필수 교육 이수율 100% 미달 시 `REPAIRING` 상태 전이 차단

</details>

<details>
<summary><b>📄 5. 정비 리포트 & 정산</b></summary>
<br>

- 수리 완료 후 부품 · 공임 입력 및 표준 단가 검증 기반 리포트 작성
- 리포트 미작성 배정 건 필터링 조회
- 리포트 기반 정산 데이터 생성

</details>

<hr>

## 🙋 내 역할

> **담당 범위:** 매칭·배정 엔진 + 출동 관제 + LMS 엔지니어·관리자 파트 + 정비 리포트 — 풀사이클 구현

### 구현 기능 상세

| 기능 | 구현 내용 |
|---|---|
| 매칭·배정 엔진 | 거리·전문분야·평점·가용성·긴급도 5요소 가중치 스코어링 엔진 설계·구현, Redisson 분산 락 동시성 제어 |
| 출동요청 대기열 | 엔지니어 반경 내 요청 종합 점수 내림차순 정렬 조회 API |
| 출동상세 · 상태 변경 | 배정 상세 조회 및 DISPATCHED→ARRIVED→REPAIRING→COMPLETED 상태 전이 + SSE 알림 연동 |
| LMS 엔지니어 파트 | 코스 수강, 차시별 동영상 진도율 관리, 온라인 필기시험 응시 |
| LMS 본사관리자 파트 | 차시 URL 등록, 엔지니어 이수율 통계, 시험 문제 출제·관리 |
| 정비 리포트 | 부품·공임 입력, 표준 단가 검증 기반 리포트 작성, 미작성 배정 건 필터링 |

### 가중치 스코어링 공식

```
종합 점수 = (거리 역수 × W1) + (전문분야 일치 × W2) + (평점 × W3)
           + (가용성 × W4) + (긴급도 × W5)

→ 최고 점수 엔지니어에게 Redis 분산 락 선점 후 배정 확정
→ 락 실패(이미 배정됨) 시 즉시 거절 응답 반환
```

<hr>

## 🐛 트러블슈팅

### 1. 긴급 출동 요청 동시 수락 시 중복 배정 (Race Condition)

| 구분 | 내용 |
|---|---|
| **문제** | EMERGENCY 우선순위 요청에 여러 엔지니어가 동시에 수락 시 동일 접수 건이 2명 이상에게 중복 배정 |
| **원인** | 단일 서버의 `synchronized` 블록은 다중 인스턴스 스케일아웃 환경에서 동시성 제어 불가. DB 비관적 락은 응답 지연과 락 경합 부하를 DB로 전가 |
| **해결** | Redisson 분산 락 (`tryLock`, key: `lock:as:{asRequestId}`) 도입. 최초 락 획득 엔지니어에게만 배정 확정, 나머지는 즉시 거절. TTL 설정으로 데드락 방지 |
| **결과** | 동시 다중 수락 환경에서도 단 1명에게만 배정 확정, 중복 배정 완전 제거 |

```java
// 핵심 코드 (개념)
RLock lock = redissonClient.getLock("lock:as:" + asRequestId);
if (lock.tryLock(0, 5, TimeUnit.SECONDS)) {
    try {
        // 배정 확정 로직
    } finally {
        lock.unlock();
    }
} else {
    throw new AlreadyAssignedException("이미 배정된 요청입니다.");
}
```

> 💡 **배운 점:** 분산 환경의 동시성 문제는 애플리케이션 레벨이 아닌 **인프라 레벨 분산 락**으로 풀어야 하며, 락 실패 시 사용자에게 즉각적인 피드백을 주는 UX 설계가 함께 필요합니다.

<hr>

### 2. 필수 교육 미이수 상태에서 수리 개시 진입 가능

| 구분 | 내용 |
|---|---|
| **문제** | 필수 교육 콘텐츠를 100% 시청하지 않은 엔지니어도 출동 상태를 `REPAIRING`으로 변경 가능 — QA 중 발견 |
| **원인** | 상태 변경 API가 `DISPATCHED → ARRIVED → REPAIRING` 순서만 검증하고, `as_request_trainings`의 필수 콘텐츠 이수 여부(`is_completed`) 미확인 |
| **해결** | `REPAIRING` 전이 직전 필수 교육 게이트 검증 단계 추가. 미이수 시 명확한 예외 메시지와 함께 전이 차단 |
| **결과** | LMS 이수율 100% 미달 시 REPAIRING 상태 전이 원천 차단 |

```java
// REPAIRING 전이 전 게이트 검증
if (status == DispatchStatus.REPAIRING) {
    boolean isAllCompleted = trainingRepository
        .findByAsRequestId(asRequestId)
        .stream()
        .allMatch(AsRequestTraining::isCompleted);

    if (!isAllCompleted) {
        throw new TrainingNotCompletedException("필수 교육을 모두 이수해야 수리를 시작할 수 있습니다.");
    }
}
```

> 💡 **배운 점:** 상태 머신은 **단계 순서**뿐만 아니라 **도메인 간 선행 조건**까지 함께 검증해야 완전한 비즈니스 규칙이 됩니다. 이후 기능 설계 시 게이트 조건을 먼저 명세하는 습관이 생겼습니다.

<hr>

## 🚀 시작하기

### 사전 요구사항

- Java 21 이상
- MySQL 8.0 이상
- Redis 7.0 이상
- Node.js 18 이상
- Gradle

### Backend 실행

```bash
# 1. 레포지토리 클론
git clone https://github.com/JA3WOOK/Run4You.git
cd Run4You/backend

# 2. MySQL 데이터베이스 생성
CREATE DATABASE run4you DEFAULT CHARACTER SET utf8mb4;

# 3. Redis 실행 확인
redis-cli ping  # PONG 응답 확인

# 4. application.yml 설정
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/run4you
    username: YOUR_USERNAME
    password: YOUR_PASSWORD
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: YOUR_JWT_SECRET
  access-expiration: 3600000     # 1시간
  refresh-expiration: 604800000  # 7일

# 5. 빌드 및 실행
./gradlew bootRun
```

### Frontend 실행

```bash
cd Run4You/frontend
npm install
npm run dev
```

### 접속 URL

```
Frontend : http://localhost:5173
Backend  : http://localhost:8080
```
