package com.run4you.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class BrandSignupRequest {

    @NotBlank(message = "브랜드명을 입력해주세요.")
    private String brandName;

    @NotBlank(message = "사업자 번호를 입력해주세요.")
    private String businessNo;

    @NotNull(message = "수수료율을 입력해주세요.")
    @Positive(message = "수수료율은 0보다 커야 합니다.")
    private BigDecimal commissionRate;

    @Email
    @NotBlank(message = "이메일을 입력해주세요.")
    private String adminEmail;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String adminPassword;

    @NotBlank(message = "이름을 입력해주세요.")
    private String adminName;

    private String adminPhone;
}
