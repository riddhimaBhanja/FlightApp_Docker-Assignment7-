package com.flightapp.flight.controller;

import com.flightapp.flight.config.JwtUtil;
import com.flightapp.flight.dto.AuthRequest;
import com.flightapp.flight.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        if ("admin".equals(request.getUsername()) && "password".equals(request.getPassword())) {
            String token = jwtUtil.generateToken(request.getUsername());
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .username(request.getUsername())
                    .message("Login successful")
                    .build();
            return Mono.just(ResponseEntity.ok(response));
        }

        AuthResponse response = AuthResponse.builder()
                .message("Invalid credentials")
                .build();
        return Mono.just(ResponseEntity.status(401).body(response));
    }

    @GetMapping("/validate")
    public Mono<ResponseEntity<String>> validateToken(@RequestHeader("Authorization") String token) {
        return Mono.just(ResponseEntity.ok("Token is valid"));
    }
}
