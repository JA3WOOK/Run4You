package com.run4you.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * X-USER-ID / X-USER-ROLE 헤더를 읽어 SecurityContext 에 인증 주체를 채운다. (dev 전용)
 *
 * <p>AuthFacade 는 principal name 을 user_id 로 해석하므로 principal 에 userId 문자열을 넣고,
 * @PreAuthorize("hasRole('ENGINEER')") 검증을 위해 ROLE_{role} 권한을 부여한다.
 *
 * <p>예: -H "X-USER-ID: 4" -H "X-USER-ROLE: ENGINEER"
 */
@Component
@Profile("dev")
public class DevAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String userId = req.getHeader("X-USER-ID");
        String role = req.getHeader("X-USER-ROLE");

        if (userId != null && !userId.isBlank()) {
            String r = (role == null || role.isBlank()) ? "ENGINEER" : role.trim().toUpperCase();
            var auth = new UsernamePasswordAuthenticationToken(
                    userId.trim(), null, List.of(new SimpleGrantedAuthority("ROLE_" + r)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(req, res);
    }
}
