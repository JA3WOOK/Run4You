package com.run4you.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  대문자/소문자/숫자/특수문자 중 2종류 이상 조합 시 10자 이상,
  3종류 이상 조합 시 8자 이상 (개인정보보호위원회 안전성 확보조치 기준)
  null/빈 문자열은 통과시킴 — 필수 여부는 @NotBlank로 별도 검증
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordPolicyValidator.class)
public @interface ValidPassword {
    String message() default "비밀번호는 대문자/소문자/숫자/특수문자 중 2종류 이상 조합 시 10자 이상, 3종류 이상 조합 시 8자 이상이어야 합니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
