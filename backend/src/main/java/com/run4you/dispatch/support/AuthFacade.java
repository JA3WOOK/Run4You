package com.run4you.dispatch.support;

import com.run4you.dispatch.port.UserLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 현재 인증 사용자 식별 헬퍼.
 *
 * <p>인증 도메인 ① 의 JWT principal 이 email 인 경우 users 테이블에서 user_id 로 해석한다.
 * dev 하네스(X-USER-ID)처럼 principal 이 숫자면 그대로 사용한다.
 */
@Component
@RequiredArgsConstructor
public class AuthFacade {

    private final UserLookupPort userLookup;

    public Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("인증 정보가 없습니다.");
        }
        String principal = auth.getName();
        // 숫자면 그대로(dev 하네스 X-USER-ID 호환), 이메일이면 users 조회
        try {
            return Long.valueOf(principal);
        } catch (NumberFormatException e) {
            return userLookup.findIdByEmail(principal);
        }
    }
}