package com.flightapp.auth.service.impl;

import com.flightapp.auth.dto.AuthRequest;
import com.flightapp.auth.dto.AuthResponse;
import com.flightapp.auth.dto.RegisterRequest;
import com.flightapp.auth.dto.ValidateTokenResponse;
import com.flightapp.auth.entity.User;
import com.flightapp.auth.repository.UserRepository;
import com.flightapp.auth.service.AuthService;
import com.flightapp.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<AuthResponse> login(AuthRequest request) {
        log.info("Login attempt for username: {}", request.getUsername());

        return userRepository.findByUsername(request.getUsername())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .filter(User::getEnabled)
                .flatMap(user -> {
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("email", user.getEmail());
                    claims.put("role", user.getRole());

                    String token = jwtUtil.generateToken(user.getUsername(), claims);

                    AuthResponse response = AuthResponse.builder()
                            .token(token)
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .role(user.getRole())
                            .message("Login successful")
                            .build();

                    log.info("Login successful for user: {}", user.getUsername());
                    return Mono.just(response);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Login failed for username: {}", request.getUsername());
                    return Mono.just(AuthResponse.builder()
                            .message("Invalid username or password")
                            .build());
                }));
    }

    @Override
    public Mono<AuthResponse> register(RegisterRequest request) {
        log.info("Registration attempt for username: {}", request.getUsername());

        return userRepository.existsByUsername(request.getUsername())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        log.warn("Registration failed - username already exists: {}", request.getUsername());
                        return Mono.just(AuthResponse.builder()
                                .message("Username already exists")
                                .build());
                    }

                    return userRepository.existsByEmail(request.getEmail())
                            .flatMap(emailExists -> {
                                if (Boolean.TRUE.equals(emailExists)) {
                                    log.warn("Registration failed - email already exists: {}", request.getEmail());
                                    return Mono.just(AuthResponse.builder()
                                            .message("Email already exists")
                                            .build());
                                }

                                User user = User.builder()
                                        .username(request.getUsername())
                                        .password(passwordEncoder.encode(request.getPassword()))
                                        .email(request.getEmail())
                                        .firstName(request.getFirstName())
                                        .lastName(request.getLastName())
                                        .role("USER")
                                        .enabled(true)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                return userRepository.save(user)
                                        .flatMap(savedUser -> {
                                            Map<String, Object> claims = new HashMap<>();
                                            claims.put("email", savedUser.getEmail());
                                            claims.put("role", savedUser.getRole());

                                            String token = jwtUtil.generateToken(savedUser.getUsername(), claims);

                                            AuthResponse response = AuthResponse.builder()
                                                    .token(token)
                                                    .username(savedUser.getUsername())
                                                    .email(savedUser.getEmail())
                                                    .role(savedUser.getRole())
                                                    .message("Registration successful")
                                                    .build();

                                            log.info("Registration successful for user: {}", savedUser.getUsername());
                                            return Mono.just(response);
                                        });
                            });
                });
    }

    @Override
    public Mono<ValidateTokenResponse> validateToken(String token) {
        try {
            if (Boolean.TRUE.equals(jwtUtil.validateToken(token))) {
                String username = jwtUtil.extractUsername(token);

                return userRepository.findByUsername(username)
                        .filter(User::getEnabled)
                        .map(user -> ValidateTokenResponse.builder()
                                .valid(true)
                                .username(user.getUsername())
                                .message("Token is valid")
                                .build())
                        .switchIfEmpty(Mono.just(ValidateTokenResponse.builder()
                                .valid(false)
                                .message("User not found or disabled")
                                .build()));
            }

            return Mono.just(ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Invalid or expired token")
                    .build());

        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return Mono.just(ValidateTokenResponse.builder()
                    .valid(false)
                    .message("Token validation failed")
                    .build());
        }
    }
}
