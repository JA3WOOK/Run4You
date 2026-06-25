-- ================================================================
-- Run For You (런포유) — 도메인④ 동작 확인용 시드 데이터
-- 실행: mysql -u root -p run4you < seed-data.sql
-- 전제: run4you_ddl_수정_2.sql 로 스키마가 이미 생성돼 있어야 함
-- ================================================================

USE run4you;

-- 재실행 안전: 자식 → 부모 순서로 비우기 (FK 잠시 무시)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE notifications;
TRUNCATE TABLE dispatch_status_history;
TRUNCATE TABLE assignments;
TRUNCATE TABLE as_requests;
TRUNCATE TABLE engineer_profiles;
TRUNCATE TABLE equipment;
TRUNCATE TABLE stores;
TRUNCATE TABLE users;
TRUNCATE TABLE brands;
SET FOREIGN_KEY_CHECKS = 1;

-- 1) 브랜드
INSERT INTO brands (id, name, business_no, commission_rate, status)
VALUES (1, '메가커피 본사', '123-45-67890', 10.00, 'APPROVED');

-- 2) 계정 4종 (비밀번호는 더미 BCrypt 해시 — dev 헤더 인증이라 로그인에는 쓰이지 않음)
INSERT INTO users (id, email, password, name, phone, role, status, brand_id) VALUES
(1, 'super@run4you.com',    '$2a$10$abcdefghijklmnopqrstuv', '총괄관리자',   '010-0000-0001', 'SUPER_ADMIN', 'APPROVED', NULL),
(2, 'brand@run4you.com',    '$2a$10$abcdefghijklmnopqrstuv', '본사관리자',   '010-0000-0002', 'BRAND_ADMIN', 'APPROVED', 1),
(3, 'owner@run4you.com',    '$2a$10$abcdefghijklmnopqrstuv', '점주김사장',   '010-0000-0003', 'STORE_OWNER', 'APPROVED', NULL),
(4, 'engineer@run4you.com', '$2a$10$abcdefghijklmnopqrstuv', '엔지니어이기사', '010-0000-0004', 'ENGINEER',    'APPROVED', NULL);

-- 3) 점포 (위경도 필수 — ETA 거리 계산의 기준점)
INSERT INTO stores (id, brand_id, owner_id, name, address, latitude, longitude, phone)
VALUES (1, 1, 3, '메가커피 강남점', '서울 강남구 테헤란로 1', 37.4979000, 127.0276000, '02-1234-5678');

-- 4) 기자재
INSERT INTO equipment (id, store_id, name, serial_no, manufacturer, model_name, category, status)
VALUES (1, 1, '키오스크 1호기', 'SN-KIOSK-0001', '한국포스', 'KIOSK-X1', 'KIOSK', 'FAULTY');

-- 5) 엔지니어 프로필 (users.id=4 와 1:1)
INSERT INTO engineer_profiles
(id, user_id, rating, rating_count, revisit_rate, service_radius_km, availability_status, daily_capacity, skill_grade, current_latitude, current_longitude, location_updated_at)
VALUES
(1, 4, 4.80, 25, 3.20, 15, 'AVAILABLE', 5, 'INTERMEDIATE', 37.5012000, 127.0396000, NOW(6));

-- 6) A/S 접수 (ASSIGNED — 이미 매칭이 끝나 배정된 상태)
INSERT INTO as_requests (id, equipment_id, store_id, requester_id, symptom, priority, status)
VALUES (1, 1, 1, 3, '키오스크 화면이 켜지지 않습니다', 'EMERGENCY', 'ASSIGNED');

-- 7) 배정 (ACCEPTED — 엔지니어(4)가 DISPATCHED 로 전이 가능한 출발점)
INSERT INTO assignments (id, as_request_id, engineer_id, total_score, assign_method, status, assigned_at, accepted_at)
VALUES (1, 1, 4, 89.40, 'AUTO', 'ACCEPTED', NOW(6), NOW(6));

-- 확인
SELECT '--- users ---' AS info;
SELECT id, name, role, status FROM users;
SELECT '--- assignment ---' AS info;
SELECT id, as_request_id, engineer_id, status FROM assignments;
