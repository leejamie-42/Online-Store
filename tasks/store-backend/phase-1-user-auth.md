# Phase 1: User Authentication Feature (TDD Approach)

**Version**: 1.0
**Last Updated**: October 18, 2025
**Status**: Planning
**Estimated Time**: 32 hours (4 days @ 8 hours/day)

---

## Overview

Implement JWT-based authentication for the Store Backend microservice using **Test-Driven Development (TDD)** methodology with Spring Security 6, BCrypt password hashing, and role-based access control.

This implementation follows the TDD cycle: **Red (Write Failing Test) → Green (Implement to Pass) → Refactor (Improve Code)** for each component.

---

## Section 1: Setup & Dependencies (2 hours)

### Objective
Establish foundation for test-first development with all required dependencies and test infrastructure.

### TDD Workflow
Setup foundation for test-first development.

### Tasks

#### 1.1 Add Dependencies to `store-backend/build.gradle`

Add the following dependencies:

```gradle
dependencies {
    // Existing dependencies
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    compileOnly 'org.projectlombok:lombok'
    runtimeOnly 'org.postgresql:postgresql'
    annotationProcessor 'org.projectlombok:lombok'

    // NEW: Security dependencies
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'

    // NEW: JWT dependencies
    implementation 'io.jsonwebtoken:jjwt-api:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.3'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.3'

    // NEW: Test dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'com.h2database:h2'  // For integration tests
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

#### 1.2 Create Test Package Structure

Create the following test directory structure:

```
store-backend/src/test/java/com/comp5348/store/
├── auth/
│   ├── controller/
│   ├── service/
│   ├── security/
│   ├── dto/
│   └── util/
└── config/
    └── TestSecurityConfig.java
```

Command:
```bash
cd store-backend/src/test/java/com/comp5348/store
mkdir -p auth/controller auth/service auth/security auth/dto auth/util config
```

#### 1.3 Create Test Configuration

Create `TestSecurityConfig.java` for test utilities:

```java
package com.comp5348.store.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

#### 1.4 Verify Setup

Run the following commands to verify setup:

```bash
# From project root
./gradlew :store-backend:build

# Verify test execution
./gradlew :store-backend:test
```

### Acceptance Criteria
- [ ] All dependencies compile successfully
- [ ] Test directory structure created
- [ ] Can run `./gradlew :store-backend:test` without errors
- [ ] Dependencies are properly resolved (check with `./gradlew :store-backend:dependencies`)

---

## Section 2: Domain Layer - User Entity (TDD) (3 hours)

### Objective
Create the User entity with Spring Security UserDetails integration using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 2.1 RED - Write Failing Tests

#### Test File: `UserEntityTest.java`

Location: `src/test/java/com/comp5348/store/auth/entity/UserEntityTest.java`

```java
package com.comp5348.store.auth.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void shouldCreateUserWithRequiredFields() {
        // Given
        User user = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        // Then
        assertThat(user.getName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedPassword");
        assertThat(user.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void shouldImplementUserDetailsCorrectly() {
        // Given
        User user = User.builder()
                .email("user@example.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        // Then - UserDetails methods
        assertThat(user.getUsername()).isEqualTo("user@example.com");
        assertThat(user.getPassword()).isEqualTo("hashedPassword");
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void shouldMapCustomerRoleToAuthority() {
        // Given
        User user = User.builder()
                .email("customer@example.com")
                .role(UserRole.CUSTOMER)
                .build();

        // Then
        assertThat(user.getAuthorities())
                .hasSize(1)
                .contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    @Test
    void shouldMapAdminRoleToAuthority() {
        // Given
        User user = User.builder()
                .email("admin@example.com")
                .role(UserRole.ADMIN)
                .build();

        // Then
        assertThat(user.getAuthorities())
                .hasSize(1)
                .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
```

#### Test File: `UserRepositoryTest.java`

Location: `src/test/java/com/comp5348/store/auth/repository/UserRepositoryTest.java`

```java
package com.comp5348.store.auth.repository;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindUserByEmail() {
        // Given
        User user = createTestUser("test@example.com", "Test User");
        entityManager.persistAndFlush(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // When
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Given
        User user = createTestUser("exists@example.com", "Existing User");
        entityManager.persistAndFlush(user);

        // When/Then
        assertThat(userRepository.existsByEmail("exists@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("notexists@example.com")).isFalse();
    }

    @Test
    void shouldEnforceUniqueEmailConstraint() {
        // Given
        User user1 = createTestUser("duplicate@example.com", "User 1");
        entityManager.persistAndFlush(user1);

        User user2 = createTestUser("duplicate@example.com", "User 2");

        // When/Then
        assertThat(entityManager.getEntityManager()
                .createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email")
                .setParameter("email", "duplicate@example.com")
                .getSingleResult()).isEqualTo(1L);
    }

    private User createTestUser(String email, String name) {
        return User.builder()
                .email(email)
                .name(name)
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests UserEntityTest
./gradlew :store-backend:test --tests UserRepositoryTest
```

---

### 2.2 GREEN - Implement to Pass Tests

#### Create `UserRole` Enum

Location: `src/main/java/com/comp5348/store/auth/entity/UserRole.java`

```java
package com.comp5348.store.auth.entity;

public enum UserRole {
    CUSTOMER,
    ADMIN
}
```

#### Create `User` Entity

Location: `src/main/java/com/comp5348/store/auth/entity/User.java`

```java
package com.comp5348.store.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (enabled == null) {
            enabled = true;
        }
        if (role == null) {
            role = UserRole.CUSTOMER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // UserDetails implementation

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getUsername() {
        return email;  // Use email as username
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
```

#### Create `UserRepository` Interface

Location: `src/main/java/com/comp5348/store/auth/repository/UserRepository.java`

```java
package com.comp5348.store.auth.repository;

import com.comp5348.store.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests UserEntityTest
./gradlew :store-backend:test --tests UserRepositoryTest
```

---

### 2.3 REFACTOR

#### Optimization Checklist
- [x] Lombok annotations minimize boilerplate (@Data, @Builder)
- [x] JPA indexes on email for query performance
- [x] Unique constraint on email column
- [x] Proper validation constraints (@Email, @NotBlank, @Size)
- [x] Audit fields (created_at, updated_at) with @PrePersist/@PreUpdate
- [x] UserDetails integration for Spring Security
- [x] Null-safe defaults in @PrePersist (enabled, role)

### Acceptance Criteria
- [x] All User entity tests pass
- [x] All repository tests pass with H2 test database
- [x] User implements UserDetails correctly
- [x] Email uniqueness enforced at database level
- [x] Validation constraints work as expected

---

## Section 3: JWT Utility Service (TDD) (4 hours)

### Objective
Create JWT token generation and validation service using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 3.1 RED - Write Failing Tests

#### Test File: `JwtUtilTest.java`

Location: `src/test/java/com/comp5348/store/auth/util/JwtUtilTest.java`

```java
package com.comp5348.store.auth.util;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        // Set test configuration
        ReflectionTestUtils.setField(jwtUtil, "secretKey",
            "test-secret-key-for-jwt-token-generation-minimum-256-bits");
        ReflectionTestUtils.setField(jwtUtil, "accessTokenExpiration", 3600000L); // 1 hour
        ReflectionTestUtils.setField(jwtUtil, "refreshTokenExpiration", 604800000L); // 7 days

        jwtUtil.init(); // Initialize secret key

        userDetails = User.builder()
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
        assertThat(username).isEqualTo("test@example.com");
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
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "secretKey",
            "test-secret-key-for-jwt-token-generation-minimum-256-bits");
        ReflectionTestUtils.setField(shortExpiryJwtUtil, "accessTokenExpiration", 1L); // 1ms
        shortExpiryJwtUtil.init();

        String token = shortExpiryJwtUtil.generateAccessToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void shouldRejectMalformedToken() {
        // Given
        String malformedToken = "not.a.valid.jwt.token";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void shouldRejectTokenWithInvalidSignature() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 10) + "tampered123";

        // When/Then
        assertThatThrownBy(() -> jwtUtil.extractUsername(tamperedToken))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtUtil.generateAccessToken(userDetails);

        // When
        String role = jwtUtil.extractClaim(token, claims ->
            claims.get("role", String.class));

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
            claims.getExpiration().getTime());
        Long refreshExpiry = jwtUtil.extractClaim(refreshToken, claims ->
            claims.getExpiration().getTime());

        // Then
        assertThat(accessExpiry).isLessThan(refreshExpiry);
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests JwtUtilTest
```

---

### 3.2 GREEN - Implement to Pass Tests

#### Create JWT Configuration Properties

Location: `src/main/java/com/comp5348/store/auth/config/JwtProperties.java`

```java
package com.comp5348.store.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {
    private String secretKey;
    private Long accessTokenExpiration;  // in milliseconds
    private Long refreshTokenExpiration;  // in milliseconds
}
```

#### Create `JwtUtil` Service

Location: `src/main/java/com/comp5348/store/auth/util/JwtUtil.java`

```java
package com.comp5348.store.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token (short-lived)
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CUSTOMER")
                .replace("ROLE_", ""));

        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * Generate refresh token (long-lived)
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    /**
     * Create JWT token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract username from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract expiration date from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract specific claim from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extract all claims from token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Check if token is expired
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Validate token against user details
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
```

#### Update `application.yml`

Location: `src/main/resources/application.yml`

Add JWT configuration:

```yaml
jwt:
  secret-key: ${JWT_SECRET:dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLW1pbmltdW0tMjU2LWJpdHMtbG9uZw==}
  access-token-expiration: 3600000  # 1 hour in milliseconds
  refresh-token-expiration: 604800000  # 7 days in milliseconds
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests JwtUtilTest
```

---

### 3.3 REFACTOR

#### Optimization Checklist
- [x] Extract configuration to properties class
- [x] Use Base64-encoded secret key for security
- [x] Implement both access and refresh token generation
- [x] Add comprehensive exception handling
- [x] Add logging for security events
- [x] Use HS256 algorithm for token signing
- [x] Include role claim in access token
- [x] Validate token expiration and signature

### Acceptance Criteria
- [x] All JWT utility tests pass
- [x] Tokens are generated with correct expiration times
- [x] Token validation catches all invalid cases (expired, malformed, tampered)
- [x] Secret key is loaded from configuration
- [x] Role information is included in access token claims

---

## Section 4: Authentication DTOs (TDD) (2 hours)

### Objective
Create Data Transfer Objects (DTOs) for authentication with validation using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 4.1 RED - Write Failing Tests

#### Test File: `AuthenticationDtoTest.java`

Location: `src/test/java/com/comp5348/store/auth/dto/AuthenticationDtoTest.java`

```java
package com.comp5348.store.auth.dto;

import com.comp5348.store.auth.entity.UserRole;
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
                "Password123"
        );

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
                "Password123"
        );

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
                "Pass1"  // Only 5 characters
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Password must be at least 8 characters");
    }

    @Test
    void registerRequest_shouldFail_whenNameTooShort() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "J",  // Only 1 character
                "john@example.com",
                "Password123"
        );

        // When
        Set<ConstraintViolation<RegisterRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage())
                .contains("Name must be between 2 and 100 characters");
    }

    // LoginResponse Tests

    @Test
    void loginResponse_shouldContainAllFields() {
        // Given
        UserResponse userResponse = new UserResponse(
                1L,
                "John Doe",
                "john@example.com",
                UserRole.CUSTOMER
        );

        LoginResponse response = new LoginResponse(
                "access-token",
                "refresh-token",
                userResponse
        );

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
                UserRole.CUSTOMER
        );

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
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests AuthenticationDtoTest
```

---

### 4.2 GREEN - Implement to Pass Tests

#### Create `LoginRequest` DTO

Location: `src/main/java/com/comp5348/store/auth/dto/LoginRequest.java`

```java
package com.comp5348.store.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
```

#### Create `RegisterRequest` DTO

Location: `src/main/java/com/comp5348/store/auth/dto/RegisterRequest.java`

```java
package com.comp5348.store.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
```

#### Create `UserResponse` DTO

Location: `src/main/java/com/comp5348/store/auth/dto/UserResponse.java`

```java
package com.comp5348.store.auth.dto;

import com.comp5348.store.auth.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
}
```

#### Create `LoginResponse` DTO

Location: `src/main/java/com/comp5348/store/auth/dto/LoginResponse.java`

```java
package com.comp5348.store.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private UserResponse user;
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests AuthenticationDtoTest
```

---

### 4.3 REFACTOR

#### Add Password Strength Validation (Enhanced)

Create custom password validator annotation:

Location: `src/main/java/com/comp5348/store/auth/validation/ValidPassword.java`

```java
package com.comp5348.store.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must contain at least 8 characters, including uppercase, lowercase, and number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
```

Location: `src/main/java/com/comp5348/store/auth/validation/PasswordValidator.java`

```java
package com.comp5348.store.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }

        return UPPERCASE_PATTERN.matcher(password).find()
                && LOWERCASE_PATTERN.matcher(password).find()
                && DIGIT_PATTERN.matcher(password).find();
    }
}
```

Update `RegisterRequest` to use custom validation:

```java
@ValidPassword
private String password;
```

### Acceptance Criteria
- [x] All DTO validation tests pass
- [x] Password validation enforces: min 8 chars, uppercase, lowercase, number
- [x] Email validation works correctly
- [x] DTOs serialize/deserialize properly
- [x] UserResponse does not expose password field
- [x] Validation messages are clear and helpful

---

## Section 5: UserDetailsService Implementation (TDD) (3 hours)

### Objective
Implement Spring Security's UserDetailsService for loading user data during authentication using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 5.1 RED - Write Failing Tests

#### Test File: `CustomUserDetailsServiceTest.java`

Location: `src/test/java/com/comp5348/store/auth/service/CustomUserDetailsServiceTest.java`

```java
package com.comp5348.store.auth.service;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

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
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Given
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Given
        when(userRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsDisabled() {
        // Given
        User disabledUser = User.builder()
                .id(2L)
                .name("Disabled User")
                .email("disabled@example.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(false)
                .build();

        when(userRepository.findByEmail("disabled@example.com"))
                .thenReturn(Optional.of(disabledUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("disabled@example.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_shouldHandleEmailCaseInsensitively() {
        // Given
        when(userRepository.findByEmail("TEST@EXAMPLE.COM"))
                .thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("TEST@EXAMPLE.COM");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests CustomUserDetailsServiceTest
```

---

### 5.2 GREEN - Implement to Pass Tests

#### Create `CustomUserDetailsService`

Location: `src/main/java/com/comp5348/store/auth/service/CustomUserDetailsService.java`

```java
package com.comp5348.store.auth.service;

import com.comp5348.store.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException(
                            "User not found with email: " + username
                    );
                });
    }
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests CustomUserDetailsServiceTest
```

---

### 5.3 REFACTOR

#### Add Caching (Optional Enhancement)

For production optimization, you can add caching:

```java
@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "users")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#username", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    log.warn("User not found with email: {}", username);
                    return new UsernameNotFoundException(
                            "User not found with email: " + username
                    );
                });
    }

    @CacheEvict(key = "#username")
    public void evictUserCache(String username) {
        log.debug("Evicting user cache for: {}", username);
    }
}
```

### Acceptance Criteria
- [x] All UserDetailsService tests pass
- [x] Throws UsernameNotFoundException for non-existent users
- [x] Returns UserDetails for valid users
- [x] Properly handles disabled users
- [x] Transaction boundaries defined (@Transactional)
- [x] Logging implemented for authentication attempts

---

## Section 6: Authentication Service (TDD) (4 hours)

### Objective
Implement the authentication service handling login, registration, and token management using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 6.1 RED - Write Failing Tests

#### Test File: `AuthenticationServiceTest.java`

Location: `src/test/java/com/comp5348/store/auth/service/AuthenticationServiceTest.java`

```java
package com.comp5348.store.auth.service;

import com.comp5348.store.auth.dto.LoginRequest;
import com.comp5348.store.auth.dto.LoginResponse;
import com.comp5348.store.auth.dto.RegisterRequest;
import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.exception.UserAlreadyExistsException;
import com.comp5348.store.auth.repository.UserRepository;
import com.comp5348.store.auth.util.JwtUtil;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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

        loginRequest = new LoginRequest("test@example.com", "Password123");
        registerRequest = new RegisterRequest("New User", "new@example.com", "Password123");
    }

    // Login Tests

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(testUser)).thenReturn("refresh-token");

        // When
        LoginResponse response = authenticationService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateAccessToken(testUser);
        verify(jwtUtil).generateRefreshToken(testUser);
    }

    @Test
    void login_shouldThrowException_whenCredentialsInvalid() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When/Then
        assertThatThrownBy(() -> authenticationService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, never()).generateAccessToken(any());
    }

    // Register Tests

    @Test
    void register_shouldCreateUser_whenEmailNotExists() {
        // Given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(2L)
                .name("New User")
                .email("new@example.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(any(User.class))).thenReturn("refresh-token");

        // When
        LoginResponse response = authenticationService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        assertThat(response.getUser().getEmail()).isEqualTo("new@example.com");

        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        // Given
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User already exists with email: new@example.com");

        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void register_shouldHashPassword_beforeSaving() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("Password123")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessToken(any())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(argThat(user ->
            "hashedPassword".equals(user.getPassword())
        ));
    }

    @Test
    void register_shouldSetDefaultRole_toCustomer() {
        // Given
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtil.generateAccessToken(any())).thenReturn("token");
        when(jwtUtil.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository).save(argThat(user ->
            UserRole.CUSTOMER.equals(user.getRole())
        ));
    }

    // Refresh Token Tests

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenValid() {
        // Given
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.extractUsername(refreshToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(refreshToken, testUser)).thenReturn(true);
        when(jwtUtil.generateAccessToken(testUser)).thenReturn("new-access-token");

        // When
        String newAccessToken = authenticationService.refreshToken(refreshToken);

        // Then
        assertThat(newAccessToken).isEqualTo("new-access-token");
        verify(jwtUtil).validateToken(refreshToken, testUser);
        verify(jwtUtil).generateAccessToken(testUser);
    }

    @Test
    void refreshToken_shouldThrowException_whenTokenInvalid() {
        // Given
        String invalidToken = "invalid-token";
        when(jwtUtil.extractUsername(invalidToken)).thenReturn("test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtUtil.validateToken(invalidToken, testUser)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> authenticationService.refreshToken(invalidToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid refresh token");

        verify(jwtUtil, never()).generateAccessToken(any());
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests AuthenticationServiceTest
```

---

### 6.2 GREEN - Implement to Pass Tests

#### Create Custom Exception

Location: `src/main/java/com/comp5348/store/auth/exception/UserAlreadyExistsException.java`

```java
package com.comp5348.store.auth.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
```

#### Create `AuthenticationService`

Location: `src/main/java/com/comp5348/store/auth/service/AuthenticationService.java`

```java
package com.comp5348.store.auth.service;

import com.comp5348.store.auth.dto.LoginRequest;
import com.comp5348.store.auth.dto.LoginResponse;
import com.comp5348.store.auth.dto.RegisterRequest;
import com.comp5348.store.auth.dto.UserResponse;
import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.exception.UserAlreadyExistsException;
import com.comp5348.store.auth.repository.UserRepository;
import com.comp5348.store.auth.util.JwtUtil;
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

    /**
     * Authenticate user and generate tokens
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

        // Load user details
        User user = userRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        log.info("User logged in successfully: {}", user.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken,
                mapToUserResponse(user)
        );
    }

    /**
     * Register new user and generate tokens
     */
    @Transactional
    public LoginResponse register(RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.getEmail());

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: User already exists with email: {}", request.getEmail());
            throw new UserAlreadyExistsException(
                    "User already exists with email: " + request.getEmail()
            );
        }

        // Create new user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)  // Default role
                .enabled(true)
                .build();

        // Save user
        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedUser);
        String refreshToken = jwtUtil.generateRefreshToken(savedUser);

        log.info("User registered successfully: {}", savedUser.getEmail());

        return new LoginResponse(
                accessToken,
                refreshToken,
                mapToUserResponse(savedUser)
        );
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional(readOnly = true)
    public String refreshToken(String refreshToken) {
        log.debug("Refresh token request");

        // Extract username from refresh token
        String username = jwtUtil.extractUsername(refreshToken);

        // Load user
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        // Validate refresh token
        if (!jwtUtil.validateToken(refreshToken, user)) {
            log.warn("Invalid refresh token for user: {}", username);
            throw new BadCredentialsException("Invalid refresh token");
        }

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user);

        log.info("Access token refreshed for user: {}", username);

        return newAccessToken;
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
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests AuthenticationServiceTest
```

---

### 6.3 REFACTOR

#### Enhancements
- [x] Transaction boundaries defined (@Transactional)
- [x] Comprehensive logging for authentication events
- [x] Proper exception handling with meaningful messages
- [x] Password hashing before saving user
- [x] Default role assignment (CUSTOMER)
- [x] Email uniqueness validation
- [x] Token generation separated from authentication logic

### Acceptance Criteria
- [x] All authentication service tests pass
- [x] Login validates credentials correctly using AuthenticationManager
- [x] Register creates users with BCrypt hashed passwords
- [x] Duplicate email registration fails with UserAlreadyExistsException
- [x] Token refresh works correctly with valid refresh tokens
- [x] Invalid credentials throw BadCredentialsException
- [x] Logging captures all authentication events

---

## Section 7: Security Configuration (TDD) (4 hours)

### Objective
Configure Spring Security with stateless JWT authentication, define public and protected endpoints, and set up CORS using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 7.1 RED - Write Failing Tests

#### Test File: `SecurityConfigTest.java`

Location: `src/test/java/com/comp5348/store/auth/config/SecurityConfigTest.java`

```java
package com.comp5348.store.auth.config;

import com.comp5348.store.auth.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    // Public Endpoints Tests

    @Test
    void authEndpoints_shouldBeAccessible_withoutAuthentication() throws Exception {
        // POST /api/auth/login should be public
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"username\":\"test\",\"password\":\"test\"}"))
                .andExpect(status().isUnauthorized()); // Fails auth, but accessible

        // POST /api/auth/register should be public
        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content("{\"name\":\"test\",\"email\":\"test@test.com\",\"password\":\"Test1234\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void productEndpoints_shouldBeAccessible_withoutAuthentication() throws Exception {
        // GET /api/products should be public
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorEndpoints_shouldBeAccessible_withoutAuthentication() throws Exception {
        // GET /actuator/health should be public
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // Protected Endpoints Tests

    @Test
    void userEndpoints_shouldRequireAuthentication() throws Exception {
        // GET /api/users/me should require auth
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "CUSTOMER")
    void userEndpoints_shouldBeAccessible_withAuthentication() throws Exception {
        // GET /api/users/me should work with auth
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk());
    }

    @Test
    void orderEndpoints_shouldRequireAuthentication() throws Exception {
        // POST /api/orders should require auth
        mockMvc.perform(post("/api/orders")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    // CORS Tests

    @Test
    void corsConfiguration_shouldAllowFrontendOrigin() throws Exception {
        mockMvc.perform(get("/api/products")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String allowOrigin = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    assert allowOrigin != null && allowOrigin.contains("http://localhost:3000");
                });
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests SecurityConfigTest
```

---

### 7.2 GREEN - Implement to Pass Tests

#### Create `SecurityConfig`

Location: `src/main/java/com/comp5348/store/auth/config/SecurityConfig.java`

```java
package com.comp5348.store.auth.config;

import com.comp5348.store.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    /**
     * Define public and protected endpoints
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/products/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()

                        // Protected endpoints - require authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configure CORS for frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Authentication provider with UserDetailsService
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication manager bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * BCrypt password encoder with strength 12
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests SecurityConfigTest
```

---

### 7.3 REFACTOR

#### Security Enhancements

Add security headers:

```java
http
    .headers(headers -> headers
        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
        .frameOptions(frame -> frame.deny())
    );
```

### Acceptance Criteria
- [x] Security config tests pass
- [x] Public endpoints accessible without authentication (/api/auth/**, /api/products/**, /actuator/**)
- [x] Protected endpoints return 401 without valid JWT
- [x] CORS configured for frontend origin (http://localhost:3000)
- [x] Password encoder uses BCrypt strength 12
- [x] Stateless session management configured
- [x] JWT filter added before UsernamePasswordAuthenticationFilter

---

## Section 8: JWT Authentication Filter (TDD) (3 hours)

### Objective
Implement JWT authentication filter to intercept requests, validate tokens, and set Spring Security authentication context using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 8.1 RED - Write Failing Tests

#### Test File: `JwtAuthenticationFilterTest.java`

Location: `src/test/java/com/comp5348/store/auth/security/JwtAuthenticationFilterTest.java`

```java
package com.comp5348.store.auth.security;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private User testUser;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
    }

    @Test
    void shouldAuthenticateUser_whenValidTokenProvided() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
        when(jwtUtil.validateToken(token, testUser)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(testUser);
        assertThat(SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
                .isTrue();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenTokenInvalid() throws ServletException, IOException {
        // Given
        String token = "invalid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUser);
        when(jwtUtil.validateToken(token, testUser)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldContinueFilterChain_whenNoAuthorizationHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void shouldContinueFilterChain_whenAuthorizationHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic sometoken");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtUtil, never()).extractUsername(anyString());
    }

    @Test
    void shouldNotAuthenticate_whenUserNotFound() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticate_whenTokenExtractionFails() throws ServletException, IOException {
        // Given
        String token = "malformed.token";
        String authHeader = "Bearer " + token;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtil.extractUsername(token)).thenThrow(new io.jsonwebtoken.MalformedJwtException("Malformed JWT"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void shouldSkipAuthentication_whenSecurityContextAlreadyHasAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid.jwt.token";
        String authHeader = "Bearer " + token;

        // Simulate already authenticated user
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken existingAuth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        testUser, null, testUser.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtUtil, never()).extractUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests JwtAuthenticationFilterTest
```

---

### 8.2 GREEN - Implement to Pass Tests

#### Create `JwtAuthenticationFilter`

Location: `src/main/java/com/comp5348/store/auth/security/JwtAuthenticationFilter.java`

```java
package com.comp5348.store.auth.security;

import com.comp5348.store.auth.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 *
 * Intercepts incoming requests, extracts JWT token from Authorization header,
 * validates the token, and sets the authentication in the SecurityContext.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Get Authorization header
        final String authHeader = request.getHeader("Authorization");

        // Skip if no Authorization header or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token
            final String jwt = authHeader.substring(7);

            // Extract username from token
            final String username = jwtUtil.extractUsername(jwt);

            // Authenticate if username exists and no authentication in context
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Load user details
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Validate token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Set authentication details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {}", username);
                } else {
                    log.warn("JWT validation failed for user: {}", username);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication error: {}", e.getMessage());
            // Continue filter chain even on error (authentication will remain null)
        }

        // Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests JwtAuthenticationFilterTest
```

---

### 8.3 REFACTOR

#### Optimization Checklist
- [x] Extends OncePerRequestFilter to ensure single execution per request
- [x] Null-safe header extraction
- [x] Skip authentication if already authenticated (performance)
- [x] Comprehensive exception handling
- [x] Logging for security events (debug and error levels)
- [x] Non-blocking filter chain continuation
- [x] WebAuthenticationDetailsSource for request details

#### Performance Enhancements

Add early returns for public endpoints:

```java
@Override
protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/api/auth/") ||
           path.startsWith("/api/products/") ||
           path.startsWith("/actuator/");
}
```

### Acceptance Criteria
- [x] All JWT filter tests pass
- [x] Valid tokens set authentication in SecurityContext
- [x] Invalid tokens do not authenticate
- [x] Missing Authorization header is handled gracefully
- [x] Exception during token processing doesn't break filter chain
- [x] Already authenticated users are not re-authenticated
- [x] Filter logs security events appropriately

---

## Section 9: Controllers & Exception Handling (TDD) (4 hours)

### Objective
Create REST controllers for authentication endpoints and global exception handling using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 9.1 RED - Write Failing Tests

#### Test File: `AuthenticationControllerTest.java`

Location: `src/test/java/com/comp5348/store/auth/controller/AuthenticationControllerTest.java`

```java
package com.comp5348.store.auth.controller;

import com.comp5348.store.auth.dto.LoginRequest;
import com.comp5348.store.auth.dto.LoginResponse;
import com.comp5348.store.auth.dto.RegisterRequest;
import com.comp5348.store.auth.dto.UserResponse;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.exception.UserAlreadyExistsException;
import com.comp5348.store.auth.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private LoginResponse loginResponse;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "Password123");
        registerRequest = new RegisterRequest("Test User", "test@example.com", "Password123");

        UserResponse userResponse = new UserResponse(1L, "Test User", "test@example.com", UserRole.CUSTOMER);
        loginResponse = new LoginResponse("access-token", "refresh-token", userResponse);
    }

    // Login Endpoint Tests

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() throws Exception {
        // Given
        when(authenticationService.login(any(LoginRequest.class)))
                .thenReturn(loginResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        // Given
        when(authenticationService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_shouldReturn400_whenRequestInvalid() throws Exception {
        // Given - Missing password
        LoginRequest invalidRequest = new LoginRequest("test@example.com", "");

        // When/Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // Register Endpoint Tests

    @Test
    void register_shouldReturnTokens_whenRegistrationSuccessful() throws Exception {
        // Given
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(loginResponse);

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    void register_shouldReturn409_whenUserAlreadyExists() throws Exception {
        // Given
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists"));
    }

    @Test
    void register_shouldReturn400_whenEmailInvalid() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("Test User", "invalid-email", "Password123");

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_shouldReturn400_whenPasswordTooShort() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest("Test User", "test@example.com", "Pass1");

        // When/Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    // Refresh Token Endpoint Tests

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenValid() throws Exception {
        // Given
        when(authenticationService.refreshToken("valid-refresh-token"))
                .thenReturn("new-access-token");

        // When/Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"valid-refresh-token\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    void refreshToken_shouldReturn401_whenRefreshTokenInvalid() throws Exception {
        // Given
        when(authenticationService.refreshToken("invalid-refresh-token"))
                .thenThrow(new BadCredentialsException("Invalid refresh token"));

        // When/Then
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"invalid-refresh-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests AuthenticationControllerTest
```

---

### 9.2 GREEN - Implement to Pass Tests

#### Create Error Response DTO

Location: `src/main/java/com/comp5348/store/auth/dto/ErrorResponse.java`

```java
package com.comp5348.store.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse(String message, int status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }
}
```

#### Create Refresh Token Request DTO

Location: `src/main/java/com/comp5348/store/auth/dto/RefreshTokenRequest.java`

```java
package com.comp5348.store.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
```

#### Create Access Token Response DTO

Location: `src/main/java/com/comp5348/store/auth/dto/AccessTokenResponse.java`

```java
package com.comp5348.store.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenResponse {

    private String accessToken;
}
```

#### Create `AuthenticationController`

Location: `src/main/java/com/comp5348/store/auth/controller/AuthenticationController.java`

```java
package com.comp5348.store.auth.controller;

import com.comp5348.store.auth.dto.*;
import com.comp5348.store.auth.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST Controller
 *
 * Handles user authentication endpoints: login, register, and token refresh.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * POST /api/auth/login
     * Authenticate user with email and password
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request for user: {}", request.getUsername());
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/register
     * Register new user account
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request for email: {}", request.getEmail());
        LoginResponse response = authenticationService.register(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh-token
     * Refresh access token using refresh token
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AccessTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Refresh token request");
        String newAccessToken = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }
}
```

#### Create `GlobalExceptionHandler`

Location: `src/main/java/com/comp5348/store/auth/exception/GlobalExceptionHandler.java`

```java
package com.comp5348.store.auth.exception;

import com.comp5348.store.auth.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 *
 * Centralized exception handling for all controllers.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        String message = "Validation failed: " + errors.toString();
        ErrorResponse errorResponse = new ErrorResponse(
                message,
                HttpStatus.BAD_REQUEST.value(),
                java.time.LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        log.warn("Validation error: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Handle authentication failures
     */
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            Exception ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                java.time.LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        log.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle user already exists
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyExists(
            UserAlreadyExistsException ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                java.time.LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        log.warn("User already exists error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                java.time.LocalDateTime.now(),
                request.getDescription(false).replace("uri=", "")
        );

        log.error("Unexpected error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests AuthenticationControllerTest
```

---

### 9.3 REFACTOR

#### Add API Documentation (Optional)

Add Swagger/OpenAPI annotations:

```java
@Operation(summary = "User login", description = "Authenticate user with email and password")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Login successful"),
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
})
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    // ...
}
```

### Acceptance Criteria
- [x] All controller tests pass
- [x] Login endpoint returns 200 with tokens for valid credentials
- [x] Login endpoint returns 401 for invalid credentials
- [x] Register endpoint returns 200 with tokens for new users
- [x] Register endpoint returns 409 when user already exists
- [x] Refresh endpoint returns 200 with new access token
- [x] Validation errors return 400 with clear messages
- [x] Global exception handler catches all exceptions
- [x] Error responses include timestamp, status, message, and path

---

## Section 10: Integration, Data Initialization & Documentation (TDD) (3 hours)

### Objective
Create end-to-end integration tests, initialize default data, and complete documentation using TDD approach.

### TDD Workflow
**Red → Green → Refactor**

---

### 10.1 RED - Write Failing Tests

#### Test File: `AuthenticationIntegrationTest.java`

Location: `src/test/java/com/comp5348/store/auth/integration/AuthenticationIntegrationTest.java`

```java
package com.comp5348.store.auth.integration;

import com.comp5348.store.auth.dto.LoginRequest;
import com.comp5348.store.auth.dto.LoginResponse;
import com.comp5348.store.auth.dto.RegisterRequest;
import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void fullAuthenticationFlow_shouldWork() throws Exception {
        // Step 1: Register new user
        RegisterRequest registerRequest = new RegisterRequest(
                "Integration Test User",
                "integration@example.com",
                "Password123"
        );

        MvcResult registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.user.email").value("integration@example.com"))
                .andReturn();

        LoginResponse registerResponse = objectMapper.readValue(
                registerResult.getResponse().getContentAsString(),
                LoginResponse.class
        );

        // Verify user in database
        User savedUser = userRepository.findByEmail("integration@example.com").orElseThrow();
        assertThat(savedUser.getName()).isEqualTo("Integration Test User");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(savedUser.isEnabled()).isTrue();

        // Step 2: Access protected endpoint with access token
        String accessToken = registerResponse.getAccessToken();
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Step 3: Login with same credentials
        LoginRequest loginRequest = new LoginRequest("integration@example.com", "Password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                loginResult.getResponse().getContentAsString(),
                LoginResponse.class
        );

        // Step 4: Refresh access token
        String refreshToken = loginResponse.getRefreshToken();
        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void shouldNotAllowDuplicateEmailRegistration() throws Exception {
        // Register first user
        RegisterRequest firstRequest = new RegisterRequest(
                "First User",
                "duplicate@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Try to register second user with same email
        RegisterRequest secondRequest = new RegisterRequest(
                "Second User",
                "duplicate@example.com",
                "Password123"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("User already exists with email: duplicate@example.com"));
    }

    @Test
    void shouldRejectInvalidCredentials() throws Exception {
        // Create user directly
        User user = User.builder()
                .name("Test User")
                .email("test@example.com")
                .password(passwordEncoder.encode("CorrectPassword123"))
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
        userRepository.save(user);

        // Try login with wrong password
        LoginRequest wrongPasswordRequest = new LoginRequest("test@example.com", "WrongPassword123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPasswordRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectAccessWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer invalid.jwt.token"))
                .andExpect(status().isUnauthorized());
    }
}
```

#### Create Test Configuration

Location: `src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  h2:
    console:
      enabled: true

jwt:
  secret-key: dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tZ2VuZXJhdGlvbi1taW5pbXVtLTI1Ni1iaXRzLWxvbmc=
  access-token-expiration: 3600000
  refresh-token-expiration: 604800000

logging:
  level:
    com.comp5348.store: DEBUG
```

**Run tests** (they should fail):
```bash
./gradlew :store-backend:test --tests AuthenticationIntegrationTest
```

---

### 10.2 GREEN - Implement to Pass Tests

#### Create Data Initialization

Location: `src/main/java/com/comp5348/store/config/DataInitializer.java`

```java
package com.comp5348.store.config;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;
import com.comp5348.store.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data Initialization
 *
 * Creates default users for development and testing.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (userRepository.count() == 0) {
                log.info("Initializing default users...");

                // Create default customer user
                User customer = User.builder()
                        .name("Customer User")
                        .email("customer")
                        .password(passwordEncoder.encode("COMP5348"))
                        .role(UserRole.CUSTOMER)
                        .enabled(true)
                        .build();
                userRepository.save(customer);
                log.info("Created default customer user: customer/COMP5348");

                // Create default admin user
                User admin = User.builder()
                        .name("Admin User")
                        .email("admin")
                        .password(passwordEncoder.encode("COMP5348"))
                        .role(UserRole.ADMIN)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Created default admin user: admin/COMP5348");

                log.info("Data initialization complete.");
            } else {
                log.info("Users already exist. Skipping data initialization.");
            }
        };
    }
}
```

#### Update `application-local.yml`

Location: `src/main/resources/application-local.yml`

Add JWT configuration:

```yaml
jwt:
  secret-key: ${JWT_SECRET:dGhpcy1pcy1hLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLW1pbmltdW0tMjU2LWJpdHMtbG9uZw==}
  access-token-expiration: 3600000  # 1 hour
  refresh-token-expiration: 604800000  # 7 days

logging:
  level:
    com.comp5348.store.auth: DEBUG
    org.springframework.security: DEBUG
```

#### Update README with Authentication Endpoints

Add to README.md:

```markdown
## Authentication

### Default Test Accounts

- **Customer**: username `customer`, password `COMP5348`
- **Admin**: username `admin`, password `COMP5348`

### API Endpoints

#### POST /api/auth/register
Register a new user account.

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "Password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "CUSTOMER"
  }
}
```

#### POST /api/auth/login
Authenticate with email and password.

**Request:**
```json
{
  "username": "customer",
  "password": "COMP5348"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "Customer User",
    "email": "customer",
    "role": "CUSTOMER"
  }
}
```

#### POST /api/auth/refresh-token
Refresh access token using refresh token.

**Request:**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Protected Endpoints

Add JWT token to Authorization header:

```bash
curl -H "Authorization: Bearer <access-token>" http://localhost:8081/api/users/me
```

### Testing Authentication

```bash
# Register new user
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Password123"}'

# Login
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"customer","password":"COMP5348"}'

# Access protected endpoint
curl -H "Authorization: Bearer <access-token>" \
  http://localhost:8081/api/users/me
```
```

**Run tests again** (they should pass now):
```bash
./gradlew :store-backend:test --tests AuthenticationIntegrationTest
```

---

### 10.3 REFACTOR

#### Create Test Data Factory (Optional)

Location: `src/test/java/com/comp5348/store/auth/TestDataFactory.java`

```java
package com.comp5348.store.auth;

import com.comp5348.store.auth.entity.User;
import com.comp5348.store.auth.entity.UserRole;

public class TestDataFactory {

    public static User createTestCustomer() {
        return User.builder()
                .name("Test Customer")
                .email("customer@test.com")
                .password("hashedPassword")
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();
    }

    public static User createTestAdmin() {
        return User.builder()
                .name("Test Admin")
                .email("admin@test.com")
                .password("hashedPassword")
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();
    }
}
```

### Acceptance Criteria
- [x] All integration tests pass
- [x] End-to-end authentication flow works (register → login → access → refresh)
- [x] Default users initialized on application startup (customer/COMP5348, admin/COMP5348)
- [x] Test configuration uses H2 in-memory database
- [x] README updated with authentication endpoints and examples
- [x] Protected endpoints require valid JWT
- [x] Duplicate registration prevented
- [x] Invalid credentials rejected

---

## Progress Tracking

Use this checklist to track progress:

- [ ] Section 1: Setup & Dependencies (2 hours)
- [ ] Section 2: Domain Layer - User Entity (3 hours)
- [ ] Section 3: JWT Utility Service (4 hours)
- [ ] Section 4: Authentication DTOs (2 hours)
- [ ] Section 5: UserDetailsService Implementation (3 hours)
- [ ] Section 6: Authentication Service (4 hours)
- [ ] Section 7: Security Configuration (4 hours)
- [ ] Section 8: JWT Authentication Filter (3 hours)
- [ ] Section 9: Controllers & Exception Handling (4 hours)
- [ ] Section 10: Integration, Data Initialization & Documentation (3 hours)

**Total Estimated Time**: 32 hours (4 days @ 8 hours/day)

---

## Test Coverage Goals

- Overall test coverage: **≥80%**
- Service layer coverage: **≥90%**
- Controller layer coverage: **≥85%**
- All tests passing: `./gradlew :store-backend:test`

---

## Final Validation Checklist

Before marking this phase complete, verify:

- [ ] All 32 hours of tasks completed
- [ ] All unit tests passing (Sections 2-6)
- [ ] All integration tests passing (Section 10)
- [ ] Security configuration tested (Section 7)
- [ ] JWT filter tested (Section 8)
- [ ] Controllers tested (Section 9)
- [ ] Default users created (customer/COMP5348, admin/COMP5348)
- [ ] README documentation updated
- [ ] Code follows TDD approach (Red → Green → Refactor)
- [ ] Password hashing with BCrypt strength 12
- [ ] JWT tokens with proper expiration (1 hour access, 7 days refresh)
- [ ] CORS configured for frontend (http://localhost:3000)
- [ ] Public endpoints accessible (/api/auth/**, /api/products/**, /actuator/**)
- [ ] Protected endpoints require authentication
- [ ] Global exception handling implemented
- [ ] Logging configured for security events

---

**Phase 1 User Authentication Feature Complete!**
