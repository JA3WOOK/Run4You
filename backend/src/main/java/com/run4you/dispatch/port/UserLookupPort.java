package com.run4you.dispatch.port;

/** users 테이블에서 email → user_id 해석 포트 (인증 ① principal=email 규약 대응). */
public interface UserLookupPort {
    Long findIdByEmail(String email);
}