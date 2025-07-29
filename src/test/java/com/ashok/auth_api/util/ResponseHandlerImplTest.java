package com.ashok.auth_api.util;

import com.ashok.auth_api.utils.ApiResponse;
import com.ashok.auth_api.utils.ResponseHandlerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResponseHandlerImplTest {

    private ResponseHandlerImpl responseHandler;

    @BeforeEach
    void setup() {
        responseHandler = new ResponseHandlerImpl();
    }

    @Test
    void testSuccessResponse() {
        String data = "SuccessPayload";
        ApiResponse<String> response = responseHandler.success(data, "Operation succeeded", 200);

        assertEquals(200, response.getStatus());
        assertEquals("Operation succeeded", response.getMessage());
        assertEquals("SuccessPayload", response.getData());
    }

    @Test
    void testErrorResponse() {
        ApiResponse<Object> response = responseHandler.error("Something went wrong", 500);

        assertEquals(500, response.getStatus());
        assertEquals("Something went wrong", response.getMessage());
        assertNull(response.getData());
    }
}
