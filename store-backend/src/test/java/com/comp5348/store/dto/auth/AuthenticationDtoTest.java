package com.comp5348.store.dto.auth;

import com.comp5348.store.model.auth.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // LoginRequest Tests

  @Test
  void loginRequest_shouldBeValid_withValidData() {
    // Given
    LoginRequest request = new LoginRequest("user@example.com", "Password123");

    // When
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  void loginRequest_shouldFail_whenUsernameIsBlank() {
    // Given
    LoginRequest request = new LoginRequest("", "Password123");

    // When
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Username is required");
  }

  @Test
  void loginRequest_shouldFail_whenPasswordIsBlank() {
    // Given
    LoginRequest request = new LoginRequest("user@example.com", "");

    // When
    Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Password is required");
  }

  // RegisterRequest Tests

  @Test
  void registerRequest_shouldBeValid_withValidData() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "john@example.com",
        "Password123");

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  @Test
  void registerRequest_shouldFail_whenEmailInvalid() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "invalid-email",
        "Password123");

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Email must be valid");
  }

  @Test
  void registerRequest_shouldFail_whenPasswordTooShort() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "john@example.com",
        "Pass1" // Only 5 characters
    );

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then - Multiple violations: size and password complexity
    assertThat(violations).isNotEmpty();
    assertThat(violations).anyMatch(v -> v.getMessage().contains("Password must be at least 8 characters"));
  }

  @Test
  void registerRequest_shouldFail_whenNameTooShort() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "J", // Only 1 character
        "john@example.com",
        "Password123");

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Name must be between 2 and 100 characters");
  }

  @Test
  void registerRequest_shouldFail_whenPasswordLacksUppercase() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "john@example.com",
        "password123" // No uppercase
    );

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Password must contain at least 8 characters, including uppercase and number");
  }

  @Test
  void registerRequest_shouldFail_whenPasswordLacksDigit() {
    // Given
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "john@example.com",
        "Password" // No digit
    );

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Password must contain at least 8 characters, including uppercase and number");
  }

  @Test
  void registerRequest_shouldBeValid_withoutLowercase() {
    // Given - Password with only uppercase and digits (no lowercase) is now valid
    RegisterRequest request = new RegisterRequest(
        "John Doe",
        "john@example.com",
        "PASSWORD123" // No lowercase - but now valid
    );

    // When
    Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

    // Then
    assertThat(violations).isEmpty();
  }

  // LoginResponse Tests

  @Test
  void loginResponse_shouldContainAllFields() {
    // Given
    UserResponse userResponse = new UserResponse(
        1L,
        "John Doe",
        "john@example.com",
        UserRole.CUSTOMER);

    LoginResponse response = new LoginResponse(
        "access-token",
        "refresh-token",
        userResponse);

    // Then
    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
    assertThat(response.getUser()).isNotNull();
    assertThat(response.getUser().getEmail()).isEqualTo("john@example.com");
  }

  // UserResponse Tests

  @Test
  void userResponse_shouldNotExposePassword() {
    // Given
    UserResponse response = new UserResponse(
        1L,
        "John Doe",
        "john@example.com",
        UserRole.CUSTOMER);

    // Then - UserResponse should only have safe fields
    assertThat(response.getId()).isEqualTo(1L);
    assertThat(response.getName()).isEqualTo("John Doe");
    assertThat(response.getEmail()).isEqualTo("john@example.com");
    assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);

    // Password field should not exist
    assertThat(response.getClass().getDeclaredFields())
        .noneMatch(field -> field.getName().equals("password"));
  }
}
