package com.ashok.auth_api.dto;

public record SignupRequestDTO(
        String username,
        String email,
        String password
) {
}
