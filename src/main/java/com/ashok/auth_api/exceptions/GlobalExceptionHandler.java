package com.ashok.auth_api.exceptions;

import com.ashok.auth_api.utils.ApiResponse;
import com.ashok.auth_api.utils.HttpStatusCodes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Object>> handleDuplicateUser(UserAlreadyExistsException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse<>(HttpStatusCodes.CONFLICT,ex.getMessage(),null));
    }
    @ExceptionHandler(InvalidSignupDataException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidSignUp(InvalidSignupDataException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(HttpStatusCodes.BAD_REQUEST, ex.getMessage(), null));
    }
}
