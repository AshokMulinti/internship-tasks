package com.ashok.auth_api.service.interfaces;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.utils.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    ApiResponse<SignupResponseDTO> register(SignupRequestDTO dto);
    ApiResponse<LoginResponseDTO>login(LoginRequestDTO dto);
    ApiResponse<String> registerUsersFromExcel(MultipartFile file);
    ApiResponse<String> registerUsersFromCSV(MultipartFile file);
    ApiResponse<List<UserResponseDTO>> getDashboardData(String authHeader);

    ApiResponse<DeleteUserResponseDTO> deleteUser(Long id);

    ApiResponse<EditUserResponseDTO> editUserById(Long id, EditUserRequestDTO dto);

    ApiResponse<ViewUserResponseDTO> getUserById(Long id);
    ApiResponse<EditUserResponseDTO> patchUserById(Long id, PatchUserRequestDTO dto);

}
