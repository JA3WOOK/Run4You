package com.run4you.asrequest.service;

import com.run4you.asrequest.dto.*;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.entity.AsStatus;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.dispatch.domain.DispatchStatus;
import com.run4you.dispatch.repository.DispatchStatusHistoryRepository;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.entity.EquipmentStatus;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.matching.entity.Assignment;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.repository.AssignmentRepository;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.report.entity.RepairReport;
import com.run4you.report.repository.RepairReportPartsRepository;
import com.run4you.report.repository.RepairReportRepository;
import com.run4you.settlement.entity.Settlement;
import com.run4you.settlement.repository.SettlementRepository;
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
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AsRequestService {

    private final AsRequestRepository asRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    private final RepairReportRepository repairReportRepository;
    private final AssignmentRepository assignmentRepository;
    private final SettlementRepository settlementRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final RepairReportPartsRepository repairReportPartsRepository;
    private final DispatchStatusHistoryRepository dispatchStatusHistoryRepository;

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

    // A/S 접수 생성
    public AsRequestResponseDto createAsRequest(AsRequestCreateDto createDto){

        // 기자재 존재 여부 확인
        Equipment equipment = equipmentRepository.findById(createDto.getEquipmentId())
                .orElseThrow(() -> new IllegalArgumentException("기자재를 찾을 수 없습니다."));

        // 정상 상태인 기자재만 접수 가능
        if (equipment.getStatus() != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("정상 상태의 기자재만 A/S 접수가 가능합니다.");
        }

        // 중복 접수 확인
        boolean hasActiveRequest  = asRequestRepository
                .existsActiveByEquipmentId(createDto.getEquipmentId());
        if(hasActiveRequest){
            throw new IllegalArgumentException("이미 진행 중인 접수가 있습니다.");
        }

        // 점수자 조회
        User requester = getCurrentUser();
        Store store = getCurrentStore(requester);

        // 기자재가 현재 매장 것인지 확인
        if (!equipment.getStore().getId().equals(store.getId())) {
            throw new IllegalStateException("이 기자재에 접근할 수 없습니다.");
        }

        // A/S 접수 생성
        AsRequest asRequest = AsRequest.builder()
                .equipment(equipment)
                .store(store)
                .requester(requester)
                .priority(createDto.getPriority())
                .errorCode(createDto.getErrorCode())
                .symptom(createDto.getSymptom())
                .requestedAt(LocalDateTime.now())
                .build();

        AsRequest saved = asRequestRepository.save(asRequest);

        // 기자재 현황 화면 - 상태 변경 (OPERATIONAL -> FAULTY)
        equipment.updateStatus(EquipmentStatus.FAULTY);

        return AsRequestResponseDto.builder()
                .id(saved.getId())
                .priority(saved.getPriority())
                .status(saved.getStatus())
                .requestedAt(saved.getRequestedAt())
                .equipmentId(equipment.getId())
                .equipmentName(equipment.getName())
                .build();
    }

    // 고장 기자재의 진행 중인 A/S 접수 상세 조회
    @Transactional(readOnly = true)
    public AsRequestResponseDto getActiveAsRequestByEquipment(Long equipmentId) {

        // 본인 매장 기자재인지 확인
        User requester = getCurrentUser();
        Store store = getCurrentStore(requester);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("기자재를 찾을 수 없습니다."));

        if (!equipment.getStore().getId().equals(store.getId())) {
            throw new IllegalStateException("이 기자재에 접근할 수 없습니다.");
        }

        // 진행 중인 접수 조회 (완료/취소가 아닌 가장 최근 건)
        AsRequest asRequest = asRequestRepository.findActiveByEquipmentId(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("진행 중인 접수가 없습니다."));

        return AsRequestResponseDto.builder()
                .id(asRequest.getId())
                .priority(asRequest.getPriority())
                .status(asRequest.getStatus())
                .requestedAt(asRequest.getRequestedAt())
                .equipmentId(equipment.getId())
                .equipmentName(equipment.getName())
                .errorCode(asRequest.getErrorCode())
                .symptom(asRequest.getSymptom())
                .build();
    }

    // 진단서 및 영수증 목록 조회
    @Transactional(readOnly = true)
    public ReceiptListResponseDto getReceipts(ReceiptSearchDto searchDto){

        // 현재 로그인한 점주
        User requester = getCurrentUser();

        // LocalDate -> LocalDateTime 변환
        LocalDateTime startDateTime = (searchDto.getStartDate() != null)
                ? searchDto.getStartDate().atStartOfDay()
                : null;

        LocalDateTime endDateTime = (searchDto.getEndDate() != null)
                ? searchDto.getEndDate().atTime(LocalTime.MAX)
                : null;

        // 조회
        List<ReceiptListResponseDto.ReceiptItemDto> items =
                asRequestRepository.findReceiptsByRequesterId(
                        requester.getId(),
                        AsStatus.COMPLETED,
                        DispatchStatus.REPAIRING,
                        startDateTime,
                        endDateTime,
                        searchDto.getCategory()
                );

        return ReceiptListResponseDto.builder()
                .receipts(items)
                .build();
    }

    // 진단서 및 영수증 상세 조회
    @Transactional(readOnly = true)
    public ReceiptDetailResponseDto getReceiptDetail(Long asRequestId) {

        User requester = getCurrentUser();

        // 1. 접수 + 기자재
        AsRequest asRequest = asRequestRepository.findById(asRequestId)
                .orElseThrow(() -> new IllegalArgumentException("접수를 찾을 수 없습니다."));

        // 본인 접수인지 확인
        if (!asRequest.getRequester().getId().equals(requester.getId())) {
            throw new IllegalStateException("이 영수증에 접근할 수 없습니다.");
        }

        // 2. 배정 (엔지니어 포함)
        Assignment assignment = assignmentRepository
                .findByAsRequestIdWithEngineer(asRequestId)
                .orElse(null);

        // 3. 리포트 (진단, 금액)
        RepairReport report = repairReportRepository
                .findByAsRequestId(asRequestId)
                .orElse(null);

        // 4. 엔지니어 평점
        BigDecimal engineerRating = null;
        String engineerName = null;
        if (assignment != null && assignment.getEngineer() != null) {
            engineerName = assignment.getEngineer().getName();
            engineerRating = engineerProfileRepository
                    .findByUserId(assignment.getEngineer().getId())
                    .map(EngineerProfile::getRating)
                    .orElse(null);
        }

        // 5. 수리 시작 시각 (REPAIRING)
        LocalDateTime startTime = null;
        if (assignment != null) {
            startTime = dispatchStatusHistoryRepository
                    .findStartTime(assignment.getId(), DispatchStatus.REPAIRING)
                    .orElse(null);
        }

        // 6. 정산 + 부품 (리포트가 있을 때만)
        String invoiceNumber = null;
        String pdfUrl = null;
        BigDecimal commissionAmount = null;
        BigDecimal vatAmount = null;
        BigDecimal billedAmount = null;
        List<ReceiptDetailResponseDto.PartItemDto> parts = List.of();

        if (report != null) {
            // 정산
            Settlement settlement = settlementRepository
                    .findByReportId(report.getId())
                    .orElse(null);
            if (settlement != null) {
                invoiceNumber = settlement.getInvoiceNumber();
                pdfUrl = settlement.getPdfUrl();
                commissionAmount = settlement.getCommissionAmount();
                vatAmount = settlement.getVatAmount();
                billedAmount = settlement.getBilledAmount();
            }

            // 부품 목록 + amount(단가×수량) 계산
            parts = repairReportPartsRepository.findPartsByReportId(report.getId())
                    .stream()
                    .map(p -> ReceiptDetailResponseDto.PartItemDto.builder()
                            .partCode(p.getPartCode())
                            .partName(p.getPartName())
                            .quantity(p.getQuantity())
                            .unitPrice(p.getUnitPrice())
                            .amount(p.getUnitPrice()
                                    .multiply(BigDecimal.valueOf(p.getQuantity())))
                            .build())
                    .toList();
        }

        return ReceiptDetailResponseDto.builder()
                .asRequestId(asRequest.getId())
                .invoiceNumber(invoiceNumber)
                .pdfUrl(pdfUrl)
                .status(asRequest.getStatus())
                .equipmentName(asRequest.getEquipment().getName())
                .modelName(asRequest.getEquipment().getModelName())
                .engineerName(engineerName)
                .engineerRating(engineerRating)
                .startTime(startTime)
                .endTime(assignment != null ? assignment.getCompletedAt() : null)
                .diagnosis(report != null ? report.getDiagnosis() : null)
                .parts(parts)
                .laborCost(report != null ? report.getLaborCost() : null)
                .partsCost(report != null ? report.getPartsCost() : null)
                .commissionAmount(commissionAmount)
                .vatAmount(vatAmount)
                .billedAmount(billedAmount)
                .build();
    }
}