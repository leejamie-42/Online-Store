# Authentication Flow Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Components](#components)
4. [Authentication Flows](#authentication-flows)
5. [Security Features](#security-features)
6. [API Endpoints](#api-endpoints)
7. [Token Management](#token-management)
8. [Error Handling](#error-handling)

## Overview

The Store Backend implements a **JWT-based authentication system** with Redis-backed token management for secure, scalable user authentication and authorization.

### Key Features

- ✅ JWT (JSON Web Token) authentication with access and refresh tokens
- ✅ Redis-based refresh token storage with automatic TTL
- ✅ Token blacklisting for secure logout
- ✅ Password hashing with BCrypt
- ✅ Role-based access control (RBAC)
- ✅ Stateless authentication for horizontal scalability

### Technology Stack

- **Spring Security 6.x**: Authentication and authorization framework
- **JJWT 0.12.3**: JWT token generation and validation
- **Redis 7**: Distributed token storage and blacklist
- **BCrypt**: Password hashing algorithm
- **PostgreSQL**: User data persistence

## Architecture

### High-Level Architecture

```
┌──────────────┐
│   Client     │
│ (Frontend)   │
└──────┬───────┘
       │ HTTP + JWT
       ▼
┌──────────────────────────────────────────┐
│      Spring Security Filter Chain        │
│  ┌────────────────────────────────────┐  │
│  │  JwtAuthenticationFilter           │  │
│  │  - Extract JWT from Authorization  │  │
│  │  - Check token blacklist (Redis)   │  │
│  │  - Validate JWT signature          │  │
│  │  - Set SecurityContext             │  │
│  └────────────────────────────────────┘  │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│     AuthenticationController             │
│  POST /api/auth/register                 │
│  POST /api/auth/login                    │
│  POST /api/auth/refresh                  │
│  POST /api/auth/logout                   │
└──────────────┬───────────────────────────┘
               │
               ▼
┌──────────────────────────────────────────┐
│     AuthenticationService                │
│  - User registration and login           │
│  - Token generation                      │
│  - Token refresh and validation          │
│  - Logout and token invalidation         │
└──────┬───────────────────────────────────┘
       │
       ├─────────────┬────────────┬─────────────┐
       │             │            │             │
       ▼             ▼            ▼             ▼
┌───────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────┐
│  JwtUtil  │ │  Redis   │ │  User    │ │   Password   │
│           │ │  Token   │ │  Repo    │ │   Encoder    │
│ - Generate│ │  Service │ │          │ │              │
│ - Validate│ │          │ │ - Find   │ │ - Hash       │
│ - Extract │ │ - Store  │ │ - Save   │ │ - Verify     │
│           │ │ - Delete │ │          │ │              │
└───────────┘ └────┬─────┘ └──────────┘ └──────────────┘
                   │
                   ▼
            ┌────────────┐
            │   Redis    │
            │  Database  │
            │            │
            │ • refresh  │
            │   tokens   │
            │ • blacklist│
            └────────────┘
```

## Components

### 1. JwtAuthenticationFilter

**Location**: `src/main/java/com/comp5348/store/security/JwtAuthenticationFilter.java`

**Responsibility**: Intercept HTTP requests and validate JWT tokens

**Flow**:

```
Request → Extract Authorization header
        → Check "Bearer " prefix
        → Extract JWT token
        → Check if token is blacklisted (Redis)
        → Validate JWT signature and expiration
        → Load UserDetails
        → Set SecurityContext
        → Continue filter chain
```

**Key Code**:

```java
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain) {
    final String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        filterChain.doFilter(request, response);
        return;
    }

    final String jwt = authHeader.substring(7);

    // Check if token is blacklisted (logged out)
    if (tokenBlacklistService.isBlacklisted(jwt)) {
        log.warn("Attempted to use blacklisted token");
        filterChain.doFilter(request, response);
        return;
    }

    // Extract username and validate
    final String username = jwtUtil.extractUsername(jwt);

    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtil.validateToken(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    filterChain.doFilter(request, response);
}
```

### 2. AuthenticationService

**Location**: `src/main/java/com/comp5348/store/service/auth/AuthenticationService.java`

**Responsibility**: Core authentication business logic

**Methods**:

- `login(LoginRequest)`: Authenticate user and return tokens
- `register(RegisterRequest)`: Create new user account
- `refreshToken(String)`: Generate new access token from refresh token
- `logout(String, String)`: Invalidate user tokens

### 3. RedisTokenService

**Location**: `src/main/java/com/comp5348/store/service/auth/RedisTokenService.java`

**Responsibility**: Manage refresh tokens in Redis

**Redis Key Structure**:

- `refresh_token:{token}` → `userId (email)`
- `user_refresh:{userId}` → `{token}`

**TTL**: 7 days (604800000 ms)

**Key Operations**:

```java
// Store refresh token with TTL
storeRefreshToken(token, userId)
    → SET refresh_token:{token} {userId} EX 604800
    → SET user_refresh:{userId} {token} EX 604800

// Validate refresh token
validateRefreshToken(token)
    → GET refresh_token:{token}
    → Returns userId if exists, null if expired/invalid

// Delete refresh token (logout)
deleteRefreshToken(token)
    → GET refresh_token:{token} to get userId
    → DEL refresh_token:{token}
    → DEL user_refresh:{userId}
```

### 4. TokenBlacklistService

**Location**: `src/main/java/com/comp5348/store/service/auth/TokenBlacklistService.java`

**Responsibility**: Manage invalidated access tokens

**Redis Key Structure**:

- `blacklist:token:{token}` → `"blacklisted"`

**TTL**: Matches token's remaining expiration time

**Key Operations**:

```java
// Add token to blacklist
blacklistToken(token)
    → Extract expiration from JWT
    → Calculate remaining TTL
    → SET blacklist:token:{token} "blacklisted" EX {ttl}

// Check if token is blacklisted
isBlacklisted(token)
    → EXISTS blacklist:token:{token}
    → Returns true if key exists, false otherwise
```

### 5. JwtUtil

**Location**: `src/main/java/com/comp5348/store/util/JwtUtil.java`

**Responsibility**: JWT token generation and validation

**Token Configuration**:

- Access Token: 1 hour (3600000 ms)
- Refresh Token: 7 days (604800000 ms)
- Algorithm: HS256 (HMAC with SHA-256)
- Secret: Base64-encoded 256-bit key

## Authentication Flows

### 1. User Registration Flow

```
┌────────┐                ┌────────────┐                ┌─────────┐                ┌─────────┐
│ Client │                │ Controller │                │ Service │                │  Redis  │
└───┬────┘                └─────┬──────┘                └────┬────┘                └────┬────┘
    │                           │                            │                          │
    │ POST /api/auth/register   │                            │                          │
    │ {name, email, password}   │                            │                          │
    ├──────────────────────────>│                            │                          │
    │                           │                            │                          │
    │                           │ register(request)          │                          │
    │                           ├───────────────────────────>│                          │
    │                           │                            │                          │
    │                           │                            │ Check email exists       │
    │                           │                            │ (PostgreSQL)             │
    │                           │                            │                          │
    │                           │                            │ Hash password (BCrypt)   │
    │                           │                            │                          │
    │                           │                            │ Save user (PostgreSQL)   │
    │                           │                            │                          │
    │                           │                            │ Generate access token    │
    │                           │                            │ Generate refresh token   │
    │                           │                            │                          │
    │                           │                            │ Store refresh token      │
    │                           │                            ├─────────────────────────>│
    │                           │                            │ SET refresh_token:{token}│
    │                           │                            │     userId EX 604800     │
    │                           │                            │                          │
    │                           │ LoginResponse              │                          │
    │                           │ {accessToken, refreshToken}│                          │
    │                           │<───────────────────────────┤                          │
    │                           │                            │                          │
    │ 201 Created               │                            │                          │
    │ {accessToken, refreshToken, user}                      │                          │
    │<──────────────────────────┤                            │                          │
    │                           │                            │                          │
```

**Steps**:

1. Client sends registration request with name, email, and password
2. Controller validates request body (Bean Validation)
3. Service checks if email already exists in database
4. If email is unique, password is hashed using BCrypt (strength: 10)
5. User entity is created with role `CUSTOMER` and saved to PostgreSQL
6. JWT access token (1h TTL) and refresh token (7d TTL) are generated
7. Refresh token is stored in Redis with user email as value
8. Both tokens and user details are returned to client
9. Client stores tokens (localStorage/sessionStorage) for subsequent requests

### 2. User Login Flow

```
┌────────┐                ┌────────────┐                ┌─────────┐                ┌─────────┐
│ Client │                │ Controller │                │ Service │                │  Redis  │
└───┬────┘                └─────┬──────┘                └────┬────┘                └────┬────┘
    │                           │                            │                          │
    │ POST /api/auth/login      │                            │                          │
    │ {username, password}      │                            │                          │
    ├──────────────────────────>│                            │                          │
    │                           │                            │                          │
    │                           │ login(request)             │                          │
    │                           ├───────────────────────────>│                          │
    │                           │                            │                          │
    │                           │                            │ AuthenticationManager    │
    │                           │                            │ .authenticate()          │
    │                           │                            │ (validates password)     │
    │                           │                            │                          │
    │                           │                            │ Extract User from        │
    │                           │                            │ Authentication principal │
    │                           │                            │                          │
    │                           │                            │ Generate access token    │
    │                           │                            │ Generate refresh token   │
    │                           │                            │                          │
    │                           │                            │ Store refresh token      │
    │                           │                            ├─────────────────────────>│
    │                           │                            │ SET refresh_token:{token}│
    │                           │                            │     userId EX 604800     │
    │                           │                            │                          │
    │                           │ LoginResponse              │                          │
    │                           │<───────────────────────────┤                          │
    │                           │                            │                          │
    │ 200 OK                    │                            │                          │
    │ {accessToken, refreshToken, user}                      │                          │
    │<──────────────────────────┤                            │                          │
    │                           │                            │                          │
```

**Steps**:

1. Client sends login request with username (email) and password
2. Controller validates request body
3. Service delegates to Spring Security's `AuthenticationManager`
4. `AuthenticationManager` uses `UserDetailsService` to load user by email
5. Password is verified using BCrypt password encoder
6. If authentication succeeds, user is extracted from Authentication principal (no redundant DB query)
7. JWT access token and refresh token are generated
8. Refresh token is stored in Redis with 7-day TTL
9. Both tokens and user details are returned to client

### 3. Authenticated Request Flow

```
┌────────┐                ┌───────────┐                ┌─────────────┐                ┌─────────┐
│ Client │                │  Filter   │                │ UserDetails │                │  Redis  │
└───┬────┘                └─────┬─────┘                └──────┬──────┘                └────┬────┘
    │                           │                             │                            │
    │ GET /api/products         │                             │                            │
    │ Authorization: Bearer {token}                           │                            │
    ├──────────────────────────>│                             │                            │
    │                           │                             │                            │
    │                           │ Extract JWT from header     │                            │
    │                           │                             │                            │
    │                           │ Check if blacklisted        │                            │
    │                           ├────────────────────────────────────────────────────────>│
    │                           │ EXISTS blacklist:token:{token}                           │
    │                           │<────────────────────────────────────────────────────────┤
    │                           │ false                       │                            │
    │                           │                             │                            │
    │                           │ Extract username from JWT   │                            │
    │                           │                             │                            │
    │                           │ Load user details           │                            │
    │                           ├────────────────────────────>│                            │
    │                           │                             │ Load from PostgreSQL       │
    │                           │<────────────────────────────┤                            │
    │                           │ UserDetails                 │                            │
    │                           │                             │                            │
    │                           │ Validate JWT signature      │                            │
    │                           │ Check expiration            │                            │
    │                           │                             │                            │
    │                           │ Set SecurityContext         │                            │
    │                           │ Continue filter chain       │                            │
    │                           │                             │                            │
    │                           ├──────────────────────────────────────────────────────────┐
    │                           │                             │                            ││
    │                           │ Request reaches controller  │                            ││
    │                           │ with authenticated user     │                            ││
    │                           │<─────────────────────────────────────────────────────────┘
    │                           │                             │                            │
    │ 200 OK                    │                             │                            │
    │ {products: [...]}         │                             │                            │
    │<──────────────────────────┤                             │                            │
    │                           │                             │                            │
```

**Steps**:

1. Client sends request with `Authorization: Bearer {accessToken}` header
2. `JwtAuthenticationFilter` intercepts the request
3. Token is extracted from Authorization header
4. Filter checks if token is blacklisted in Redis
5. If not blacklisted, username is extracted from JWT
6. `UserDetailsService` loads user from PostgreSQL
7. JWT signature and expiration are validated
8. If valid, `SecurityContext` is set with authenticated user
9. Request proceeds to controller with authentication
10. Controller can access authenticated user via `@AuthenticationPrincipal` or `SecurityContextHolder`

### 4. Token Refresh Flow

```
┌────────┐                ┌────────────┐                ┌─────────┐                ┌─────────┐
│ Client │                │ Controller │                │ Service │                │  Redis  │
└───┬────┘                └─────┬──────┘                └────┬────┘                └────┬────┘
    │                           │                            │                          │
    │ POST /api/auth/refresh    │                            │                          │
    │ {refreshToken}            │                            │                          │
    ├──────────────────────────>│                            │                          │
    │                           │                            │                          │
    │                           │ refreshToken(token)        │                          │
    │                           ├───────────────────────────>│                          │
    │                           │                            │                          │
    │                           │                            │ Validate token in Redis  │
    │                           │                            ├─────────────────────────>│
    │                           │                            │ GET refresh_token:{token}│
    │                           │                            │<─────────────────────────┤
    │                           │                            │ userId (if exists)       │
    │                           │                            │                          │
    │                           │                            │ Extract username from JWT│
    │                           │                            │                          │
    │                           │                            │ Verify username matches  │
    │                           │                            │ Redis userId             │
    │                           │                            │                          │
    │                           │                            │ Load user from DB        │
    │                           │                            │                          │
    │                           │                            │ Validate JWT signature   │
    │                           │                            │                          │
    │                           │                            │ Generate new access token│
    │                           │                            │                          │
    │                           │ newAccessToken             │                          │
    │                           │<───────────────────────────┤                          │
    │                           │                            │                          │
    │ 200 OK                    │                            │                          │
    │ {accessToken}             │                            │                          │
    │<──────────────────────────┤                            │                          │
    │                           │                            │                          │
```

**Steps**:

1. Client sends refresh token when access token expires (1h)
2. Service validates refresh token exists in Redis
3. If found, username is extracted from JWT
4. Username is verified against the userId stored in Redis
5. User is loaded from PostgreSQL for additional validation
6. JWT signature and expiration are validated
7. If all validations pass, new access token is generated (1h TTL)
8. New access token is returned to client
9. Refresh token remains valid until its 7-day TTL expires

**Security Notes**:

- Refresh token is validated in Redis before JWT validation
- Username mismatch between JWT and Redis is rejected
- Refresh token is single-use in production (can implement rotation)

### 5. Logout Flow

```
┌────────┐                ┌────────────┐                ┌─────────┐                ┌─────────┐
│ Client │                │ Controller │                │ Service │                │  Redis  │
└───┬────┘                └─────┬──────┘                └────┬────┘                └────┬────┘
    │                           │                            │                          │
    │ POST /api/auth/logout     │                            │                          │
    │ Authorization: Bearer {accessToken}                    │                          │
    │ {refreshToken}            │                            │                          │
    ├──────────────────────────>│                            │                          │
    │                           │                            │                          │
    │                           │ Extract access token       │                          │
    │                           │ from Authorization header  │                          │
    │                           │                            │                          │
    │                           │ logout(accessToken,        │                          │
    │                           │        refreshToken)       │                          │
    │                           ├───────────────────────────>│                          │
    │                           │                            │                          │
    │                           │                            │ Blacklist access token   │
    │                           │                            ├─────────────────────────>│
    │                           │                            │ Extract expiration from  │
    │                           │                            │ JWT, calculate TTL       │
    │                           │                            │ SET blacklist:token:     │
    │                           │                            │     {token} "blacklisted"│
    │                           │                            │     EX {remaining_ttl}   │
    │                           │                            │                          │
    │                           │                            │ Delete refresh token     │
    │                           │                            ├─────────────────────────>│
    │                           │                            │ GET refresh_token:{token}│
    │                           │                            │ to get userId            │
    │                           │                            │ DEL refresh_token:{token}│
    │                           │                            │ DEL user_refresh:{userId}│
    │                           │                            │                          │
    │                           │ Success                    │                          │
    │                           │<───────────────────────────┤                          │
    │                           │                            │                          │
    │ 200 OK                    │                            │                          │
    │ {message: "Logged out successfully"}                   │                          │
    │<──────────────────────────┤                            │                          │
    │                           │                            │                          │
    │ Clear tokens from storage │                            │                          │
    │                           │                            │                          │
```

**Steps**:

1. Client sends logout request with access token in header and refresh token in body
2. Controller extracts access token from `Authorization: Bearer {token}` header
3. Service receives both tokens for invalidation
4. Access token is added to blacklist in Redis with TTL matching token's remaining expiration
5. Refresh token is deleted from Redis (both `refresh_token:{token}` and `user_refresh:{userId}` keys)
6. Success response is returned
7. Client clears tokens from local storage
8. Subsequent requests with blacklisted access token are rejected by `JwtAuthenticationFilter`

**Token Invalidation**:

- Access Token: Blacklisted until natural expiration (max 1 hour)
- Refresh Token: Immediately deleted, cannot be used to refresh access tokens

## Security Features

### 1. Password Security

**Hashing Algorithm**: BCrypt with default strength (10 rounds)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Storage**:

- Plain passwords are NEVER stored
- BCrypt generates salt automatically
- Each password has unique salt
- Hash stored in PostgreSQL `users.password` column

### 2. JWT Token Security

**Token Structure**:

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNjk1MTI0ODAwLCJleHAiOjE2OTUxMjg0MDB9.
signature
```

**Parts**:

1. **Header**: Algorithm (HS256) and type (JWT)
2. **Payload**: Subject (email), issued at, expiration
3. **Signature**: HMAC-SHA256(header + payload, secret)

**Security Measures**:

- Secret key stored in environment variables (256-bit minimum)
- Tokens signed with HMAC-SHA256
- Expiration enforced (access: 1h, refresh: 7d)
- Tokens validated on every request

### 3. Token Blacklist

**Purpose**: Prevent use of access tokens after logout

**Implementation**:

- Redis-based distributed blacklist
- TTL matches token's remaining expiration
- Automatic cleanup by Redis
- Checked before authentication

**Trade-offs**:

- ✅ Immediate token revocation
- ✅ Distributed across instances
- ❌ Requires Redis dependency
- ❌ Slight latency overhead

### 4. Refresh Token Management

**Security Benefits**:

- Short-lived access tokens (1h) limit exposure window
- Refresh tokens stored server-side in Redis
- Refresh tokens can be revoked instantly
- User-specific refresh token mapping

**Rotation Strategy** (Future Enhancement):

- Generate new refresh token on each refresh request
- Invalidate old refresh token
- Detect token reuse (potential compromise)

### 5. Role-Based Access Control

**User Roles**:

- `CUSTOMER`: Default role for registered users
- `ADMIN`: Administrative access (future)
- `MANAGER`: Management access (future)

**Authorization**:

```java
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/admin/users")
public List<User> getAllUsers() {
    // Only accessible by ADMIN role
}
```

### 6. Security Best Practices

**Implemented**:

- ✅ HTTPS required in production
- ✅ Password strength validation (min 8 chars)
- ✅ Email validation
- ✅ SQL injection prevention (JPA)
- ✅ CSRF protection (Spring Security)
- ✅ CORS configuration
- ✅ Rate limiting via Redis (future)

## API Endpoints

### POST /api/auth/register

**Description**: Register a new user account

**Request**:

```json
{
  "name": "John Doe",
  "email": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Validation Rules**:

- `name`: Required, 2-100 characters
- `email`: Required, valid email format
- `password`: Required, minimum 8 characters

**Success Response** (201 Created):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "CUSTOMER"
  }
}
```

**Error Responses**:

- `400 Bad Request`: Validation errors
- `409 Conflict`: Email already exists

### POST /api/auth/login

**Description**: Authenticate user and receive JWT tokens

**Request**:

```json
{
  "username": "john.doe@example.com",
  "password": "SecurePassword123!"
}
```

**Success Response** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "role": "CUSTOMER"
  }
}
```

**Error Responses**:

- `400 Bad Request`: Validation errors
- `401 Unauthorized`: Invalid credentials

### POST /api/auth/refresh

**Description**: Obtain new access token using refresh token

**Request**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Error Responses**:

- `401 Unauthorized`: Invalid or expired refresh token

**Usage**:

- Call when access token expires (1 hour)
- Client should handle 401 errors by attempting token refresh
- If refresh fails, redirect to login

### POST /api/auth/logout

**Description**: Invalidate user tokens and logout

**Request Headers**:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Request Body**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Success Response** (200 OK):

```json
{
  "message": "Logged out successfully"
}
```

**Post-Logout**:

- Client should clear tokens from storage
- Subsequent requests with logged-out access token return 401
- Refresh token cannot be used to obtain new access tokens

## Token Management

### Access Token

**Purpose**: Short-lived token for API authentication

**Lifetime**: 1 hour (3600000 ms)

**Storage**: Client-side (localStorage/sessionStorage)

**Claims**:

```json
{
  "sub": "user@example.com",
  "iat": 1695124800,
  "exp": 1695128400
}
```

**Usage**:

```javascript
// Store after login/register
localStorage.setItem('accessToken', response.accessToken);

// Add to requests
headers: {
  'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
}

// Clear on logout
localStorage.removeItem('accessToken');
```

### Refresh Token

**Purpose**: Long-lived token for obtaining new access tokens

**Lifetime**: 7 days (604800000 ms)

**Storage**:

- Client: localStorage/sessionStorage
- Server: Redis with TTL

**Claims**: Same as access token

**Usage**:

```javascript
// Store after login/register
localStorage.setItem("refreshToken", response.refreshToken);

// Use when access token expires
const response = await fetch("/api/auth/refresh", {
  method: "POST",
  body: JSON.stringify({
    refreshToken: localStorage.getItem("refreshToken"),
  }),
});

const { accessToken } = await response.json();
localStorage.setItem("accessToken", accessToken);

// Clear on logout
localStorage.removeItem("refreshToken");
```

### Token Expiration Handling

**Client-Side Strategy**:

```javascript
async function makeAuthenticatedRequest(url, options) {
  // Add access token
  options.headers = {
    ...options.headers,
    Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
  };

  let response = await fetch(url, options);

  // If 401, try refreshing token
  if (response.status === 401) {
    const refreshResponse = await fetch("/api/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        refreshToken: localStorage.getItem("refreshToken"),
      }),
    });

    if (refreshResponse.ok) {
      const { accessToken } = await refreshResponse.json();
      localStorage.setItem("accessToken", accessToken);

      // Retry original request with new token
      options.headers.Authorization = `Bearer ${accessToken}`;
      response = await fetch(url, options);
    } else {
      // Refresh failed, redirect to login
      localStorage.clear();
      window.location.href = "/login";
    }
  }

  return response;
}
```

## Error Handling

### Authentication Errors

**401 Unauthorized**:

- Invalid credentials (login)
- Invalid or expired JWT token
- Invalid refresh token
- Blacklisted token (logged out)

**Response Format**:

```json
{
  "error": "Unauthorized",
  "message": "Invalid credentials"
}
```

### Validation Errors

**400 Bad Request**:

- Missing required fields
- Invalid email format
- Password too short
- Invalid request body

**Response Format**:

```json
{
  "error": "Bad Request",
  "fieldErrors": {
    "name": "Name is required",
    "email": "Email must be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

### Business Logic Errors

**409 Conflict**:

- Email already exists (registration)

**Response Format**:

```json
{
  "error": "Conflict",
  "message": "User already exists with email: john.doe@example.com"
}
```

### Server Errors

**500 Internal Server Error**:

- Redis connection failure
- Database connection failure
- Unexpected exceptions

**Response Format**:

```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

**Error Handling Strategy**:

- Log errors for debugging
- Return generic messages to client (no sensitive details)
- Graceful degradation (e.g., continue if Redis unavailable)

## Additional Resources

- **Redis Setup Guide**: `store-backend/REDIS_SETUP.md`
- **Postman Collection**: `store-backend/postman/README.md`
- **Main Documentation**: `CLAUDE.md`
- **API Tests**: `store-backend/src/test/java/com/comp5348/store/controller/AuthenticationControllerTest.java`
- **Service Tests**: `store-backend/src/test/java/com/comp5348/store/service/auth/AuthenticationServiceTest.java`
