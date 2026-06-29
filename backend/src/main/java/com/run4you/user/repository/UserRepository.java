package com.run4you.user.repository;

import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByStatus(UserStatus status);
    List<User> findAllByStatusAndBrandId(UserStatus status, Long brandId);
    Optional<User> findByBrandIdAndRole(Long brandId, Role role);
    long countByRoleAndStatus(Role role, UserStatus status);
}
