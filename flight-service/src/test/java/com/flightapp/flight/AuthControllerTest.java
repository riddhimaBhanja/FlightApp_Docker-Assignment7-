package com.flightapp.flight;

import com.flightapp.flight.config.JwtUtil;
import com.flightapp.flight.controller.AuthController;
import com.flightapp.flight.dto.AuthRequest;
import com.flightapp.flight.dto.AuthResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testLogin_Success() {
        String mockToken = "eyJhbGciOiJIUzUxMiJ9.test.token";
        when(jwtUtil.generateToken(anyString())).thenReturn(mockToken);

        AuthRequest request = new AuthRequest("admin", "password");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getToken().equals(mockToken);
                    assert response.getUsername().equals("admin");
                    assert response.getMessage().equals("Login successful");
                });
    }

    @Test
    void testLogin_InvalidCredentials_WrongUsername() {
        AuthRequest request = new AuthRequest("wronguser", "password");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getMessage().equals("Invalid credentials");
                });
    }

    @Test
    void testLogin_InvalidCredentials_WrongPassword() {
        AuthRequest request = new AuthRequest("admin", "wrongpassword");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(AuthResponse.class)
                .value(response -> {
                    assert response.getMessage().equals("Invalid credentials");
                });
    }

    @Test
    void testLogin_InvalidCredentials_BothWrong() {
        AuthRequest request = new AuthRequest("wronguser", "wrongpassword");

        webTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testValidateToken_Success() {
        webTestClient.get()
                .uri("/api/auth/validate")
                .header("Authorization", "Bearer test.token.here")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Token is valid");
    }
}
