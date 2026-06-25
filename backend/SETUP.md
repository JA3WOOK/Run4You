# 출동 관제(④) 모듈 — 하네스 실행 가이드

> 이 하네스는 **혼자 테스트할 때만** 쓰는 임시 받침대입니다.
> 팀 깃허브 레포에는 `Run4youApplication`·`DevSecurityConfig`·`DevAuthFilter`·`seed-data.sql`을
> **올리지 않고**, `dispatch/`·`notification/` 모듈만 올립니다.

## 파일 배치

```
run4you/                                  ← 프로젝트 루트
├─ build.gradle                           ← 하네스
├─ settings.gradle                        ← 하네스
├─ db/seed-data.sql                       ← 하네스
├─ test-requests.http                     ← 하네스
└─ src/
   ├─ main/
   │  ├─ java/com/runforyou/
   │  │  ├─ Run4youApplication.java        ← 하네스 (메인, 패키지 최상단)
   │  │  ├─ config/DevSecurityConfig.java  ← 하네스
   │  │  ├─ config/DevAuthFilter.java      ← 하네스
   │  │  ├─ dispatch/ …                     ← 모듈
   │  │  └─ notification/ …                 ← 모듈
   │  └─ resources/application.yml          ← 하네스
   └─ test/java/com/runforyou/dispatch/…    ← 모듈 (테스트 3종)
```

## 실행 순서

```bash
# 0) 사전 점검
java -version      # 21
mysql --version    # 8.x

# 1) 스키마 (DDL 이 run4you DB 까지 직접 생성)
mysql -u root -p < run4you_ddl_수정_2.sql

# 2) 시드
mysql -u root -p run4you < db/seed-data.sql

# 3) application.yml 의 password: CHANGE_ME 를 본인 비밀번호로 변경

# 4) 단위 테스트 (DB·보안 없이 로직 검증)
./gradlew test

# 5) 서버 기동 — dev 프로파일 필수
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 동작 확인 (터미널 2개)

```bash
# 터미널 A — 점주 SSE 구독 (연결 유지)
curl -N -H "X-USER-ID: 3" -H "X-USER-ROLE: STORE_OWNER" \
  http://localhost:8080/api/notifications/subscribe

# 터미널 B — 엔지니어 출동 시작
curl -X PATCH http://localhost:8080/api/assignments/1/status \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 4" -H "X-USER-ROLE: ENGINEER" \
  -d '{"status":"DISPATCHED","latitude":37.5012,"longitude":127.0396}'
```

→ 터미널 B 응답에 `etaMinutes`, 터미널 A 에 `event: dispatch` / `event: notification` 이 실시간으로 찍히면 정상.
나머지 단계(ARRIVED→REPAIRING→COMPLETED)와 실패 케이스는 `test-requests.http` 참고.

## 자주 막히는 지점

- **401** → `--spring.profiles.active=dev` 누락 (헤더 인증이 안 켜짐)
- **기동 시 Schema-validation 에러** → `ddl-auto: validate` 가 엔티티↔테이블 컬럼 불일치를 잡은 것. 로그의 `missing column` 줄 확인.
- **베이스 패키지가 `com.runforyou` 가 아니면** → 하네스 4개 .java 파일의 `package` 선언을 레포 패키지에 맞춰 일괄 치환.
