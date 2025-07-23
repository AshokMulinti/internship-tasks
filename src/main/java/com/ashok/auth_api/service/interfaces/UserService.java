package com.ashok.auth_api.service.interfaces;

import com.ashok.auth_api.dto.LoginRequestDTO;
import com.ashok.auth_api.dto.LoginResponseDTO;
import com.ashok.auth_api.dto.SignupRequestDTO;
import com.ashok.auth_api.dto.SignupResponseDTO;
import com.ashok.auth_api.utils.ApiResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    ApiResponse<SignupResponseDTO> register(SignupRequestDTO dto);
    ApiResponse<LoginResponseDTO>login(LoginRequestDTO dto);
    ApiResponse<String> registerUsersFromExcel(MultipartFile file);
    ApiResponse<String> registerUsersFromCSV(MultipartFile file);
}
