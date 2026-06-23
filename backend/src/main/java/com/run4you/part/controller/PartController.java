package com.run4you.part.controller;

import com.run4you.common.response.ApiResponse;
import com.run4you.part.entity.Parts;
import com.run4you.part.service.PartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 부품 마스터 조회 API. 응답은 팀 공통 포맷(ApiResponse)으로 감싼다.
 */
@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartService partService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Parts>>> list(@RequestParam(required = false) String category) {
        List<Parts> parts = (category == null || category.isBlank())
                ? partService.findAll()
                : partService.findByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(parts));
    }
}
