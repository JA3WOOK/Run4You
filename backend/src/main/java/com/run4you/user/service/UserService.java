package com.run4you.user.service;

import com.run4you.user.dto.UserResponse;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers(String email) {
        User requester = findUserByEmail(email);

        if (requester.getRole() == Role.SUPER_ADMIN) {
            return userRepository.findAllByStatus(UserStatus.PENDING).stream()
                    .map(UserResponse::new)
                    .toList();
        }

        return userRepository.findAllByStatusAndBrandId(UserStatus.PENDING, requester.getBrandId()).stream()
                .map(UserResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(UserResponse::new)
                .toList();
    }

    @Transactional
    public UserResponse approve(Long id, String email) {
        User requester = findUserByEmail(email);
        User target = findUser(id);

        validateAuthority(requester, target);

        if (target.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 사용자가 아닙니다.");
        }
        target.approve();
        return new UserResponse(target);
    }

    @Transactional
    public UserResponse reject(Long id, String email) {
        User requester = findUserByEmail(email);
        User target = findUser(id);

        validateAuthority(requester, target);

        if (target.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 사용자가 아닙니다.");
        }
        target.reject();
        return new UserResponse(target);
    }

    @Transactional
    public UserResponse deactivate(Long id) {
        User target = findUser(id);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new IllegalStateException("플랫폼 총괄 계정은 비활성화할 수 없습니다.");
        }
        target.deactivate();
        return new UserResponse(target);
    }

    @Transactional
    public UserResponse activate(Long id) {
        User target = findUser(id);
        if (target.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalStateException("비활성화된 계정이 아닙니다.");
        }
        target.activate();
        return new UserResponse(target);
    }

    private void validateAuthority(User requester, User target) {
        if (requester.getRole() == Role.BRAND_ADMIN) {
            if (!requester.getBrandId().equals(target.getBrandId())) {
                throw new IllegalStateException("자기 브랜드 소속 사용자만 승인할 수 있습니다.");
            }
        }
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }
}
