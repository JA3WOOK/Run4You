package com.run4you.user.dto;

import com.run4you.common.validation.ValidPassword;
import lombok.Getter;

@Getter
public class UpdateMyProfileRequest {
    private String name;
    private String phone;
    private String currentPassword;

    @ValidPassword
    private String newPassword;
}
