package com.ashok.auth_api.controller;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.security.JwtUtil;
import com.ashok.auth_api.service.interfaces.UserService;
import com.ashok.auth_api.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(@RequestBody SignupRequestDTO dto) {
       ApiResponse<SignupResponseDTO> response = userService.register(dto);
       return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO dto) {
        ApiResponse<LoginResponseDTO> resp = userService.login(dto);
        return ResponseEntity.status(resp.getStatus()).body(resp);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> dashboard(@RequestHeader("Authorization") String authHeader) {
        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData(authHeader);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    //@PostMapping("/upload-excel")
    @PostMapping(value = "/upload-excel", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> registerFromExcel(
            @Parameter(description = "Upload Excel file") @RequestParam("file") MultipartFile file) {
        ApiResponse<String> response = userService.registerUsersFromExcel(file);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    //@PostMapping("/upload-csv")
    @PostMapping(value = "/upload-csv", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> registerFromCSV(@Parameter(description = "upload CSV file") @RequestParam("file") MultipartFile file){
        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<DeleteUserResponseDTO>> deleteUser(@PathVariable Long id){
        ApiResponse<DeleteUserResponseDTO> response = userService.deleteUser(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<EditUserResponseDTO>> editUser(@PathVariable Long id,
                                                                     @RequestBody EditUserRequestDTO dto) {
        ApiResponse<EditUserResponseDTO> response = userService.editUserById(id, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<ViewUserResponseDTO>> getUserById(@PathVariable Long id) {
        ApiResponse<ViewUserResponseDTO> response = userService.getUserById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }


}