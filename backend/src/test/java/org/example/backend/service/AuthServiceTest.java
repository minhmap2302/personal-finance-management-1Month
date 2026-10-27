package org.example.backend.service;

import org.example.backend.exception.ConflictException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RevokedTokenRepository revokedTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Đăng ký thành công: mã hóa mật khẩu và lưu user")
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .username("user123")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("user123")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("user123")
                .passwordHash("hashedPassword")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("user123", response.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng ký thất bại khi email đã tồn tại")
    void register_conflictEmail() {
        RegisterRequest request = RegisterRequest.builder()
                .email("user@example.com")
                .username("user123")
                .password("password123")
                .build();

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Đăng nhập thành công: trả về access token và thông tin user")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("user@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .username("user123")
                .passwordHash("hashedPassword")
                .build();

        when(userRepository.findByEmailOrUsername("user@example.com", "user@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("user@example.com", response.getUser().getEmail());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Đăng nhập thất bại: người dùng không tồn tại")
    void login_userNotFound() {
        LoginRequest request = LoginRequest.builder()
                .usernameOrEmail("unknown@example.com")
                .password("password123")
                .build();

        when(userRepository.findByEmailOrUsername("unknown@example.com", "unknown@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Đăng xuất: thu hồi token và lưu vào RevokedTokenRepository")
    void logout_success() {
        String token = "sample.jwt.token";
        String authHeader = "Bearer " + token;

        when(revokedTokenRepository.existsByToken(token)).thenReturn(false);
        when(jwtService.extractExpirationAsLocalDateTime(token)).thenReturn(LocalDateTime.now().plusHours(1));

        authService.logout(authHeader);

        verify(revokedTokenRepository).save(any(RevokedToken.class));
    }
}
