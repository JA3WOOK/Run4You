package com.run4you.dispatch.support;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 인증 사용자 식별 헬퍼.
 *
 * <p>JWT subject 를 user_id 로 사용한다는 전제(인증 도메인 ① 규약)에 맞춰 구현했다.
 * 인증 도메인이 커스텀 Principal(UserDetails) 을 제공하면 이 한 곳만 교체하면 된다.
 */
@Component
public class AuthFacade {

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("인증 정보가 없습니다.");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("인증 principal 에서 user_id 를 해석할 수 없습니다: " + auth.getName());
        }
    }
}
