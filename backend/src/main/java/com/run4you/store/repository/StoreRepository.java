package com.run4you.store.repository;

import com.run4you.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    // owner_id로 매장 조회
    @Query("SELECT s FROM Store s WHERE s.owner.id = :ownerId")
    Optional<Store> findByOwnerId(@Param("ownerId") Long ownerId);
}
