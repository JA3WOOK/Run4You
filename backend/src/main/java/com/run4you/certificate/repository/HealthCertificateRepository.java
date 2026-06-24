package com.run4you.certificate.repository;

import com.run4you.certificate.entity.HealthCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthCertificateRepository extends JpaRepository<HealthCertificate, Long> {

    List<HealthCertificate> findByEquipmentIdOrderByIssuedAtDesc(Long equipmentId);

    List<HealthCertificate> findAllByOrderByIssuedAtDesc();
}
