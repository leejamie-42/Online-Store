package com.comp5348.store.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.comp5348.store.dto.auth.*;
import com.comp5348.store.exception.UserAlreadyExistsException;
import com.comp5348.store.model.auth.*;
import com.comp5348.store.repository.UserRepository;
import com.comp5348.store.util.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private RedisTokenService redisTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User testUser;
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .password("hashedPassword")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();

        loginRequest = new LoginRequest("Test User", "Password123");
        registerRequest = new RegisterRequest(
            "New User",
            "new@example.com",
            "Password123"
        );
    }

    // Login Tests

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(
            authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
            )
        ).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn(
            "refresh-token"
        );

        // When
        LoginResponse response = authenticationService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getName()).isEqualTo("Test User");

        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class)
        );
        verify(authentication).getPrincipal();
        verify(userRepository, never()).findByName(anyString()); // No redundant DB query
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
        verify(redisTokenService).storeRefreshToken(
            "refresh-token",
            "Test User"
        );
    }

    @Test
    void login_shouldThrowException_whenCredentialsInvalid() {
        // Given
        when(
            authenticationManager.authenticate(
                any(UsernamePasswordAuthenticationToken.class)
            )
        ).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(
            any(UsernamePasswordAuthenticationToken.class)
        );
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    // Register Tests

    @Test
    void register_shouldCreateUser_whenNameAndEmailNotExists() {
        // Given
        when(userRepository.existsByName("New User")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn(
            "hashedPassword"
        );

        User savedUser = User.builder()
            .id(2L)
            .name("New User")
            .email("new@example.com")
            .password("hashedPassword")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn(
            "access-token"
        );
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn(
            "refresh-token"
        );

        // When
        LoginResponse response = authenticationService.register(
            registerRequest
        );

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");
        assertThat(response.getUser().getName()).isEqualTo("New User");

        verify(userRepository).existsByName("New User");
        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
        verify(redisTokenService).storeRefreshToken(
            "refresh-token",
            "New User"
        );
    }

    @Test
    void register_shouldThrowException_whenUsernameAlreadyExists() {
        // Given
        when(userRepository.existsByName("New User")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            authenticationService.register(registerRequest)
        )
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining(
                "User already exists with username: New User"
            );

        verify(userRepository).existsByName("New User");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByName("New User")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() ->
            authenticationService.register(registerRequest)
        )
            .isInstanceOf(UserAlreadyExistsException.class)
            .hasMessageContaining(
                "User already exists with email: new@example.com"
            );

        verify(userRepository).existsByName("New User");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldHashPassword_beforeSaving() {
        // Given
        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn(
            "hashedPassword"
        );
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
            invocation.getArgument(0)
        );
        when(jwtUtil.generateAccessToken(any())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(
            argThat(user -> "hashedPassword".equals(user.getPassword()))
        );
    }

    @Test
    void register_shouldSetDefaultRole_toCustomer() {
        // Given
        when(userRepository.existsByName(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation ->
            invocation.getArgument(0)
        );
        when(jwtUtil.generateAccessToken(any())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository).save(
            argThat(user -> UserRole.CUSTOMER.equals(user.getRole()))
        );
    }

    // Refresh Token Tests

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenValid() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(redisTokenService.validateRefreshToken(refreshToken)).thenReturn(
            "Test User"
        );
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("Test User");
        when(userRepository.findByName("Test User")).thenReturn(
            Optional.of(testUser)
        );
        when(jwtUtil.validateToken(refreshToken, testUser)).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn(
            "new-access-token"
        );

        // When
        String newAccessToken = authenticationService.refreshToken(
            refreshToken
        );

        // Then
        assertThat(newAccessToken).isEqualTo("new-access-token");
        verify(redisTokenService).validateRefreshToken(refreshToken);
        verify(jwtUtil).validateToken(refreshToken, testUser);
        verify(jwtUtil).generateAccessToken(testUser);
    }

    @Test
    void refreshToken_shouldThrowException_whenTokenInvalid() {
        // Given - Redis validation returns null (token not found or expired)
        String invalidToken = "invalid-token";
        when(redisTokenService.validateRefreshToken(invalidToken)).thenReturn(
            null
        );

        // When/Then
        assertThatThrownBy(() ->
            authenticationService.refreshToken(invalidToken)
        )
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid or expired refresh token");

        verify(redisTokenService).validateRefreshToken(invalidToken);
        verify(jwtUtil, never()).generateAccessToken(any());
    }
}
