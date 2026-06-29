package com.run4you.auth.service;

import com.run4you.auth.dto.BrandSignupRequest;
import com.run4you.auth.dto.LoginRequest;
import com.run4you.auth.dto.SignupRequest;
import com.run4you.auth.dto.TokenResponse;
import com.run4you.auth.security.JwtProvider;
import com.run4you.brand.entity.Brand;
import com.run4you.brand.entity.BrandStatus;
import com.run4you.brand.repository.BrandRepository;
import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.entity.EngineerSpecialty;
import com.run4you.matching.repository.EngineerProfileRepository;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BrandRepository brandRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        if (request.getRole() == Role.SUPER_ADMIN || request.getRole() == Role.BRAND_ADMIN) {
            throw new IllegalArgumentException("해당 역할로는 가입할 수 없습니다.");
        }

        if ((request.getRole() == Role.STORE_OWNER || request.getRole() == Role.ENGINEER)
                && request.getBrandId() == null) {
            throw new IllegalArgumentException("브랜드를 선택해주세요.");
        }

        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 브랜드입니다."));
            if (brand.getStatus() != BrandStatus.ACTIVE) {
                throw new IllegalArgumentException("승인된 브랜드가 아닙니다.");
            }
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .brandId(request.getBrandId())
                .build();

        User savedUser = userRepository.save(user);

        if (request.getRole() == Role.ENGINEER) {
            EngineerProfile profile = EngineerProfile.builder()
                    .user(savedUser)
                    .build();
            EngineerProfile savedProfile = engineerProfileRepository.save(profile);

            List<String> specialties = request.getSpecialties();
            if (specialties != null && !specialties.isEmpty()) {
                for (String category : specialties) {
                    savedProfile.getSpecialties().add(EngineerSpecialty.of(savedProfile, category));
                }
                engineerProfileRepository.save(savedProfile);
            }
        }
    }

    @Transactional
    public void signupBrand(BrandSignupRequest request) {
        if (brandRepository.existsByBusinessNo(request.getBusinessNo())) {
            throw new IllegalArgumentException("이미 등록된 사업자 번호입니다.");
        }

        if (userRepository.existsByEmail(request.getAdminEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        Brand brand = Brand.builder()
                .name(request.getBrandName())
                .businessNo(request.getBusinessNo())
                .commissionRate(request.getCommissionRate())
                .status(BrandStatus.PENDING)
                .build();

        Brand savedBrand = brandRepository.save(brand);

        User admin = User.builder()
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .name(request.getAdminName())
                .phone(request.getAdminPhone())
                .role(Role.BRAND_ADMIN)
                .status(UserStatus.PENDING)
                .brandId(savedBrand.getId())
                .build();

        userRepository.save(admin);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() == UserStatus.PENDING) {
            throw new IllegalStateException("승인 대기 중인 계정입니다.");
        }
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new IllegalStateException("가입이 거절된 계정입니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        redisTemplate.opsForValue().set(
                "refresh:" + user.getEmail(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())
        );

        return new TokenResponse(accessToken, refreshToken, user.getName());
    }

    public void logout(String email) {
        redisTemplate.delete("refresh:" + email);
    }

    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        String email = jwtProvider.getEmail(refreshToken);
        String stored = redisTemplate.opsForValue().get("refresh:" + email);

        if (!refreshToken.equals(stored)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        String newAccessToken = jwtProvider.generateAccessToken(email, user.getRole().name());
        String newRefreshToken = jwtProvider.generateRefreshToken(email);

        redisTemplate.opsForValue().set(
                "refresh:" + email,
                newRefreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())
        );

        return new TokenResponse(newAccessToken, newRefreshToken, user.getName());
    }
}
