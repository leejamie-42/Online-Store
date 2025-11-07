package com.comp5348.store.service.auth;

import com.comp5348.store.dto.auth.*;
import com.comp5348.store.exception.UserAlreadyExistsException;
import com.comp5348.store.model.auth.*;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;
    private final RedisTokenService redisTokenService;

    /**
     * Authenticate user and generate tokens
     * Stores refresh token in Redis with TTL
     */
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        // Extract authenticated user from principal (no additional DB query needed)
        User user = (User) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        // Store refresh token in Redis with automatic TTL
        redisTokenService.storeRefreshToken(refreshToken, user.getUsername());

        log.info("User logged in successfully: {}", user.getId());

        return new LoginResponse(
            accessToken,
            refreshToken,
            mapToUserResponse(user)
        );
    }

    /**
     * Register new user and generate tokens
     * Stores refresh token in Redis with TTL
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info(
            "Registration attempt for username: {}, email: {}",
            request.getName(),
            request.getEmail()
        );

        // Check if username already exists
        if (userRepository.existsByName(request.getName())) {
            log.warn(
                "Registration failed: User already exists with username: {}",
                request.getName()
            );
            throw new UserAlreadyExistsException(
                "User already exists with username: " + request.getName()
            );
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn(
                "Registration failed: User already exists with email: {}",
                request.getEmail()
            );
            throw new UserAlreadyExistsException(
                "User already exists with email: " + request.getEmail()
            );
        }

        // Create new user
        User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.CUSTOMER) // Default role
            .enabled(true)
            .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        // Store refresh token in Redis with automatic TTL
        redisTokenService.storeRefreshToken(
            refreshToken,
            savedUser.getUsername()
        );

        log.info(
            "User registered successfully - ID: {}, Username: {}, Email: {}",
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail()
        );

        return new LoginResponse(
            accessToken,
            refreshToken,
            mapToUserResponse(savedUser)
        );
    }

    /**
     * Refresh access token using refresh token
     * Validates refresh token exists in Redis and generates new access token
     */
    @Transactional(readOnly = true)
    public String refreshToken(String refreshToken) {
        log.debug("Refresh token request");

        // Validate refresh token exists in Redis
        String storedUsername = redisTokenService.validateRefreshToken(
            refreshToken
        );

        if (storedUsername == null) {
            log.warn("Refresh token not found in Redis or expired");
            throw new BadCredentialsException(
                "Invalid or expired refresh token"
            );
        }

        // Extract username from refresh token
        String username = jwtUtil.extractUsername(refreshToken);

        // Verify username matches Redis stored userId
        if (!username.equals(storedUsername)) {
            log.warn(
                "Refresh token username mismatch. Token: {}, Redis: {}",
                username,
                storedUsername
            );
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Load user
        User user = userRepository
            .findByName(username)
            .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Validate refresh token JWT signature and expiration
        if (!jwtUtil.validateToken(refreshToken, user)) {
            log.warn("Invalid refresh token signature for user: {}", username);
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user);

        log.info("Access token refreshed for user: {}", username);

        return newAccessToken;
    }

    /**
     * Logout user by invalidating tokens
     * Access token is blacklisted in Redis, refresh token is deleted from Redis
     */
    public void logout(String accessToken, String refreshToken) {
        log.info("Logout request - invalidating tokens");

        // Blacklist access token
        if (accessToken != null && !accessToken.isBlank()) {
            tokenBlacklistService.blacklistToken(accessToken);
            log.debug("Access token blacklisted");
        }

        // Delete refresh token from Redis (removes ability to refresh)
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTokenService.deleteRefreshToken(refreshToken);
            log.debug("Refresh token deleted from Redis");
        }

        log.info("User logged out successfully - tokens invalidated");
    }

    /**
     * Map User entity to UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole()
        );
    }
}
