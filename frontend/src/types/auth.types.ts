/**
 * Authentication Types - Aligned with Backend JWT API
 *
 * Backend API Documentation: docs/AUTHENTICATION_FLOW.md
 * Backend Port: 8081
 */

// User role enumeration
export type UserRole = "CUSTOMER" | "ADMIN" | "MANAGER";

// User entity as returned by backend
export interface User {
  id: number; // Backend uses Long (numeric ID)
  name: string; // User's full name
  email: string; // Email address (unique)
  role: UserRole; // User role for RBAC
}

// POST /api/auth/login request
export interface LoginRequest {
  username: string; // Backend expects 'username' field (email value)
  password: string; // Plain password (hashed on backend)
}

// POST /api/auth/login response
export interface LoginResponse {
  accessToken: string; // JWT access token (1h TTL)
  refreshToken: string; // JWT refresh token (7d TTL)
  user: User; // Authenticated user data
}

// POST /api/auth/register request
export interface RegisterRequest {
  name: string; // 2-100 characters
  email: string; // Valid email format
  password: string; // Minimum 8 characters
}

// POST /api/auth/register response (same as login - auto-login)
export interface RegisterResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

// POST /api/auth/refresh request
export interface RefreshTokenRequest {
  refreshToken: string;
}

// POST /api/auth/refresh response
export interface RefreshTokenResponse {
  accessToken: string; // Only returns new access token, not refresh token
}

// POST /api/auth/logout request
export interface LogoutRequest {
  refreshToken: string; // Sent in request body
  // Note: Access token sent in Authorization header
}

// Error response structure
export interface AuthErrorResponse {
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
