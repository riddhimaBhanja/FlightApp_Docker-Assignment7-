package com.flightapp.flight;

import com.flightapp.flight.config.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testSecret = "mySecretKeyForJWTTokenGenerationAndValidationMustBeLongEnough1234567890";
    private Long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void testGenerateToken_Success() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // JWT has 3 parts
    }

    @Test
    void testGenerateToken_VerifyUsername() {
        String username = "admin";
        String token = jwtUtil.generateToken(username);

        // Verify token contains correct username
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
    }

    @Test
    void testGenerateToken_VerifyExpiration() {
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void testGenerateToken_DifferentUsersGetDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void testGenerateToken_VerifyIssuedAt() {
        long beforeGeneration = System.currentTimeMillis() - 1000; // 1 second buffer
        String token = jwtUtil.generateToken("testuser");
        long afterGeneration = System.currentTimeMillis() + 1000; // 1 second buffer

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long issuedAt = claims.getIssuedAt().getTime();
        assertTrue(issuedAt >= beforeGeneration && issuedAt <= afterGeneration);
    }

    @Test
    void testGenerateToken_EmptyUsername() {
        String token = jwtUtil.generateToken("");
        assertNotNull(token);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String subject = claims.getSubject();
        assertTrue(subject == null || subject.isEmpty(), "Subject should be null or empty");
    }

    @Test
    void testGenerateToken_SpecialCharactersInUsername() {
        String username = "user@example.com";
        String token = jwtUtil.generateToken(username);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
    }

    @Test
    void testGenerateToken_LongUsername() {
        String username = "a".repeat(500);
        String token = jwtUtil.generateToken(username);

        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals(username, claims.getSubject());
    }
}
