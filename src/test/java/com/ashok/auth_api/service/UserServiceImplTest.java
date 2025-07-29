package com.ashok.auth_api.service;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.exceptions.GlobalExceptionHandler;
import com.ashok.auth_api.exceptions.InvalidSignupDataException;
import com.ashok.auth_api.exceptions.UserAlreadyExistsException;
import com.ashok.auth_api.model.User;
import com.ashok.auth_api.repository.UserRepository;
import com.ashok.auth_api.security.JwtUtil;
import com.ashok.auth_api.service.implementation.UserServiceImpl;
import com.ashok.auth_api.utils.ApiResponse;
import com.ashok.auth_api.utils.HttpStatusCodes;
import com.ashok.auth_api.utils.ResponseHandler;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ResponseHandler responseHandler;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testRegisterSuccess() {
        // Arrange
        SignupRequestDTO requestDTO = new SignupRequestDTO("john", "john@example.com", "password");

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("hashedPass");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("john");
        savedUser.setEmail("john@example.com");
        savedUser.setPassword("hashedPass");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        SignupResponseDTO responseDTO = new SignupResponseDTO(1L, "john", "john@example.com");
        ApiResponse<SignupResponseDTO> expectedResponse =
                new ApiResponse<>(201, "user registered successfully", responseDTO);

        when(responseHandler.success(any(SignupResponseDTO.class), eq("user registered successfully"), eq(201)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<SignupResponseDTO> actualResponse = userService.register(requestDTO);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(201, actualResponse.getStatus());
        assertEquals("john", actualResponse.getData().username());
        assertEquals("john@example.com", actualResponse.getData().email());
    }

    @Test
    void testRegisterThrowsUserAlreadyExistsException() {
        SignupRequestDTO dto = new SignupRequestDTO("john", "john@example.com", "password");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));
        assertThrows(UserAlreadyExistsException.class, () -> userService.register(dto));
    }
    @Test
    void testRegisterThrowsInvalidSignupDataException() {
        SignupRequestDTO dto = new SignupRequestDTO("", "", "");
        assertThrows(InvalidSignupDataException.class, () -> userService.register(dto));
    }
    @Test
    void testLoginSuccess() {
        // Arrange
        LoginRequestDTO dto = new LoginRequestDTO("john@example.com", "password");

        User user = new User();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("hashedPass"); // stored hashed password

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPass")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token");

        LoginResponseDTO responseDTO = new LoginResponseDTO("jwt-token");
        ApiResponse<LoginResponseDTO> expectedResponse = new ApiResponse<>(200, "Login successful", responseDTO);

        when(responseHandler.success(any(LoginResponseDTO.class), eq("Login successful"), eq(200))).thenReturn(expectedResponse);

        // Act
        ApiResponse<LoginResponseDTO> actualResponse = userService.login(dto);

        // Assert
        assertNotNull(actualResponse);
        assertEquals(200, actualResponse.getStatus());
        assertEquals("jwt-token", actualResponse.getData().token());
    }
    @Test
    void testLoginFailure() {
        LoginRequestDTO dto = new LoginRequestDTO("john@example.com", "wrong");
        User user = new User(1L, "john", "john@example.com", "hashedPass");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashedPass")).thenReturn(false);
        when(responseHandler.error("Invalid email or password", HttpStatusCodes.UNAUTHORIZED))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.UNAUTHORIZED, "Invalid email or password", null));

        ApiResponse<LoginResponseDTO> response = userService.login(dto);
        assertEquals(HttpStatusCodes.UNAUTHORIZED, response.getStatus());
    }

    @Test
    void testRegisterUsersFromExcel_Success() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet();
        var header = sheet.createRow(0);
        header.createCell(0).setCellValue("username");
        header.createCell(1).setCellValue("email");
        header.createCell(2).setCellValue("password");

        var row = sheet.createRow(1);
        row.createCell(0).setCellValue("john");
        row.createCell(1).setCellValue("john@example.com");
        row.createCell(2).setCellValue("123456");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        when(responseHandler.success(eq(null), eq("Successfully registered: 1, Skipped: 0"), eq(HttpStatusCodes.OK)))
                .thenAnswer(invocation -> new ApiResponse<>(HttpStatusCodes.OK, invocation.getArgument(1), null));

        ApiResponse<String> response = userService.registerUsersFromExcel(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Successfully registered: 1, Skipped: 0", response.getMessage());
    }
    @Test
    void testRegisterUsersFromCSV_Success() throws IOException {
        String csv = "username,email,password\njohn,john@example.com,123456";
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenReturn(new User());

        when(responseHandler.success(eq(null), eq("Successfully registered: 1, Skipped: 0"), eq(HttpStatusCodes.OK)))
                .thenAnswer(invocation -> new ApiResponse<>(HttpStatusCodes.OK, invocation.getArgument(1), null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Successfully registered: 1, Skipped: 0", response.getMessage());
    }
    @Test
    void testGetDashboardData_Success() {
        String token = "valid.jwt.token";
        List<User> users = List.of(new User(1L, "john", "john@example.com", "pass"));
        when(jwtUtil.validateToken("jwt-token")).thenReturn(true);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponseDTO> dtos = List.of(new UserResponseDTO(1L, "john", "john@example.com", "pass"));
        when(responseHandler.success(eq(dtos), eq("Fetched all users"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Fetched all users", dtos));

        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData("Bearer jwt-token");
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Fetched all users", response.getMessage());
    }
    @Test
    void testDeleteUser_Success() {
        User user = new User(1L, "john", "john@example.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        DeleteUserResponseDTO dto = new DeleteUserResponseDTO(1L, "john", "john@example.com", "pass");
        when(responseHandler.success(eq(dto), eq("User deleted successfully"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "User deleted successfully", dto));

        ApiResponse<DeleteUserResponseDTO> response = userService.deleteUser(1L);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }
    @Test
    void testEditUserById_Success() {
        EditUserRequestDTO dto = new EditUserRequestDTO("johnny", "johnny@example.com", "newpass");
        User user = new User(1L, "john", "john@example.com", "oldpass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);

        EditUserResponseDTO responseDto = new EditUserResponseDTO(1L, "johnny", "johnny@example.com");
        when(responseHandler.success(eq(responseDto), eq("User updated successfully"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "User updated successfully", responseDto));

        ApiResponse<EditUserResponseDTO> response = userService.editUserById(1L, dto);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }
    @Test
    void testGetUserById_Success() {
        User user = new User(1L, "john", "john@example.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ViewUserResponseDTO dto = new ViewUserResponseDTO(1L, "john", "john@example.com");
        when(responseHandler.success(eq(dto), eq("User fetched successfully"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "User fetched successfully", dto));

        ApiResponse<ViewUserResponseDTO> response = userService.getUserById(1L);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }

    @Test
    void testPatchUserById_Success() {
        PatchUserRequestDTO dto = new PatchUserRequestDTO("newname", null, null);
        User user = new User(1L, "john", "john@example.com", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        EditUserResponseDTO responseDto = new EditUserResponseDTO(1L, "newname", "john@example.com");
        when(responseHandler.success(eq(responseDto), eq("User patched successfully"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "User patched successfully", responseDto));

        ApiResponse<EditUserResponseDTO> response = userService.patchUserById(1L, dto);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }
    @Test
    void testHandleUserAlreadyExistsException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");
        ResponseEntity<ApiResponse<Object>> response = handler.handleDuplicateUser(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }

    @Test
    void testHandleInvalidSignupDataException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        InvalidSignupDataException ex = new InvalidSignupDataException("Missing fields");
        ResponseEntity<ApiResponse<Object>> response = handler.handleInvalidSignUp(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing fields", response.getBody().getMessage());
        assertNull(response.getBody().getData());
    }
    @Test
    void testRegisterUsersFromCSV_IOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("error"));
        when(responseHandler.error("Failed to read CSV file", HttpStatusCodes.INTERNAL_SERVER_ERROR))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.INTERNAL_SERVER_ERROR, "Failed to read CSV file", null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR, response.getStatus());
    }
    @Test
    void testRegisterUsersFromExcel_IOException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("error"));
        when(responseHandler.error("Failed to read Excel file", HttpStatusCodes.INTERNAL_SERVER_ERROR))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.INTERNAL_SERVER_ERROR, "Failed to read Excel file", null));

        ApiResponse<String> response = userService.registerUsersFromExcel(file);
        assertEquals(HttpStatusCodes.INTERNAL_SERVER_ERROR, response.getStatus());
    }
    @Test
    void testGetDashboardData_InvalidHeader() {
        when(responseHandler.error("Invalid or missing token", HttpStatusCodes.UNAUTHORIZED))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.UNAUTHORIZED, "Invalid or missing token", null));

        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData(null);
        assertEquals(HttpStatusCodes.UNAUTHORIZED, response.getStatus());
    }
    @Test
    void testGetDashboardData_InvalidToken() {
        when(jwtUtil.validateToken("invalid")).thenReturn(false);
        when(responseHandler.error("Invalid or missing token", HttpStatusCodes.UNAUTHORIZED))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.UNAUTHORIZED, "Invalid or missing token", null));

        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData("Bearer invalid");
        assertEquals(HttpStatusCodes.UNAUTHORIZED, response.getStatus());
    }
    @Test
    void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(responseHandler.error("User not found", HttpStatusCodes.NOT_FOUND))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.NOT_FOUND, "User not found", null));

        ApiResponse<ViewUserResponseDTO> response = userService.getUserById(1L);
        assertEquals(HttpStatusCodes.NOT_FOUND, response.getStatus());
    }
    @Test
    void testDeleteUser_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(responseHandler.error("User not found with ID: 1", HttpStatusCodes.NOT_FOUND))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.NOT_FOUND, "User not found with ID: 1", null));

        ApiResponse<DeleteUserResponseDTO> response = userService.deleteUser(1L);
        assertEquals(HttpStatusCodes.NOT_FOUND, response.getStatus());
    }
    @Test
    void testEditUserById_NotFound() {
        EditUserRequestDTO dto = new EditUserRequestDTO("johnny", "johnny@example.com", "newpass");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(responseHandler.error("User not found", HttpStatusCodes.NOT_FOUND))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.NOT_FOUND, "User not found", null));

        ApiResponse<EditUserResponseDTO> response = userService.editUserById(1L, dto);
        assertEquals(HttpStatusCodes.NOT_FOUND, response.getStatus());
    }
    @Test
    void testPatchUserById_NotFound() {
        PatchUserRequestDTO dto = new PatchUserRequestDTO("name", "email", "pass");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(responseHandler.error("User not found", HttpStatusCodes.NOT_FOUND))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.NOT_FOUND, "User not found", null));

        ApiResponse<EditUserResponseDTO> response = userService.patchUserById(1L, dto);
        assertEquals(HttpStatusCodes.NOT_FOUND, response.getStatus());
    }
    @Test
    void testRegisterUsersFromCSV_SkippedDueToInvalidFieldCount() throws IOException {
        String csv = "username,email,password\njohn,john@example.com"; // missing password
        MockMultipartFile file = new MockMultipartFile("file", "users.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8));

        when(responseHandler.success(eq(null), eq("Successfully registered: 0, Skipped: 1"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Successfully registered: 0, Skipped: 1", null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Successfully registered: 0, Skipped: 1", response.getMessage());
    }
    @Test
    void testRegisterUsersFromExcel_SkippedDueToBlankFields() throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        var sheet = workbook.createSheet();
        sheet.createRow(0); // header
        var row = sheet.createRow(1);
        row.createCell(0).setCellValue(""); // blank username
        row.createCell(1).setCellValue("email@example.com");
        row.createCell(2).setCellValue("password");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        workbook.close();

        MockMultipartFile file = new MockMultipartFile("file", "users.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bos.toByteArray());

        when(responseHandler.success(eq(null), eq("Successfully registered: 0, Skipped: 1"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Successfully registered: 0, Skipped: 1", null));

        ApiResponse<String> response = userService.registerUsersFromExcel(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Successfully registered: 0, Skipped: 1", response.getMessage());
    }
    @Test
    void testGetDashboardData_HeaderWithoutBearerPrefix() {
        when(responseHandler.error("Invalid or missing token", HttpStatusCodes.UNAUTHORIZED))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.UNAUTHORIZED, "Invalid or missing token", null));

        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData("Token invalid");
        assertEquals(HttpStatusCodes.UNAUTHORIZED, response.getStatus());
    }
    @Test
    void testGetDashboardData_HeaderMissingBearerPrefix() {
        when(responseHandler.error("Invalid or missing token", HttpStatusCodes.UNAUTHORIZED))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.UNAUTHORIZED, "Invalid or missing token", null));

        ApiResponse<List<UserResponseDTO>> response = userService.getDashboardData("Token invalid");
        assertEquals(HttpStatusCodes.UNAUTHORIZED, response.getStatus());
    }
    @Test
    void testRegisterUsersFromCSV_SkippedDueToBlankUsername() throws Exception {
        String csv = "username,email,password\n,blank@example.com,123456";

        MockMultipartFile file = new MockMultipartFile("file", "blank.csv", "text/csv", csv.getBytes());

        when(responseHandler.success(eq(null), eq("Successfully registered: 0, Skipped: 1"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Successfully registered: 0, Skipped: 1", null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
        assertEquals("Successfully registered: 0, Skipped: 1", response.getMessage());
    }
    @Test
    void testRegisterThrowsInvalidSignupDataException_NullFields() {
        SignupRequestDTO dto = new SignupRequestDTO(null, null, null);
        assertThrows(InvalidSignupDataException.class, () -> userService.register(dto));
    }
    @Test
    void testRegisterUsersFromCSV_BlankFields() throws IOException {
        String csv = "username,email,password\n,,";
        MockMultipartFile file = new MockMultipartFile("file", "blank.csv", "text/csv", csv.getBytes());

        when(responseHandler.success(eq(null), eq("Successfully registered: 0, Skipped: 1"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Successfully registered: 0, Skipped: 1", null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }
    @Test
    void testRegisterUsersFromCSV_UserAlreadyExists() throws IOException {
        String csv = "username,email,password\njohn,john@example.com,123456";
        MockMultipartFile file = new MockMultipartFile("file", "duplicate.csv", "text/csv", csv.getBytes());

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(new User()));

        when(responseHandler.success(eq(null), eq("Successfully registered: 0, Skipped: 1"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "Successfully registered: 0, Skipped: 1", null));

        ApiResponse<String> response = userService.registerUsersFromCSV(file);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }
    @Test
    void testPatchUserById_PatchEmailAndPassword() {
        PatchUserRequestDTO dto = new PatchUserRequestDTO(null, "new@example.com", "newpass");
        User user = new User(1L, "john", "john@example.com", "oldpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);

        EditUserResponseDTO responseDto = new EditUserResponseDTO(1L, "john", "new@example.com");
        when(responseHandler.success(eq(responseDto), eq("User patched successfully"), eq(HttpStatusCodes.OK)))
                .thenReturn(new ApiResponse<>(HttpStatusCodes.OK, "User patched successfully", responseDto));

        ApiResponse<EditUserResponseDTO> response = userService.patchUserById(1L, dto);
        assertEquals(HttpStatusCodes.OK, response.getStatus());
    }


}