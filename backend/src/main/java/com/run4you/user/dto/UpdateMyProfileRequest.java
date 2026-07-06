package com.run4you.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateMyProfileRequest {
    private String name;
    private String phone;
    private String currentPassword;

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String newPassword;
}
