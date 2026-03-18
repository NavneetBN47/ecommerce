package com.ecommerce.usermanagement.application.mapper;

import com.ecommerce.usermanagement.application.dto.UserProfileResponse;
import com.ecommerce.usermanagement.application.dto.UserRegistrationResponse;
import com.ecommerce.usermanagement.domain.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    
    public UserRegistrationResponse toRegistrationResponse(User user) {
        return UserRegistrationResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .message("User registered successfully")
            .build();
    }
    
    public UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .phoneNumber(user.getPhoneNumber())
            .address(user.getAddress())
            .role(user.getRole().name())
            .status(user.getStatus().name())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
            .build();
    }
}