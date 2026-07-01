package com.run4you.brand.service;

import com.run4you.brand.dto.BrandResponse;
import com.run4you.brand.dto.BrandUpdateCommissionRequest;
import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import com.run4you.brand.repository.BrandRepository;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BrandResponse> getAll() {
        return brandRepository.findAll().stream()
                .map(BrandResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> getActive() {
        return brandRepository.findAllByStatus(BrandStatus.ACTIVE).stream()
                .map(BrandResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse getById(Long id) {
        return new BrandResponse(findBrand(id));
    }

    @Transactional
    public BrandResponse approve(Long id) {
        Brand brand = findBrand(id);
        if (brand.getStatus() != BrandStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 브랜드가 아닙니다.");
        }
        brand.approve();

        userRepository.findByBrandIdAndRole(id, Role.BRAND_ADMIN)
                .ifPresent(User::approve);

        return new BrandResponse(brand);
    }

    @Transactional
    public BrandResponse reject(Long id) {
        Brand brand = findBrand(id);
        if (brand.getStatus() != BrandStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 브랜드가 아닙니다.");
        }
        userRepository.findByBrandIdAndRole(id, Role.BRAND_ADMIN)
                .ifPresent(userRepository::delete);

        BrandResponse response = new BrandResponse(brand);
        brandRepository.delete(brand);
        return response;
    }

    @Transactional
    public BrandResponse updateCommissionRate(Long id, BrandUpdateCommissionRequest request) {
        Brand brand = findBrand(id);
        brand.updateCommissionRate(request.getCommissionRate());
        return new BrandResponse(brand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = findBrand(id);
        userRepository.findByBrandIdAndRole(id, Role.BRAND_ADMIN)
                .ifPresent(userRepository::delete);
        brandRepository.delete(brand);
    }

    private Brand findBrand(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));
    }
}
