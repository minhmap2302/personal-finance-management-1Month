package org.example.backend.security.config;

<<<<<<< HEAD
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.example.backend.repository.UserRepository;
import org.example.backend.security.jwt.JwtAccessDeniedHandler;
import org.example.backend.security.jwt.JwtAuthenticationEntryPoint;
import org.example.backend.security.jwt.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
=======
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.backend.security.jwt.JwtAuthenticationFilter;
import org.example.backend.security.principal.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
<<<<<<< HEAD
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
=======
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.time.LocalDateTime;
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
<<<<<<< HEAD
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final UserRepository userRepository;
=======
    private final CustomUserDetailsService customUserDetailsService;
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
<<<<<<< HEAD
                // Cho phép xử lý CORS trước khi xác thực request.
                .cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép đăng nhập, đăng ký và các endpoint xác thực.
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/error"
                        ).permitAll()
                        // Các endpoint còn lại vẫn yêu cầu xác thực.
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );
=======
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new AuthenticationEntryPoint() {
                            @Override
                            public void commence(HttpServletRequest request, HttpServletResponse response,
                                                 AuthenticationException authException) throws IOException {
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.setCharacterEncoding("UTF-8");
                                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                                response.getWriter().write(String.format(
                                        "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                                        LocalDateTime.now(), HttpStatus.UNAUTHORIZED.value(),
                                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                                        "Yêu cầu xác thực để truy cập tài nguyên này",
                                        request.getRequestURI()
                                ));
                            }
                        })
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response,
                                               org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.setCharacterEncoding("UTF-8");
                                response.setStatus(HttpStatus.FORBIDDEN.value());
                                response.getWriter().write(String.format(
                                        "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\"}",
                                        LocalDateTime.now(), HttpStatus.FORBIDDEN.value(),
                                        HttpStatus.FORBIDDEN.getReasonPhrase(),
                                        "Bạn không có quyền truy cập tài nguyên này",
                                        request.getRequestURI()
                                ));
                            }
                        })
                )
                .authorizeHttpRequests(auth -> auth
                        // --- Endpoint công khai (không cần đăng nhập) ---
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Swagger / OpenAPI
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Spring Boot error page
                        .requestMatchers("/error").permitAll()
                        // --- Khu vực quản trị chỉ dành cho ADMIN ---
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // --- Tất cả request còn lại phải xác thực ---
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693

        return http.build();
    }

    @Bean
<<<<<<< HEAD
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Origin frontend đang chạy bằng Vite.
        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        // Các phương thức HTTP được phép.
        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        // Các header frontend có thể gửi.
        configuration.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsernameOrEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Không tìm thấy người dùng với tài khoản: " + username
                        )
                );
=======
    public UserDetailsService userDetailsService() {
        return customUserDetailsService;
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
<<<<<<< HEAD
        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(userDetailsService());

        authProvider.setPasswordEncoder(passwordEncoder());

=======
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
<<<<<<< HEAD
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }
}
=======
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
>>>>>>> bf2505d3ef85ba8b183aab2397ec156e3337d693
