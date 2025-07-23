package com.ashok.auth_api.controller;

import com.ashok.auth_api.dto.LoginRequestDTO;
import com.ashok.auth_api.dto.LoginResponseDTO;
import com.ashok.auth_api.dto.SignupRequestDTO;
import com.ashok.auth_api.dto.SignupResponseDTO;
import com.ashok.auth_api.security.JwtUtil;
import com.ashok.auth_api.service.interfaces.UserService;
import com.ashok.auth_api.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



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
    public ResponseEntity<String> dashboard(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok("Welcome to dashboard, " + username);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing token");
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

}