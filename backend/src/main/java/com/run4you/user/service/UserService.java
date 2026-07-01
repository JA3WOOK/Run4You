package com.run4you.user.service;

import com.run4you.brand.repository.BrandRepository;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.user.dto.MyProfileResponse;
import com.run4you.user.dto.UpdateMyProfileRequest;
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
    private final EngineerProfileRepository engineerProfileRepository;
    private final BrandRepository brandRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MyProfileResponse getMe(String email) {
        User user = findUserByEmail(email);
        EngineerProfile profile = user.getRole() == Role.ENGINEER
                ? engineerProfileRepository.findByUserId(user.getId()).orElse(null)
                : null;
        return new MyProfileResponse(user, profile, resolveBrandName(user.getBrandId()));
    }

    @Transactional
    public MyProfileResponse updateMe(String email, UpdateMyProfileRequest request) {
        User user = findUserByEmail(email);
        user.updateProfile(request.getName(), request.getPhone());

        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            if (request.getCurrentPassword() == null
                    || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
            }
            user.changePassword(passwordEncoder.encode(request.getNewPassword()));
        }

        EngineerProfile profile = user.getRole() == Role.ENGINEER
                ? engineerProfileRepository.findByUserId(user.getId()).orElse(null)
                : null;
        return new MyProfileResponse(user, profile, resolveBrandName(user.getBrandId()));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getPendingUsers(String email) {
        User requester = findUserByEmail(email);

        if (requester.getRole() == Role.SUPER_ADMIN) {
            return userRepository.findAllByStatus(UserStatus.PENDING).stream()
                    .map(u -> new UserResponse(u, resolveBrandName(u.getBrandId())))
                    .toList();
        }

        return userRepository.findAllByStatusAndBrandId(UserStatus.PENDING, requester.getBrandId()).stream()
                .map(u -> new UserResponse(u, resolveBrandName(u.getBrandId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponse(u, resolveBrandName(u.getBrandId())))
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
        return new UserResponse(target, resolveBrandName(target.getBrandId()));
    }

    @Transactional
    public UserResponse reject(Long id, String email) {
        User requester = findUserByEmail(email);
        User target = findUser(id);

        validateAuthority(requester, target);

        if (target.getStatus() != UserStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 사용자가 아닙니다.");
        }
        UserResponse response = new UserResponse(target, resolveBrandName(target.getBrandId()));
        userRepository.delete(target);
        return response;
    }

    @Transactional
    public UserResponse deactivate(Long id) {
        User target = findUser(id);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new IllegalStateException("플랫폼 총괄 계정은 비활성화할 수 없습니다.");
        }
        target.deactivate();
        return new UserResponse(target, resolveBrandName(target.getBrandId()));
    }

    @Transactional
    public UserResponse activate(Long id) {
        User target = findUser(id);
        if (target.getStatus() != UserStatus.INACTIVE) {
            throw new IllegalStateException("비활성화된 계정이 아닙니다.");
        }
        target.activate();
        return new UserResponse(target, resolveBrandName(target.getBrandId()));
    }

    @Transactional
    public void deleteUser(Long id) {
        User target = findUser(id);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new IllegalStateException("플랫폼 총괄 계정은 삭제할 수 없습니다.");
        }
        userRepository.delete(target);
    }

    private String resolveBrandName(Long brandId) {
        if (brandId == null) return null;
        return brandRepository.findById(brandId).map(b -> b.getName()).orElse(null);
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
