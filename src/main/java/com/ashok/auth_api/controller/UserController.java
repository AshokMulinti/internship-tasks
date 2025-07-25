package com.ashok.auth_api.controller;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.security.JwtUtil;
import com.ashok.auth_api.service.interfaces.UserService;
import com.ashok.auth_api.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/signup")
    @Operation(summary = "Register new user", description = "Registers a new user with username, email, and password.")
    public ResponseEntity<ApiResponse<SignupResponseDTO>> signup(@RequestBody SignupRequestDTO dto) {
       ApiResponse<SignupResponseDTO> response = userService.register(dto);
       return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token if successful.")
    public ResponseEntity<ApiResponse<LoginResponseDTO>> login(@RequestBody LoginRequestDTO dto) {
        ApiResponse<LoginResponseDTO> resp = userService.login(dto);
        return ResponseEntity.status(resp.getStatus()).body(resp);
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Fetch dashboard data", description = "Returns a list of all registered users. Requires Authorization header with JWT token.")
    public ResponseEntity<ApiResponse<List<UserResponseDTO>>> dashboard(@RequestHeader("Authorization") String authHeader) {
        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData(authHeader);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    //@PostMapping("/upload-excel")
    @PostMapping(value = "/upload-excel", consumes = "multipart/form-data")
    @Operation(summary = "Upload Excel file", description = "Registers multiple users from uploaded Excel file (.xlsx).")
    public ResponseEntity<ApiResponse<String>> registerFromExcel(
            @Parameter(description = "Upload Excel file") @RequestParam("file") MultipartFile file) {
        ApiResponse<String> response = userService.registerUsersFromExcel(file);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    //@PostMapping("/upload-csv")
    @PostMapping(value = "/upload-csv", consumes = "multipart/form-data")
    @Operation(summary = "Upload CSV file", description = "Registers multiple users from uploaded CSV file.")
    public ResponseEntity<ApiResponse<String>> registerFromCSV(@Parameter(description = "upload CSV file") @RequestParam("file") MultipartFile file){
        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID.")
    public ResponseEntity<ApiResponse<DeleteUserResponseDTO>> deleteUser(@PathVariable Long id){
        ApiResponse<DeleteUserResponseDTO> response = userService.deleteUser(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Edit user", description = "Updates user information by ID.")
    public ResponseEntity<ApiResponse<EditUserResponseDTO>> editUser(@PathVariable Long id,
                                                                     @RequestBody EditUserRequestDTO dto) {
        ApiResponse<EditUserResponseDTO> response = userService.editUserById(id, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @GetMapping("/users/{id}")
    @Operation(summary = "View user by ID", description = "Fetches a single user's details by ID.")
    public ResponseEntity<ApiResponse<ViewUserResponseDTO>> getUserById(@PathVariable Long id) {
        ApiResponse<ViewUserResponseDTO> response = userService.getUserById(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
    @PatchMapping("/users/{id}")
    @Operation(summary = "Patch user", description = "Partially updates user information by ID.")
    public ResponseEntity<ApiResponse<EditUserResponseDTO>> patchUser(
            @PathVariable Long id,
            @RequestBody PatchUserRequestDTO dto) {

        ApiResponse<EditUserResponseDTO> response = userService.patchUserById(id, dto);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

}