package com.run4you.user.dto;

import com.run4you.matching.entity.EngineerProfile;
import com.run4you.matching.entity.EngineerSpecialty;
import com.run4you.user.entity.Role;
import com.run4you.user.entity.User;
import com.run4you.user.entity.UserStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class MyProfileResponse {

    private final Long id;
    private final String email;
    private final String name;
    private final String phone;
    private final Role role;
    private final UserStatus status;
    private final Long brandId;
    private final String brandName;
    private final List<String> specialties;
    private final BigDecimal rating;
    private final String skillGrade;

    public MyProfileResponse(User user, EngineerProfile profile, String brandName) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.status = user.getStatus();
        this.brandId = user.getBrandId();
        this.brandName = brandName;
        if (profile != null) {
            this.specialties = profile.getSpecialties().stream()
                    .map(EngineerSpecialty::getCategory)
                    .toList();
            this.rating = profile.getRating();
            this.skillGrade = profile.getSkillGrade();
        } else {
            this.specialties = List.of();
            this.rating = null;
            this.skillGrade = null;
        }
    }
}
