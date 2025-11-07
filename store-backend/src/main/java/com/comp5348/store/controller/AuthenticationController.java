package com.comp5348.store.controller;

import com.comp5348.store.dto.auth.LoginRequest;
import com.comp5348.store.dto.auth.LoginResponse;
import com.comp5348.store.dto.auth.RegisterRequest;
import com.comp5348.store.service.auth.AuthenticationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
    name = "Authentication",
    description = "API endpoints for user authentication"
)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * POST /api/auth/register
     * Register a new user account
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
        @Valid @RequestBody RegisterRequest request
    ) {
        log.info(
            "Registration request received for email: {}",
            request.getEmail()
        );
        LoginResponse response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request received for user: {}", request.getUsername());
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(
        @RequestBody Map<String, String> request
    ) {
        String refreshToken = request.get("refreshToken");
        log.info("Token refresh request received");
        String newAccessToken = authenticationService.refreshToken(
            refreshToken
        );
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * POST /api/auth/logout
     * Invalidate user tokens (logout)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
        @RequestBody Map<String, String> request,
        @RequestHeader(
            value = "Authorization",
            required = false
        ) String authHeader
    ) {
        log.info("Logout request received");

        // Extract access token from Authorization header
        String accessToken = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        // Get refresh token from request body
        String refreshToken = request.get("refreshToken");

        // Perform logout
        authenticationService.logout(accessToken, refreshToken);

        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
