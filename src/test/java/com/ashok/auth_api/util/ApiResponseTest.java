package com.ashok.auth_api.util;

import com.ashok.auth_api.utils.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void testAllArgsConstructorAndGetters() {
        ApiResponse<String> response = new ApiResponse<>(201, "Created", "TestData");

        assertEquals(201, response.getStatus());
        assertEquals("Created", response.getMessage());
        assertEquals("TestData", response.getData());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ApiResponse<String> response = new ApiResponse<>();
        response.setStatus(400);
        response.setMessage("Bad Request");
        response.setData("ErrorDetails");

        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getMessage());
        assertEquals("ErrorDetails", response.getData());
    }
}
