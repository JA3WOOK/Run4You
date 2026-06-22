package com.run4you.equipment.service;

import com.run4you.asrequest.dto.AsRequestHistoryDto;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.equipment.dto.*;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.entity.EquipmentStatus;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.report.entity.RepairReport;
import com.run4you.report.repository.RepairReportRepository;
import com.run4you.store.entity.Store;
import com.run4you.store.repository.StoreRepository;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final AsRequestRepository asRequestRepository;
    private final RepairReportRepository repairReportRepository;
    private final StoreRepository storeRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    // 현재 로그인한 유저 조회
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 현재 로그인한 점주의 매장 조회
    private Store getCurrentStore(User user) {
        return storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("매장을 찾을 수 없습니다."));
    }

    // 1. 기자재 목록 조회 + 카운트
    @Transactional(readOnly = true)
    public EquipmentListResponseDto getEquipmentList(EquipmentSearchDto searchDto){

        User user = getCurrentUser();
        Long storeId = getCurrentStore(user).getId();
        List<Equipment> equipments;

        // 카테고리 필터 + 키워드 검색
        if(searchDto.getCategory() != null && searchDto.getKeyword() != null){
            equipments = equipmentRepository.findActiveByStoreIdAndCategoryAndKeyword(
                    storeId, searchDto.getCategory(), searchDto.getKeyword());
        } else if (searchDto.getCategory() != null) {
            equipments = equipmentRepository.findActiveByStoreIdAndCategory(
                    storeId, searchDto.getCategory());
        } else if (searchDto.getKeyword() != null) {
            equipments = equipmentRepository.findActiveByStoreIdAndKeyword(
                    storeId, searchDto.getKeyword());
        } else {
            // 기본 화면
            equipments = equipmentRepository.findActiveByStoreId(storeId);
        }

        // 상태별 카운트
        int totalCount = equipmentRepository.countByActiveByStoreId(storeId);
        int operationalCount = equipmentRepository.countByStoreIdAndStatus(storeId, EquipmentStatus.OPERATIONAL);
        int faultyCount = equipmentRepository.countByStoreIdAndStatus(storeId, EquipmentStatus.FAULTY);
        int repairingCount = equipmentRepository.countByStoreIdAndStatus(storeId, EquipmentStatus.REPAIRING);

        // N+1 방지 - 매장 기자재의 최근 에러코드 한번에 조회
        Map<Long, String> errorCodeMap = asRequestRepository
                .findLatestErrorCodesByStoreId(storeId)
                .stream()
                .collect(Collectors.toMap(
                        a -> a.getEquipment().getId(),
                        AsRequest::getErrorCode
                ));

        List<EquipmentResponseDto> equipmentDtos = equipments.stream()
                .map(e -> EquipmentResponseDto.builder()
                        .id(e.getId())
                        .category(e.getCategory())
                        .name(e.getName())
                        .modelName(e.getModelName())
                        .serialNo(e.getSerialNo())
                        .status(e.getStatus())
                        .errorCode(errorCodeMap.get(e.getId()))
                        .purchasedAt(e.getPurchasedAt())
                        .nextInspectionDate(e.getNextInspectionDate())
                        .build())
                .toList();

        return EquipmentListResponseDto.builder()
                .totalCount(totalCount)
                .operationalCount(operationalCount)
                .faultyCount(faultyCount)
                .repairingCount(repairingCount)
                .equipments(equipmentDtos)
                .build();
    }

    // 2. 기자재 등록
    public EquipmentResponseDto registerEquipment(EquipmentCreateDto createDto){

        User user = getCurrentUser();
        Store store = getCurrentStore(user);

        Equipment equipment = Equipment.builder()
                .store(store)
                .name(createDto.getName())
                .category(createDto.getCategory())
                .modelName(createDto.getModelName())
                .manufacturer(createDto.getManufacturer())
                .serialNo(createDto.getSerialNo())
                .purchasedAt(createDto.getPurchasedAt())
                .build();

        Equipment saved = equipmentRepository.save(equipment);

        return EquipmentResponseDto.builder()
                .id(saved.getId())
                .category(saved.getCategory())
                .name(saved.getName())
                .modelName(saved.getModelName())
                .serialNo(saved.getSerialNo())
                .status(saved.getStatus())
                .purchasedAt(saved.getPurchasedAt())
                .nextInspectionDate(saved.getNextInspectionDate())
                .build();
    }

    // 3. 이력보기 모달
    @Transactional(readOnly = true)
    public AsRequestHistoryDto getRepairHistory(Long equipmentId){

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("기자재를 찾을 수 없습니다."));

        User user = getCurrentUser();
        Store store = getCurrentStore(user);

        if (!equipment.getStore().getId().equals(store.getId())) {
            throw new IllegalStateException("이 기자재에 접근할 수 없습니다.");
        }

        // 최근 수리일 조회
        LocalDateTime lastRepairAt = assignmentRepository
                .findLastRepairAtByEquipmentId(equipmentId)
                .orElse(null);

        // 수리 이력 조회
        List<AsRequest> asRequests = asRequestRepository
                .findCompletedByEquipmentId(equipmentId);

        // 총 수리 횟수
        int totalRepairCount = asRequestRepository
                .countCompletedByEquipmentId(equipmentId);

        // 총 수리 비용
        BigDecimal totalRepairCost = repairReportRepository
                .sumTotalCostByEquipmentId(equipmentId);

        // N+1 방지 - 수리 이력 ID 목록으로 RepairReport 한번에 조회
        List<Long> asRequestIds = asRequests.stream()
                .map(AsRequest::getId)
                .toList();

        Map<Long, RepairReport> reportMap = repairReportRepository
                .findByAsRequestIds(asRequestIds)
                .stream()
                .collect(Collectors.toMap(
                        RepairReport::getAsRequestId,
                        r -> r
                ));

        // N+1 방지 - asRequestId 목록으로 Assignment 한번에 조회
        Map<Long, LocalDateTime> completedAtMap = assignmentRepository
                .findCompletedByAsRequestIds(asRequestIds)
                .stream()
                .collect(Collectors.toMap(
                        Assignment::getAsRequestId,
                        Assignment::getCompletedAt
                ));

        List<AsRequestHistoryDto.RepairHistoryItem> repairHistoryItems = asRequests.stream()
                .map(a -> {
                    RepairReport report = reportMap.get(a.getId());
                    return AsRequestHistoryDto.RepairHistoryItem.builder()
                            .completedAt(completedAtMap.get(a.getId()))
                            .errorCode(a.getErrorCode())
                            .symptom(a.getSymptom())
                            .totalCost(report != null ? report.getTotalCost() : null)
                            .status(a.getStatus())
                            .diagnosis(report != null ? report.getDiagnosis() : null)
                            .repairReportId(report != null ? report.getId() : null)
                            .build();
                }).toList();

        return AsRequestHistoryDto.builder()
                .name(equipment.getName())
                .modelName(equipment.getModelName())
                .serialNo(equipment.getSerialNo())
                .purchasedAt(equipment.getPurchasedAt())
                .lastRepairAt(lastRepairAt)
                .storeName(equipment.getStore().getName())
                .status(equipment.getStatus())
                .repairHistoryItems(repairHistoryItems)
                .totalRepairCount(totalRepairCount)
                .totalRepairCost(totalRepairCost)
                .build();
    }
}
