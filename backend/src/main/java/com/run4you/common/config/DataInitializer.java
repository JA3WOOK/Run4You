package com.run4you.common.config;

import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.entity.AsStatus;
import com.run4you.asrequest.entity.Priority;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import com.run4you.brand.repository.BrandRepository;
import com.run4you.common.enums.AssignMethod;
import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.common.enums.DispatchStatus;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.entity.EquipmentCategory;
import com.run4you.equipment.entity.EquipmentStatus;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.entity.EngineerSpecialty;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.report.entity.RepairReport;
import com.run4you.report.repository.RepairReportRepository;
import com.run4you.store.entity.Store;
import com.run4you.store.repository.StoreRepository;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final StoreRepository storeRepository;
    private final EquipmentRepository equipmentRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final AsRequestRepository asRequestRepository;
    private final AssignmentRepository assignmentRepository;
    private final RepairReportRepository repairReportRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // SUPER_ADMIN
        if (!userRepository.existsByEmail("admin@run4you.com")) {
            userRepository.save(User.builder()
                    .email("admin@run4you.com")
                    .password(passwordEncoder.encode("admin1234!"))
                    .name("관리자")
                    .role(Role.SUPER_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build());
        }

        // 브랜드 1: 스타벅스
        Brand brand1 = brandRepository.findByBusinessNo("123-45-67890").orElseGet(() ->
                brandRepository.save(Brand.builder()
                        .name("스타벅스")
                        .businessNo("123-45-67890")
                        .commissionRate(new BigDecimal("5.00"))
                        .status(BrandStatus.ACTIVE)
                        .build())
        );

        // 브랜드 2: 투썸플레이스
        Brand brand2 = brandRepository.findByBusinessNo("987-65-43210").orElseGet(() ->
                brandRepository.save(Brand.builder()
                        .name("투썸플레이스")
                        .businessNo("987-65-43210")
                        .commissionRate(new BigDecimal("4.50"))
                        .status(BrandStatus.ACTIVE)
                        .build())
        );

        // BRAND_ADMIN - 스타벅스
        if (!userRepository.existsByEmail("sbux-admin@run4you.com")) {
            userRepository.save(User.builder()
                    .email("sbux-admin@run4you.com")
                    .password(passwordEncoder.encode("test1234!"))
                    .name("스타벅스 관리자")
                    .role(Role.BRAND_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .brandId(brand1.getId())
                    .build());
        }

        // BRAND_ADMIN - 투썸
        if (!userRepository.existsByEmail("twosome-admin@run4you.com")) {
            userRepository.save(User.builder()
                    .email("twosome-admin@run4you.com")
                    .password(passwordEncoder.encode("test1234!"))
                    .name("투썸 관리자")
                    .role(Role.BRAND_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .brandId(brand2.getId())
                    .build());
        }

        // 점주 1 - 스타벅스 강남점
        User owner1 = userRepository.findByEmail("owner1@run4you.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("owner1@run4you.com")
                        .password(passwordEncoder.encode("test1234!"))
                        .name("김점주")
                        .phone("010-1111-2222")
                        .role(Role.STORE_OWNER)
                        .status(UserStatus.ACTIVE)
                        .brandId(brand1.getId())
                        .build())
        );

        // 점주 2 - 투썸 홍대점
        User owner2 = userRepository.findByEmail("owner2@run4you.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("owner2@run4you.com")
                        .password(passwordEncoder.encode("test1234!"))
                        .name("이점주")
                        .phone("010-3333-4444")
                        .role(Role.STORE_OWNER)
                        .status(UserStatus.ACTIVE)
                        .brandId(brand2.getId())
                        .build())
        );

        // 엔지니어 1
        User engineer1 = userRepository.findByEmail("engineer1@run4you.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("engineer1@run4you.com")
                        .password(passwordEncoder.encode("test1234!"))
                        .name("박엔지니어")
                        .phone("010-5555-6666")
                        .role(Role.ENGINEER)
                        .status(UserStatus.ACTIVE)
                        .brandId(brand1.getId())
                        .build())
        );

        // 엔지니어 2
        User engineer2 = userRepository.findByEmail("engineer2@run4you.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("engineer2@run4you.com")
                        .password(passwordEncoder.encode("test1234!"))
                        .name("최엔지니어")
                        .phone("010-7777-8888")
                        .role(Role.ENGINEER)
                        .status(UserStatus.ACTIVE)
                        .brandId(brand2.getId())
                        .build())
        );

        // 매장 1 - 스타벅스 강남점
        Store store1 = storeRepository.findByOwnerId(owner1.getId()).orElseGet(() ->
                storeRepository.save(Store.builder()
                        .brand(brand1)
                        .owner(owner1)
                        .name("스타벅스 강남점")
                        .address("서울시 강남구 테헤란로 1")
                        .latitude(new BigDecimal("37.4979"))
                        .longitude(new BigDecimal("127.0276"))
                        .phone("02-1111-2222")
                        .build())
        );

        // 매장 2 - 투썸 홍대점
        Store store2 = storeRepository.findByOwnerId(owner2.getId()).orElseGet(() ->
                storeRepository.save(Store.builder()
                        .brand(brand2)
                        .owner(owner2)
                        .name("투썸플레이스 홍대점")
                        .address("서울시 마포구 홍익로 2")
                        .latitude(new BigDecimal("37.5567"))
                        .longitude(new BigDecimal("126.9237"))
                        .phone("02-3333-4444")
                        .build())
        );

        // 기자재 - 매장1
        if (equipmentRepository.countByActiveByStoreId(store1.getId()) == 0) {
            equipmentRepository.save(Equipment.builder()
                    .store(store1)
                    .name("키오스크 1호기")
                    .serialNo("KIOSK-SBX-001")
                    .manufacturer("삼성전자")
                    .modelName("SBX-K100")
                    .category(EquipmentCategory.KIOSK)
                    .status(EquipmentStatus.OPERATIONAL)
                    .purchasedAt(LocalDate.of(2023, 1, 1))
                    .nextInspectionDate(LocalDate.of(2026, 7, 1))
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .store(store1)
                    .name("에스프레소 머신 1호기")
                    .serialNo("ESPRESSO-SBX-001")
                    .manufacturer("드롱기")
                    .modelName("DLSC600")
                    .category(EquipmentCategory.ESPRESSO)
                    .status(EquipmentStatus.OPERATIONAL)
                    .purchasedAt(LocalDate.of(2022, 6, 1))
                    .nextInspectionDate(LocalDate.of(2026, 8, 1))
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .store(store1)
                    .name("제빙기 1호기")
                    .serialNo("ICE-SBX-001")
                    .manufacturer("LG전자")
                    .modelName("LG-ICE200")
                    .category(EquipmentCategory.ICE_MAKER)
                    .status(EquipmentStatus.FAULTY)
                    .purchasedAt(LocalDate.of(2021, 3, 1))
                    .nextInspectionDate(LocalDate.of(2026, 6, 30))
                    .build());
        }

        // 기자재 - 매장2
        if (equipmentRepository.countByActiveByStoreId(store2.getId()) == 0) {
            equipmentRepository.save(Equipment.builder()
                    .store(store2)
                    .name("키오스크 1호기")
                    .serialNo("KIOSK-TWO-001")
                    .manufacturer("LG전자")
                    .modelName("TWO-K200")
                    .category(EquipmentCategory.KIOSK)
                    .status(EquipmentStatus.OPERATIONAL)
                    .purchasedAt(LocalDate.of(2023, 3, 1))
                    .nextInspectionDate(LocalDate.of(2026, 9, 1))
                    .build());

            equipmentRepository.save(Equipment.builder()
                    .store(store2)
                    .name("냉장고 1호기")
                    .serialNo("FRIDGE-TWO-001")
                    .manufacturer("삼성전자")
                    .modelName("TWO-R300")
                    .category(EquipmentCategory.REFRIGERATOR)
                    .status(EquipmentStatus.OPERATIONAL)
                    .purchasedAt(LocalDate.of(2022, 9, 1))
                    .nextInspectionDate(LocalDate.of(2026, 10, 1))
                    .build());
        }

        // 엔지니어 프로필 1
        if (!engineerProfileRepository.existsByUserId(engineer1.getId())) {
            engineerProfileRepository.save(EngineerProfile.builder()
                    .user(engineer1)
                    .rating(new BigDecimal("4.50"))
                    .ratingCount(20)
                    .serviceRadiusKm(10)
                    .availabilityStatus(AvailabilityStatus.AVAILABLE)
                    .currentLatitude(new BigDecimal("37.5000"))
                    .currentLongitude(new BigDecimal("127.0000"))
                    .skillGrade("INTERMEDIATE")
                    .build());
        }

        // 엔지니어 프로필 2
        if (!engineerProfileRepository.existsByUserId(engineer2.getId())) {
            engineerProfileRepository.save(EngineerProfile.builder()
                    .user(engineer2)
                    .rating(new BigDecimal("4.20"))
                    .ratingCount(15)
                    .serviceRadiusKm(8)
                    .availabilityStatus(AvailabilityStatus.AVAILABLE)
                    .currentLatitude(new BigDecimal("37.5600"))
                    .currentLongitude(new BigDecimal("126.9200"))
                    .skillGrade("BEGINNER")
                    .build());
        }

        // 엔지니어 전문분야 (specialty 없을 때만 추가)
        engineerProfileRepository.findByUserId(engineer1.getId()).ifPresent(profile -> {
            if (profile.getSpecialties().isEmpty()) {
                profile.getSpecialties().add(EngineerSpecialty.of(profile, "KIOSK"));
                profile.getSpecialties().add(EngineerSpecialty.of(profile, "ESPRESSO"));
                engineerProfileRepository.save(profile);
            }
        });

        engineerProfileRepository.findByUserId(engineer2.getId()).ifPresent(profile -> {
            if (profile.getSpecialties().isEmpty()) {
                profile.getSpecialties().add(EngineerSpecialty.of(profile, "KIOSK"));
                profile.getSpecialties().add(EngineerSpecialty.of(profile, "ICE_MAKER"));
                engineerProfileRepository.save(profile);
            }
        });

        // 완료된 수리 이력 더미 (AsRequest → Assignment → RepairReport → Settlement 전체 체인)
        try {
            Integer settlementCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM settlements", Integer.class);
            if (settlementCount == null || settlementCount == 0) {
                addRepairDummies(brand1.getId(), brand2.getId(),
                        store1, store2, owner1, owner2, engineer1, engineer2);
            }
        } catch (Exception e) {
            System.err.println("[DataInitializer] 수리 이력 더미 삽입 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addRepairDummies(Long brand1Id, Long brand2Id,
                                   Store store1, Store store2,
                                   User owner1, User owner2,
                                   User engineer1, User engineer2) {
        List<Equipment> s1 = equipmentRepository.findActiveByStoreId(store1.getId());
        List<Equipment> s2 = equipmentRepository.findActiveByStoreId(store2.getId());
        if (s1.isEmpty() || s2.isEmpty()) return;

        Equipment kiosk1   = byCategory(s1, EquipmentCategory.KIOSK);
        Equipment espresso1 = byCategory(s1, EquipmentCategory.ESPRESSO);
        Equipment kiosk2   = byCategory(s2, EquipmentCategory.KIOSK);
        Equipment fridge2  = byCategory(s2, EquipmentCategory.REFRIGERATOR);

        // month, store, equipment, owner, engineer, brandId, priority, faultCategory, symptom,
        // requestedAt, completedAt, labor, parts, emergency, billed, vat, invoiceNo
        createRepair(store1, kiosk1,   owner1, engineer1, brand1Id, Priority.NORMAL,   "화면손상",    "터치 불량",
                ldt(2026,2,8),  ldt(2026,2,10), 50000, 120000, 0,      170000, 17000, "AS-2026-001");
        createRepair(store2, kiosk2,   owner2, engineer2, brand2Id, Priority.NORMAL,   "전원불량",    "화면 꺼짐",
                ldt(2026,2,18), ldt(2026,2,20), 60000, 150000, 0,      210000, 21000, "AS-2026-002");
        createRepair(store1, espresso1,owner1, engineer1, brand1Id, Priority.EMERGENCY,"누수발생",    "누수 발생",
                ldt(2026,3,6),  ldt(2026,3,8),  80000, 200000, 84000,  364000, 36400, "AS-2026-003");
        createRepair(store2, kiosk2,   owner2, engineer2, brand2Id, Priority.NORMAL,   "네트워크오류","결제 오류",
                ldt(2026,3,20), ldt(2026,3,22), 90000, 200000, 0,      290000, 29000, "AS-2026-004");
        createRepair(store1, kiosk1,   owner1, engineer2, brand1Id, Priority.NORMAL,   "소음발생",    "버튼 불량",
                ldt(2026,4,3),  ldt(2026,4,5),  40000,  80000, 0,      120000, 12000, "AS-2026-005");
        createRepair(store2, fridge2,  owner2, engineer1, brand2Id, Priority.EMERGENCY,"냉각불량",    "냉각 불량",
                ldt(2026,4,16), ldt(2026,4,18), 100000,300000,120000,  520000, 52000, "AS-2026-006");
        createRepair(store1, espresso1,owner1, engineer1, brand1Id, Priority.NORMAL,   "소음발생",    "압력 이상",
                ldt(2026,5,10), ldt(2026,5,12), 70000, 250000, 0,      320000, 32000, "AS-2026-007");
        createRepair(store2, kiosk2,   owner2, engineer2, brand2Id, Priority.NORMAL,   "화면손상",    "프린터 불량",
                ldt(2026,5,23), ldt(2026,5,25), 75000, 160000, 0,      235000, 23500, "AS-2026-008");
        createRepair(store1, kiosk1,   owner1, engineer1, brand1Id, Priority.EMERGENCY,"전원불량",    "전원 불량",
                ldt(2026,6,6),  ldt(2026,6,8),  60000, 180000, 72000,  312000, 31200, "AS-2026-009");
        createRepair(store2, fridge2,  owner2, engineer2, brand2Id, Priority.NORMAL,   "냉각불량",    "온도 오류",
                ldt(2026,6,18), ldt(2026,6,20), 85000, 220000, 0,      305000, 30500, "AS-2026-010");
        createRepair(store1, espresso1,owner1, engineer2, brand1Id, Priority.NORMAL,   "누수발생",    "스팀 불량",
                ldt(2026,6,30), ldt(2026,7,1),  55000, 160000, 0,      215000, 21500, "AS-2026-011");
    }

    private void createRepair(Store store, Equipment equipment, User owner, User engineer,
                               Long brandId, Priority priority, String faultCategory, String symptom,
                               LocalDateTime requestedAt, LocalDateTime completedAt,
                               int labor, int parts, int emergency, int billed, int vat,
                               String invoiceNo) {
        // 1. A/S 접수 (완료 상태)
        AsRequest asRequest = asRequestRepository.save(AsRequest.builder()
                .store(store)
                .equipment(equipment)
                .requester(owner)
                .symptom(symptom)
                .faultCategory(faultCategory)
                .priority(priority)
                .status(AsStatus.COMPLETED)
                .requestedAt(requestedAt)
                .build());

        // 2. 배정 (완료 상태, 과거 시각 포함)
        Assignment assignment = assignmentRepository.save(Assignment.builder()
                .asRequest(asRequest)
                .engineer(engineer)
                .totalScore(new BigDecimal("78.50"))
                .assignMethod(AssignMethod.MANUAL_ACCEPT)
                .status(DispatchStatus.COMPLETED)
                .assignedAt(requestedAt.plusHours(1))
                .acceptedAt(requestedAt.plusHours(1))
                .completedAt(completedAt)
                .build());

        // 3. 정비 리포트
        RepairReport report = repairReportRepository.save(RepairReport.builder()
                .assignmentId(assignment.getId())
                .asRequestId(asRequest.getId())
                .engineerId(engineer.getId())
                .equipmentId(equipment.getId())
                .laborCost(new BigDecimal(labor))
                .diagnosis("점검 완료. 부품 교체 후 정상 작동 확인.")
                .build());

        // 4. Settlement — created_at을 완료 시각으로 지정해야 하므로 JDBC로 직접 삽입
        // payout_amount = billed_amount (더미 데이터용 근사값)
        String dt = completedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        jdbcTemplate.update(
                "INSERT INTO settlements (report_id, brand_id, engineer_id, invoice_number, " +
                "labor_cost, parts_cost, emergency_fee, commission_amount, billed_amount, vat_amount, " +
                "payout_amount, verification_status, approval_status, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'VERIFIED', 'APPROVED', ?, ?)",
                report.getId(), brandId, engineer.getId(), invoiceNo,
                new BigDecimal(labor), new BigDecimal(parts),
                new BigDecimal(emergency), new BigDecimal(emergency),
                new BigDecimal(billed), new BigDecimal(vat),
                new BigDecimal(billed),
                dt, dt);
    }

    private Equipment byCategory(List<Equipment> list, EquipmentCategory cat) {
        return list.stream()
                .filter(e -> e.getCategory() == cat)
                .findFirst()
                .orElse(list.get(0));
    }

    private LocalDateTime ldt(int year, int month, int day) {
        return LocalDateTime.of(year, month, day, 10, 0);
    }

}
