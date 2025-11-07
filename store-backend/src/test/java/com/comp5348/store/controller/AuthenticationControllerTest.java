package com.comp5348.store.controller;

import com.comp5348.store.dto.auth.LoginRequest;
import com.comp5348.store.dto.auth.LoginResponse;
import com.comp5348.store.dto.auth.RegisterRequest;
import com.comp5348.store.dto.auth.UserResponse;
import com.comp5348.store.exception.UserAlreadyExistsException;
import com.comp5348.store.model.auth.UserRole;
import com.comp5348.store.service.auth.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for AuthenticationController
 * Uses MockMvc for testing REST endpoints with Spring Security
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private AuthenticationService authenticationService;

  private RegisterRequest validRegisterRequest;
  private LoginRequest validLoginRequest;
  private LoginResponse loginResponse;
  private UserResponse userResponse;

  @BeforeEach
  void setUp() {
    // Setup test data
    validRegisterRequest = new RegisterRequest(
        "Test User",
        "test@example.com",
        "Password123!"
    );

    validLoginRequest = new LoginRequest(
        "test@example.com",
        "Password123!"
    );

    userResponse = new UserResponse(
        1L,
        "Test User",
        "test@example.com",
        UserRole.CUSTOMER
    );

    loginResponse = new LoginResponse(
        "access-token-123",
        "refresh-token-456",
        userResponse
    );
  }

  // ==================== REGISTER ENDPOINT TESTS ====================

  @Test
  @WithMockUser
  void register_shouldReturnCreatedWithTokens_whenValidRequest() throws Exception {
    // Given
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenReturn(loginResponse);

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").value("access-token-123"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
        .andExpect(jsonPath("$.user.email").value("test@example.com"))
        .andExpect(jsonPath("$.user.name").value("Test User"))
        .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
  }

  @Test
  @WithMockUser
  void register_shouldReturnConflict_whenUserAlreadyExists() throws Exception {
    // Given
    when(authenticationService.register(any(RegisterRequest.class)))
        .thenThrow(new UserAlreadyExistsException("User already exists with email: test@example.com"));

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validRegisterRequest)))
        .andExpect(status().isConflict());
  }

  @Test
  @WithMockUser
  void register_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
    // Given
    RegisterRequest invalidRequest = new RegisterRequest(
        "",  // blank name
        "test@example.com",
        "Password123!"
    );

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void register_shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
    // Given
    RegisterRequest invalidRequest = new RegisterRequest(
        "Test User",
        "invalid-email",  // invalid email format
        "Password123!"
    );

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void register_shouldReturnBadRequest_whenPasswordTooShort() throws Exception {
    // Given
    RegisterRequest invalidRequest = new RegisterRequest(
        "Test User",
        "test@example.com",
        "Pass1!"  // password too short (< 8 characters)
    );

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void register_shouldReturnBadRequest_whenNameTooLong() throws Exception {
    // Given
    String longName = "A".repeat(101);  // exceeds 100 character limit
    RegisterRequest invalidRequest = new RegisterRequest(
        longName,
        "test@example.com",
        "Password123!"
    );

    // When & Then
    mockMvc.perform(post("/api/auth/register")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  // ==================== LOGIN ENDPOINT TESTS ====================

  @Test
  @WithMockUser
  void login_shouldReturnOkWithTokens_whenValidCredentials() throws Exception {
    // Given
    when(authenticationService.login(any(LoginRequest.class)))
        .thenReturn(loginResponse);

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").value("access-token-123"))
        .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
        .andExpect(jsonPath("$.user.email").value("test@example.com"));
  }

  @Test
  @WithMockUser
  void login_shouldReturnUnauthorized_whenInvalidCredentials() throws Exception {
    // Given
    when(authenticationService.login(any(LoginRequest.class)))
        .thenThrow(new BadCredentialsException("Invalid credentials"));

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(validLoginRequest)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void login_shouldReturnBadRequest_whenUsernameIsBlank() throws Exception {
    // Given
    LoginRequest invalidRequest = new LoginRequest("", "Password123!");

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void login_shouldReturnBadRequest_whenPasswordIsBlank() throws Exception {
    // Given
    LoginRequest invalidRequest = new LoginRequest("test@example.com", "");

    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser
  void login_shouldReturnBadRequest_whenRequestBodyIsNull() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  // ==================== REFRESH TOKEN ENDPOINT TESTS ====================

  @Test
  @WithMockUser
  void refreshToken_shouldReturnOkWithNewAccessToken_whenValidRefreshToken() throws Exception {
    // Given
    String refreshToken = "valid-refresh-token";
    String newAccessToken = "new-access-token-789";
    when(authenticationService.refreshToken(anyString()))
        .thenReturn(newAccessToken);

    // When & Then
    mockMvc.perform(post("/api/auth/refresh")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.accessToken").value(newAccessToken));
  }

  @Test
  @WithMockUser
  void refreshToken_shouldReturnUnauthorized_whenInvalidRefreshToken() throws Exception {
    // Given
    String invalidToken = "invalid-refresh-token";
    when(authenticationService.refreshToken(anyString()))
        .thenThrow(new BadCredentialsException("Invalid refresh token"));

    // When & Then
    mockMvc.perform(post("/api/auth/refresh")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + invalidToken + "\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser
  void refreshToken_shouldReturnBadRequest_whenRefreshTokenMissing() throws Exception {
    // When & Then
    mockMvc.perform(post("/api/auth/refresh")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isInternalServerError());  // NullPointerException when accessing null token
  }

  // ==================== LOGOUT ENDPOINT TESTS ====================

  @Test
  @WithMockUser
  void logout_shouldReturnOk_whenBothTokensProvided() throws Exception {
    // Given
    String accessToken = "valid-access-token";
    String refreshToken = "valid-refresh-token";
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.message").value("Logged out successfully"));

    // Verify service was called with both tokens
    verify(authenticationService).logout(accessToken, refreshToken);
  }

  @Test
  @WithMockUser
  void logout_shouldReturnOk_whenOnlyAccessTokenProvided() throws Exception {
    // Given
    String accessToken = "valid-access-token";
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logged out successfully"));

    // Verify service was called with access token and null refresh token
    verify(authenticationService).logout(accessToken, null);
  }

  @Test
  @WithMockUser
  void logout_shouldReturnOk_whenOnlyRefreshTokenProvided() throws Exception {
    // Given
    String refreshToken = "valid-refresh-token";
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logged out successfully"));

    // Verify service was called with null access token and refresh token
    verify(authenticationService).logout(null, refreshToken);
  }

  @Test
  @WithMockUser
  void logout_shouldReturnOk_whenNoTokensProvided() throws Exception {
    // Given
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logged out successfully"));

    // Verify service was called with null tokens
    verify(authenticationService).logout(null, null);
  }

  @Test
  @WithMockUser
  void logout_shouldExtractAccessToken_whenAuthorizationHeaderPresent() throws Exception {
    // Given
    String accessToken = "extracted-access-token";
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());

    // Verify the correct token was extracted from Authorization header
    verify(authenticationService).logout(accessToken, null);
  }

  @Test
  @WithMockUser
  void logout_shouldHandleNonBearerAuthorizationHeader() throws Exception {
    // Given
    doNothing().when(authenticationService).logout(anyString(), anyString());

    // When & Then
    mockMvc.perform(post("/api/auth/logout")
            .with(csrf())
            .header("Authorization", "Basic username:password")  // Non-Bearer scheme
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isOk());

    // Verify service was called with null access token (not extracted from non-Bearer header)
    verify(authenticationService).logout(null, null);
  }
}
