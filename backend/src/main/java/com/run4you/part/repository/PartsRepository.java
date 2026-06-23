package com.run4you.part.repository;

import com.run4you.part.entity.Parts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartsRepository extends JpaRepository<Parts, Long> {

    Optional<Parts> findByPartCode(String partCode);

    List<Parts> findByCategory(String category);

    boolean existsByPartCode(String partCode);
}
