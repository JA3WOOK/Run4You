package com.run4you.user.dto;

import lombok.Getter;

@Getter
public class UpdateMyProfileRequest {
    private String name;
    private String phone;
    private String currentPassword;
    private String newPassword;
}
