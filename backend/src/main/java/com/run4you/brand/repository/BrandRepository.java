package com.run4you.brand.repository;

import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean existsByBusinessNo(String businessNo);
    java.util.Optional<Brand> findByBusinessNo(String businessNo);
    List<Brand> findAllByStatus(BrandStatus status);
}
