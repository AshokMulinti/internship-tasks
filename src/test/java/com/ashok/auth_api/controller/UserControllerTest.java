package com.ashok.auth_api.controller;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.service.interfaces.UserService;
import com.ashok.auth_api.utils.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testSignup() throws Exception {
        SignupRequestDTO request = new SignupRequestDTO("john", "john@mail.com", "password");
        SignupResponseDTO responseDTO = new SignupResponseDTO(1L, "john", "john@mail.com");
        ApiResponse<SignupResponseDTO> response = new ApiResponse<>(201, "user registered successfully", responseDTO);

        when(userService.register(request)).thenReturn(response);

        mockMvc.perform(post("/api/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("user registered successfully"));
    }

    @Test
    void testLogin() throws Exception {
        LoginRequestDTO loginDto = new LoginRequestDTO("john@mail.com", "password");
        LoginResponseDTO tokenDto = new LoginResponseDTO("fake-token");
        ApiResponse<LoginResponseDTO> response = new ApiResponse<>(200, "Login successful", tokenDto);

        when(userService.login(loginDto)).thenReturn(response);

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("fake-token"));
    }

    @Test
    void testDashboard() throws Exception {
        UserResponseDTO dto = new UserResponseDTO(1L, "john", "john@mail.com", "hashedPassword");
        ApiResponse<List<UserResponseDTO>> response = new ApiResponse<>(200, "Fetched all users", List.of(dto));

        when(userService.getDashboardData(anyString())).thenReturn(response);

        mockMvc.perform(get("/api/dashboard")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched all users"))
                .andExpect(jsonPath("$.data[0].email").value("john@mail.com"));
    }

    @Test
    void testRegisterFromExcel() throws Exception {
        // Create Excel content with Apache POI
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("email");
        header.createCell(2).setCellValue("password");

        Row row = sheet.createRow(1);
        row.createCell(0).setCellValue("john");
        row.createCell(1).setCellValue("john@example.com");
        row.createCell(2).setCellValue("pass123");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        byte[] excelBytes = outputStream.toByteArray();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                excelBytes
        );

        ApiResponse<String> response = new ApiResponse<>(200, "Successfully registered: 1, Skipped: 0", null);
        when(userService.registerUsersFromExcel(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/upload-excel").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully registered: 1, Skipped: 0"));
    }


    @Test
    void testRegisterFromCSV() throws Exception {
        String csvContent = "username,email,password\njohn,john@example.com,pass123";
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ApiResponse<String> response = new ApiResponse<>(200, "Successfully registered: 1, Skipped: 0", null);
        when(userService.registerUsersFromCSV(any())).thenReturn(response);

        mockMvc.perform(multipart("/api/upload-csv").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully registered: 1, Skipped: 0"));
    }


    @Test
    void testDeleteUser() throws Exception {
        DeleteUserResponseDTO dto = new DeleteUserResponseDTO(1L, "john", "john@mail.com", "pwd");
        ApiResponse<DeleteUserResponseDTO> response = new ApiResponse<>(200, "User deleted successfully", dto);

        when(userService.deleteUser(eq(1L))).thenReturn(response);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    void testEditUser() throws Exception {
        EditUserRequestDTO req = new EditUserRequestDTO("john", "john@mail.com", "newpass");
        EditUserResponseDTO dto = new EditUserResponseDTO(1L, "john", "john@mail.com");
        ApiResponse<EditUserResponseDTO> response = new ApiResponse<>(200, "User updated successfully", dto);

        when(userService.editUserById(eq(1L), any())).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    void testGetUserById() throws Exception {
        ViewUserResponseDTO dto = new ViewUserResponseDTO(1L, "john", "john@mail.com");
        ApiResponse<ViewUserResponseDTO> response = new ApiResponse<>(200, "User fetched successfully", dto);

        when(userService.getUserById(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("john"));
    }

    @Test
    void testPatchUser() throws Exception {
        PatchUserRequestDTO req = new PatchUserRequestDTO("john", "john@mail.com", "pass");
        EditUserResponseDTO dto = new EditUserResponseDTO(1L, "john", "john@mail.com");
        ApiResponse<EditUserResponseDTO> response = new ApiResponse<>(200, "User patched successfully", dto);

        when(userService.patchUserById(eq(1L), any())).thenReturn(response);

        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User patched successfully"));
    }
}
