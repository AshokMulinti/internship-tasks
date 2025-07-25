package com.ashok.auth_api.dto;

public record PatchUserRequestDTO(String username,
                                  String email,
                                  String password) {
}
