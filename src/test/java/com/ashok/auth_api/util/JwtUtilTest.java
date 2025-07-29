package com.ashok.auth_api.util;

import com.ashok.auth_api.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setup() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void testGenerateAndExtractUsername() {
        String token = jwtUtil.generateToken("ashok");
        assertNotNull(token);
        assertEquals("ashok", jwtUtil.extractUsername(token));
    }

    @Test
    void testValidateToken_ValidToken() {
        String token = jwtUtil.generateToken("ashok");
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void testValidateToken_InvalidToken() {
        String invalidToken = "invalid.token.value";
        assertFalse(jwtUtil.validateToken(invalidToken));
    }
}
