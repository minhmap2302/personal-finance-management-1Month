package org.example.backend.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // 256-bit Base64 secret key
        String base64Secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
        ReflectionTestUtils.setField(jwtService, "secretKey", base64Secret);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 3600000L); // 1 hour

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .username("testuser")
                .passwordHash("$2a$10$xyz")
                .build();
    }

    @Test
    @DisplayName("Tạo JWT token và giải mã username chính xác")
    void shouldGenerateAndExtractUsername() {
        String token = jwtService.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isBlank());

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("test@example.com", extractedUsername);
    }

    @Test
    @DisplayName("Kiểm tra tính hợp lệ của token với UserDetails")
    void shouldValidateTokenCorrectly() {
        String token = jwtService.generateToken(testUser);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid);
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    @DisplayName("Token không hợp lệ khi kiểm tra với UserDetails của user khác")
    void shouldBeInvalidForDifferentUser() {
        String token = jwtService.generateToken(testUser);
        User anotherUser = User.builder()
                .id(2L)
                .email("other@example.com")
                .username("otheruser")
                .build();
        CustomUserDetails otherUserDetails = new CustomUserDetails(anotherUser);

        boolean isValid = jwtService.isTokenValid(token, otherUserDetails);
        assertFalse(isValid);
    }
}
