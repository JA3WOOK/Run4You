package com.run4you.auth.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BrandSignupRequest {

    @NotBlank(message = "브랜드명을 입력해주세요.")
    private String brandName;

    @NotBlank(message = "사업자 번호를 입력해주세요.")
    @Pattern(regexp = "\\d{3}-\\d{2}-\\d{5}", message = "사업자 번호 형식이 올바르지 않습니다. (예: 123-45-67890)")
    private String businessNo;

    @NotNull(message = "수수료율을 입력해주세요.")
    @Positive(message = "수수료율은 0보다 커야 합니다.")
    @DecimalMax(value = "100.0", message = "수수료율은 100을 초과할 수 없습니다.")
    private BigDecimal commissionRate;

    @Email
    @NotBlank(message = "이메일을 입력해주세요.")
    private String adminEmail;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String adminPassword;

    @NotBlank(message = "이름을 입력해주세요.")
    private String adminName;

    private String adminPhone;
}
