package com.run4you.part.service;

import com.run4you.part.entity.Parts;
import com.run4you.part.repository.PartsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartsRepository partsRepository;

    @Transactional(readOnly = true)
    public List<Parts> findAll() {
        return partsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Parts> findByCategory(String category) {
        return partsRepository.findByCategory(category);
    }

    /** 부품 코드로 조회 — 없으면 예외(팀 GlobalExceptionHandler가 400 처리). 리포트 단가 검증에서 사용. */
    @Transactional(readOnly = true)
    public Parts getByCode(String partCode) {
        return partsRepository.findByPartCode(partCode)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 부품 코드입니다: " + partCode));
    }
}
