package com.ashok.auth_api.service.implementation;

import com.ashok.auth_api.dto.*;
import com.ashok.auth_api.exceptions.InvalidSignupDataException;
import com.ashok.auth_api.exceptions.UserAlreadyExistsException;
import com.ashok.auth_api.model.User;
import com.ashok.auth_api.repository.UserRepository;
import com.ashok.auth_api.security.JwtUtil;
import com.ashok.auth_api.service.interfaces.UserService;
import com.ashok.auth_api.utils.ApiResponse;
import com.ashok.auth_api.utils.HttpStatusCodes;
import com.ashok.auth_api.utils.ResponseHandler;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final ResponseHandler responseHandler;

    @Override
    public ApiResponse<SignupResponseDTO> register(SignupRequestDTO dto) {
        if (dto.username() == null || dto.username().isBlank() ||
                dto.email() == null || dto.email().isBlank() ||
                dto.password() == null || dto.password().isBlank()) {

            throw new InvalidSignupDataException("Username, email, and password are required.");
        }
        Optional<User> existingUser = userRepository.findByEmail(dto.email());
        if(existingUser.isPresent()){
            throw new UserAlreadyExistsException("Email already registered");
        }
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));

        User savedUser = userRepository.save(user);

        SignupResponseDTO responseDTO = new SignupResponseDTO(savedUser.getId(),savedUser.getUsername(),savedUser.getEmail());

        return responseHandler.success(responseDTO,"user registered successfully", HttpStatusCodes.CREATED);
    }

    @Override
    public ApiResponse<LoginResponseDTO> login(LoginRequestDTO dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.email());

        if (userOpt.isEmpty() || !passwordEncoder.matches(dto.password(), userOpt.get().getPassword())) {
            return responseHandler.error("Invalid email or password", HttpStatusCodes.UNAUTHORIZED);
        }
        String token = jwtUtil.generateToken(userOpt.get().getEmail());

        return responseHandler.success(
                new LoginResponseDTO(token),
                "Login successful",
                HttpStatusCodes.OK
        );
    }
    @Override
    public ApiResponse<String> registerUsersFromExcel(MultipartFile file) {
        int successCount = 0;
        int skippedCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean isHeader = true;

            for (Row row : sheet) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String username = row.getCell(0).getStringCellValue();
                String email = row.getCell(1).getStringCellValue();
                String password = row.getCell(2).getStringCellValue();

                if (username == null || username.isBlank() ||
                        email == null || email.isBlank() ||
                        password == null || password.isBlank()) {
                    skippedCount++;
                    continue;
                }

                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    skippedCount++;
                    continue;
                }

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                successCount++;
            }

        } catch (IOException e) {
            return responseHandler.error("Failed to read Excel file", HttpStatusCodes.INTERNAL_SERVER_ERROR);
        }

        String message = String.format("Successfully registered: %d, Skipped: %d", successCount, skippedCount);
        //return new ApiResponse<>(200, message, null);
       return responseHandler.success(null,message,HttpStatusCodes.OK);
    }
    @Override
    public ApiResponse<String> registerUsersFromCSV(MultipartFile file) {
        int successCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                String[] fields = line.split(",");
                if (fields.length < 3) {
                    skippedCount++;
                    continue;
                }

                String username = fields[0].trim();
                String email = fields[1].trim();
                String password = fields[2].trim();

                if (username.isBlank() || email.isBlank() || password.isBlank()) {
                    skippedCount++;
                    continue;
                }

                Optional<User> existingUser = userRepository.findByEmail(email);
                if (existingUser.isPresent()) {
                    skippedCount++;
                    continue;
                }

                User user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                successCount++;
            }

        } catch (IOException e) {
            return responseHandler.error("Failed to read CSV file", HttpStatusCodes.INTERNAL_SERVER_ERROR);
        }

        String message = String.format("Successfully registered: %d, Skipped: %d", successCount, skippedCount);
        return responseHandler.success(null, message, HttpStatusCodes.OK);
    }
    @Override
    public ApiResponse<List<UserResponseDTO>> getDashboardData(String authHeader){
        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            if(jwtUtil.validateToken(token)){
                List<User> userList = userRepository.findAll();
                List<UserResponseDTO> dtoList = new ArrayList<>();
                for(User user : userList){
                    UserResponseDTO dto = new UserResponseDTO(
                        user.getId(),user.getUsername(),user.getEmail(),user.getPassword()
                    );
                    dtoList.add(dto);
                }
                return responseHandler.success(dtoList,"Fetched all users",HttpStatusCodes.OK);
            }
        }
        return responseHandler.error("Invalid or missing token",HttpStatusCodes.UNAUTHORIZED);
    }

    @Override
    public ApiResponse<DeleteUserResponseDTO> deleteUser(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            userRepository.deleteById(id);
            DeleteUserResponseDTO dto = new DeleteUserResponseDTO(user.getId(), user.getUsername(), user.getEmail(), user.getPassword());
            return responseHandler.success(dto, "User deleted successfully", HttpStatusCodes.OK);
        }else{
            return responseHandler.error("User not found with ID: " + id, HttpStatusCodes.NOT_FOUND);
        }
    }
    @Override
    public ApiResponse<EditUserResponseDTO> editUserById(Long id, EditUserRequestDTO dto) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return responseHandler.error("User not found", HttpStatusCodes.NOT_FOUND);
        }

        User user = optionalUser.get();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password())); // encode new password

        User updatedUser = userRepository.save(user);

        EditUserResponseDTO responseDTO = new EditUserResponseDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());

        return responseHandler.success(responseDTO, "User updated successfully", HttpStatusCodes.OK);
    }
    @Override
    public ApiResponse<ViewUserResponseDTO> getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return responseHandler.error("User not found", HttpStatusCodes.NOT_FOUND);
        }

        User user = optionalUser.get();
        ViewUserResponseDTO dto = new ViewUserResponseDTO(user.getId(), user.getUsername(), user.getEmail());
        return responseHandler.success(dto, "User fetched successfully", HttpStatusCodes.OK);
    }


}