package com.comp5348.store.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.comp5348.store.model.auth.User;
import com.comp5348.store.model.auth.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Set test configuration with base64 encoded secret key
        ReflectionTestUtils.setField(
            jwtUtil,
            "secretKey",
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRz"
        );
        ReflectionTestUtils.setField(
            jwtUtil,
            "accessTokenExpiration",
            3600000L
        ); // 1 hour
        ReflectionTestUtils.setField(
            jwtUtil,
            "refreshTokenExpiration",
            604800000L
        ); // 7 days

        jwtUtil.init(); // Initialize secret key

        userDetails = User.builder()
            .name("testuser")
            .email("test@example.com")
            .password("password")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();
    }

    @Test
    void shouldGenerateAccessToken() {
        // When
        String token = jwtUtil.generateAccessToken(userDetails);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    void shouldGenerateRefreshToken() {
        // When
        String token = jwtUtil.generateRefreshToken(userDetails);

        // Then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        String username = jwtUtil.extractUsername(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        boolean isValid = jwtUtil.validateToken(token, userDetails);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldRejectTokenWithWrongUsername() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        UserDetails wrongUser = User.builder()
            .name("wronguser")
            .email("wrong@example.com")
            .password("password")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();

        // When
        boolean isValid = jwtUtil.validateToken(token, wrongUser);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldRejectExpiredToken() {
        // Given - Create JWT with very short expiration
        JwtUtil shortExpiryJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(
            shortExpiryJwtUtil,
            "secretKey",
            "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRz"
        );
        ReflectionTestUtils.setField(
            shortExpiryJwtUtil,
            "accessTokenExpiration",
            1L
        ); // 1ms
        shortExpiryJwtUtil.init();

        String token = shortExpiryJwtUtil.generateAccessToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(token)).isInstanceOf(
            ExpiredJwtException.class
        );
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When/Then
        assertThatThrownBy(() ->
            jwtUtil.extractUsername(malformedToken)
        ).isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);
        String tamperedToken =
            token.substring(0, token.length() - 10) + "tampered123";

        // When/Then
        assertThatThrownBy(() ->
            jwtUtil.extractUsername(tamperedToken)
        ).isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        String role = jwtUtil.extractClaim(token, claims ->
            claims.get("role", String.class)
        );

        // Then
        assertThat(role).isEqualTo("CUSTOMER");
    }

    @Test
    void accessTokenShouldHaveShorterExpirationThanRefreshToken() {
        // Given
        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // When
        Long accessExpiry = jwtUtil.extractClaim(accessToken, claims ->
            claims.getExpiration().getTime()
        );
        Long refreshExpiry = jwtUtil.extractClaim(refreshToken, claims ->
            claims.getExpiration().getTime()
        );

        // Then
        assertThat(accessExpiry).isLessThan(refreshExpiry);
    }
}
