package com.run4you.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordPolicyValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return true; // 필수 여부는 @NotBlank로 별도 검증
        }

        int typeCount = 0;
        if (password.chars().anyMatch(Character::isUpperCase)) typeCount++;
        if (password.chars().anyMatch(Character::isLowerCase)) typeCount++;
        if (password.chars().anyMatch(Character::isDigit)) typeCount++;
        if (password.chars().anyMatch(c -> !Character.isLetterOrDigit(c))) typeCount++;

        int length = password.length();

        if (typeCount >= 3) return length >= 8;
        if (typeCount == 2) return length >= 10;
        return false;
    }
}
