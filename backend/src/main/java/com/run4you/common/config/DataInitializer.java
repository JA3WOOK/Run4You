package com.run4you.common.config;

import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import com.run4you.brand.repository.BrandRepository;
import com.run4you.common.enums.AvailabilityStatus;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.entity.EquipmentCategory;
import com.run4you.equipment.entity.EquipmentStatus;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.repository.EngineerProfileRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final StoreRepository storeRepository;
    private final EquipmentRepository equipmentRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
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

    }

}
