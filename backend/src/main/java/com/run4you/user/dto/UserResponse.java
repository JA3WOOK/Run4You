package com.run4you.user.dto;

import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import lombok.Getter;

@Getter
public class UserResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final Role role;
    private final UserStatus status;
    private final Long brandId;
    private final String brandName;

    public UserResponse(User user, String brandName) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.brandId = user.getBrandId();
        this.brandName = brandName;
    }
}
