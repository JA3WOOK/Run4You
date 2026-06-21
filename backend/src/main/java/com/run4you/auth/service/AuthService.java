package com.run4you.auth.service;

import com.run4you.auth.dto.LoginRequest;
import com.run4you.auth.dto.SignupRequest;
import com.run4you.auth.dto.TokenResponse;
import com.run4you.auth.security.JwtProvider;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import com.run4you.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;

    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .phone(request.getPhone())
                .role(request.getRole())
                .status(UserStatus.PENDING)
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("승인 대기 중인 계정입니다.");
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getEmail());

        redisTemplate.opsForValue().set(
                "refresh:" + user.getEmail(),
                refreshToken,
                Duration.ofMillis(jwtProvider.getRefreshTokenExpiry())
        );

        return new TokenResponse(accessToken, refreshToken);
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

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
