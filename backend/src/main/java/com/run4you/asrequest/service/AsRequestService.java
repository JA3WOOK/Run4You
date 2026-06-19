package com.run4you.asrequest.service;

import com.run4you.asrequest.dto.AsRequestCreateDto;
import com.run4you.asrequest.dto.AsRequestResponseDto;
import com.run4you.asrequest.entity.AsRequest;
import com.run4you.asrequest.repository.AsRequestRepository;
import com.run4you.equipment.entity.Equipment;
import com.run4you.equipment.enums.EquipmentStatus;
import com.run4you.equipment.repository.EquipmentRepository;
import com.run4you.store.entity.Store;
import com.run4you.store.repository.StoreRepository;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AsRequestService {

    private final AsRequestRepository asRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // 현재 로그인한 유저 조회
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 현재 로그인한 점주의 매장 조회
    private Store getCurrentStore() {
        User user = getCurrentUser();
        return storeRepository.findByOwnerId(user.getId())
                .orElseThrow(() -> new RuntimeException("매장을 찾을 수 없습니다."));
    }

    // A/S 접수 생성
    public AsRequestResponseDto createAsRequest(AsRequestCreateDto createDto){

        // 기자재 존재 여부 확인
        Equipment equipment = equipmentRepository.findById(createDto.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("기자재를 찾을 수 없습니다."));

        // 중복 접수 확인
        boolean hasActiveRequest  = asRequestRepository
                .existsActiveByEquipmentId(createDto.getEquipmentId());
        if(hasActiveRequest){
            throw new RuntimeException("이미 진행 중인 접수가 있습니다.");
        }

        // 매장 조회
        Store store = getCurrentStore();

        // 점수자 조회
        User requester = getCurrentUser();

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
}
