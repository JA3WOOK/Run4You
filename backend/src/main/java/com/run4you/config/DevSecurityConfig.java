package com.run4you.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * dev 전용 보안 설정.
 *
 * <p>인증 도메인(①)이 아직 없으므로 JWT 대신 헤더(X-USER-ID / X-USER-ROLE)로 인증을 흉내 낸다.
 * @PreAuthorize 역할 검증은 @EnableMethodSecurity 로 그대로 유지된다.
 * 실제 배포에서는 ① 도메인의 보안 설정으로 대체한다.
 */
@Configuration
@Profile("dev")
@EnableMethodSecurity
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain devFilterChain(HttpSecurity http, DevAuthFilter devAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .addFilterBefore(devAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
