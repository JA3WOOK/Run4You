package com.run4you.asrequest.repository;

import com.run4you.asrequest.entity.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ErrorCodeRepository extends JpaRepository<ErrorCode, Long> {

    // 에러코드 값으로 단건 조회 <- 이미 등록된 코드인지 확인 후 재사용할 때 사용
    Optional<ErrorCode> findByCode(String code);

    // 에러코드 존재 여부만 빠르게 확인 <- 신규 저장 여부 판단 용
    boolean existsByCode(String code);
}