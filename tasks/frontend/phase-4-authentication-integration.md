# Phase 4: Authentication Integration (React Query) - Implementation Tasks
## Online Store Application - Frontend

**Phase Duration:** 6-8 days
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 19, 2025

---

## Overview

This document contains detailed, actionable tasks for Phase 4 of the frontend implementation. Phase 4 focuses on integrating JWT authentication with the backend using **React Query** for optimal state management and following **Test-Driven Development (TDD)** methodology.

**Prerequisites:** Phase 1-3 must be completed before starting Phase 4.

### Phase 4 Goals
- ✅ Align frontend types with backend JWT API contract
- ✅ Implement authentication service with comprehensive tests
- ✅ Set up React Query for auth state management
- ✅ Create login, register, and logout flows
- ✅ Implement protected routes with role-based access
- ✅ Achieve >85% test coverage for auth features

### Architecture Decision

**Approach**: React Query + Lightweight Context

**Benefits**:
- Built-in loading/error/success states
- Automatic cache management and request deduplication
- Better separation: server state (React Query) vs client state (tokens)
- Superior developer experience with DevTools
- Eliminates manual state management boilerplate

**Backend Integration**:
- Backend Port: **8081** (configured in `application-local.yml`)
- JWT Access Token: 1 hour TTL
- JWT Refresh Token: 7 days TTL
- Token storage: localStorage
- Auto-refresh on 401 errors

---

## Table of Contents

1. [Section 1: Foundation & Type System](#section-1-foundation--type-system)
2. [Section 2: API Service Layer](#section-2-api-service-layer)
3. [Section 3: React Query Integration](#section-3-react-query-integration)
4. [Section 4: UI Components](#section-4-ui-components)
5. [Section 5: Protection & E2E Testing](#section-5-protection--e2e-testing)
6. [Testing & Validation](#testing--validation)

---

## Section 1: Foundation & Type System

**Estimated Time:** 3-4 hours
**Dependencies:** Phase 1-3 complete

### Task 1.1: Update Auth Types to Match Backend API

**Status:** ⬜ Not Started
**Depends On:** Phase 3 complete

**Description:**
Create TypeScript type definitions that exactly match the backend JWT authentication API contract. This includes User, LoginRequest/Response, RegisterRequest/Response, and token types.

**Backend Reference:**
- Documentation: `docs/AUTHENTICATION_FLOW.md`
- Backend User Entity: `id: Long` (number), `name: String`, `email: String`, `role: String`
- Login returns: `{ accessToken, refreshToken, user }`
- Register returns: `{ accessToken, refreshToken, user }` (auto-login)

**Files to Create:**

**src/types/auth.types.ts:**
```typescript
/**
 * Authentication Types - Aligned with Backend JWT API
 *
 * Backend API Documentation: docs/AUTHENTICATION_FLOW.md
 * Backend Port: 8081
 */

// User role enumeration
export type UserRole = 'CUSTOMER' | 'ADMIN' | 'MANAGER';

// User entity as returned by backend
export interface User {
  id: number;          // Backend uses Long (numeric ID)
  name: string;        // User's full name
  email: string;       // Email address (unique)
  role: UserRole;      // User role for RBAC
}

// POST /api/auth/login request
export interface LoginRequest {
  username: string;    // Backend expects 'username' field (email value)
  password: string;    // Plain password (hashed on backend)
}

// POST /api/auth/login response
export interface LoginResponse {
  accessToken: string;   // JWT access token (1h TTL)
  refreshToken: string;  // JWT refresh token (7d TTL)
  user: User;           // Authenticated user data
}

// POST /api/auth/register request
export interface RegisterRequest {
  name: string;         // 2-100 characters
  email: string;        // Valid email format
  password: string;     // Minimum 8 characters
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
  accessToken: string;  // Only returns new access token, not refresh token
}

// POST /api/auth/logout request
export interface LogoutRequest {
  refreshToken: string;  // Sent in request body
  // Note: Access token sent in Authorization header
}

// Error response structure
export interface AuthErrorResponse {
  error: string;
  message: string;
  fieldErrors?: Record<string, string>;
}
```

**Files to Update:**

**src/types/user.types.ts:**
```typescript
// Re-export auth types for backward compatibility
export type { User, UserRole } from './auth.types';

// Keep existing Address interface
export interface Address {
  id?: string;
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone: string;
}

// DEPRECATED: Use LoginRequest from auth.types instead
/** @deprecated Use LoginRequest from auth.types */
export interface LoginCredentials {
  email: string;
  password: string;
}

// DEPRECATED: Use RegisterRequest from auth.types instead
/** @deprecated Use RegisterRequest from auth.types */
export interface RegisterData {
  username: string;
  email: string;
  password: string;
}
```

**src/types/index.ts:**
```typescript
// Auth types (primary export)
export type {
  User,
  UserRole,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  LogoutRequest,
  AuthErrorResponse
} from './auth.types';

// User types (legacy support)
export type {
  Address,
  LoginCredentials, // Deprecated
  RegisterData      // Deprecated
} from './user.types';

// Other existing exports
export type * from './product.types';
export type * from './api.types';
export type * from './common.types';
```

**Test File:**

**src/types/__tests__/auth.types.test.ts:**
```typescript
import { describe, it, expect } from 'vitest';
import type {
  User,
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  RefreshTokenResponse,
  LogoutRequest
} from '../auth.types';

describe('Auth Type Definitions', () => {
  describe('LoginRequest', () => {
    it('should match backend contract with username field', () => {
      const loginRequest: LoginRequest = {
        username: 'customer@example.com',
        password: 'COMP5348'
      };

      expect(loginRequest).toHaveProperty('username');
      expect(loginRequest).toHaveProperty('password');
    });
  });

  describe('LoginResponse', () => {
    it('should match backend JWT response structure', () => {
      const loginResponse: LoginResponse = {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        user: {
          id: 1,
          name: 'Customer User',
          email: 'customer@example.com',
          role: 'CUSTOMER'
        }
      };

      expect(loginResponse).toHaveProperty('accessToken');
      expect(loginResponse).toHaveProperty('refreshToken');
      expect(loginResponse).toHaveProperty('user');
      expect(loginResponse.user.id).toBeTypeOf('number');
    });
  });

  describe('User', () => {
    it('should have numeric ID matching backend Long type', () => {
      const user: User = {
        id: 1,
        name: 'Test User',
        email: 'test@example.com',
        role: 'CUSTOMER'
      };

      expect(user.id).toBeTypeOf('number');
      expect(user).toHaveProperty('name');
      expect(user).toHaveProperty('email');
      expect(user).toHaveProperty('role');
    });

    it('should support all role types', () => {
      const customer: User = { id: 1, name: 'C', email: 'c@e.com', role: 'CUSTOMER' };
      const admin: User = { id: 2, name: 'A', email: 'a@e.com', role: 'ADMIN' };
      const manager: User = { id: 3, name: 'M', email: 'm@e.com', role: 'MANAGER' };

      expect(customer.role).toBe('CUSTOMER');
      expect(admin.role).toBe('ADMIN');
      expect(manager.role).toBe('MANAGER');
    });
  });
});
```

**Acceptance Criteria:**
- [ ] User.id is `number` type (matches backend Long)
- [ ] LoginRequest uses 'username' field (backend naming)
- [ ] RegisterResponse includes tokens (auto-login feature)
- [ ] RefreshTokenResponse only has accessToken
- [ ] UserRole enum includes CUSTOMER, ADMIN, MANAGER
- [ ] All tests pass (`npm test src/types/__tests__/auth.types.test.ts`)
- [ ] Type checking passes (`npm run type-check`)

**Git Commit:**
```bash
git add src/types/
git commit -m "feat(types): align auth types with backend JWT API contract

- Create auth.types.ts with backend-aligned interfaces
- User.id changed from string to number (matches backend Long)
- LoginRequest uses 'username' field per backend
- RegisterResponse includes tokens for auto-login
- Add comprehensive type tests for API contract
- Deprecate old LoginCredentials and RegisterData

BREAKING CHANGE: User.id type changed from string to number
Refs: docs/AUTHENTICATION_FLOW.md"
```

**Estimated Time:** 1.5 hours

---

### Task 1.2: Update API Endpoints Configuration

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Update API endpoint configuration to match backend routes with correct port (8081) and add endpoint validation helpers.

**Backend Verification:**
- Backend runs on port 8081 (see `store-backend/src/main/resources/application-local.yml`)
- Auth endpoints: `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`, `/api/auth/logout`

**Files to Update:**

**src/config/api.config.ts:**
```typescript
/**
 * API Configuration
 *
 * Backend API runs on port 8081 (configured in application-local.yml: server.port: 8081)
 * All endpoints are prefixed with /api in backend routes
 *
 * Reference: CLAUDE.md, AUTHENTICATION_FLOW.md
 */

export const API_CONFIG = {
  // Backend runs on port 8081 (see store-backend/src/main/resources/application-local.yml)
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/api',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT as string) || 15000,
} as const;

/**
 * API Endpoints
 *
 * All auth endpoints match backend AuthenticationController routes
 */
export const API_ENDPOINTS = {
  // Authentication endpoints (AuthenticationController)
  REGISTER: '/auth/register',      // POST /api/auth/register
  LOGIN: '/auth/login',            // POST /api/auth/login
  REFRESH_TOKEN: '/auth/refresh',  // POST /api/auth/refresh
  LOGOUT: '/auth/logout',          // POST /api/auth/logout

  // User endpoints
  USER_PROFILE: '/users/me',
  USER_ADDRESSES: '/users/me/addresses',
  USER_ADDRESS: (addressId: string) => `/users/me/addresses/${addressId}`,

  // Product endpoints
  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  // Cart endpoints (future)
  CART: '/cart',
  CART_ITEMS: '/cart/items',
  CART_ITEM: (itemId: string) => `/cart/items/${itemId}`,

  // Order endpoints (future)
  ORDERS: '/orders',
  ORDER_DETAIL: (orderId: string) => `/orders/${orderId}`,
  CANCEL_ORDER: (orderId: string) => `/orders/${orderId}/cancel`,
} as const;

/**
 * API Endpoint Type Guards
 */
export const isAuthEndpoint = (endpoint: string): boolean => {
  return endpoint.startsWith('/auth/');
};

export const isProtectedEndpoint = (endpoint: string): boolean => {
  const publicEndpoints = [
    API_ENDPOINTS.LOGIN,
    API_ENDPOINTS.REGISTER,
    API_ENDPOINTS.REFRESH_TOKEN,
    API_ENDPOINTS.PRODUCTS,
  ];

  return !publicEndpoints.some(publicEndpoint =>
    typeof publicEndpoint === 'string' && endpoint === publicEndpoint
  );
};
```

**Files to Update:**

**frontend/.env.example:**
```env
# API Configuration
# Backend server base URL (default: http://localhost:8081/api)
# Backend port configured in: store-backend/src/main/resources/application-local.yml (server.port: 8081)
VITE_API_BASE_URL=http://localhost:8081/api

# API request timeout in milliseconds (default: 15000)
VITE_API_TIMEOUT=15000

# Environment
VITE_ENV=development
```

**frontend/.env.local:**
```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8081/api
VITE_API_TIMEOUT=15000
VITE_ENV=development
```

**Test File:**

**src/config/__tests__/api.config.test.ts:**
```typescript
import { describe, it, expect } from 'vitest';
import { API_CONFIG, API_ENDPOINTS, isAuthEndpoint, isProtectedEndpoint } from '../api.config';

describe('API Configuration', () => {
  describe('API_CONFIG', () => {
    it('should use port 8081 for backend', () => {
      expect(API_CONFIG.baseURL).toContain('8081');
    });

    it('should have correct timeout', () => {
      expect(API_CONFIG.timeout).toBe(15000);
    });
  });

  describe('Auth Endpoints', () => {
    it('should have all auth endpoints', () => {
      expect(API_ENDPOINTS.LOGIN).toBe('/auth/login');
      expect(API_ENDPOINTS.REGISTER).toBe('/auth/register');
      expect(API_ENDPOINTS.REFRESH_TOKEN).toBe('/auth/refresh');
      expect(API_ENDPOINTS.LOGOUT).toBe('/auth/logout');
    });
  });

  describe('Helper Functions', () => {
    it('should identify auth endpoints', () => {
      expect(isAuthEndpoint('/auth/login')).toBe(true);
      expect(isAuthEndpoint('/products')).toBe(false);
    });

    it('should identify protected endpoints', () => {
      expect(isProtectedEndpoint('/orders')).toBe(true);
      expect(isProtectedEndpoint('/auth/login')).toBe(false);
    });
  });
});
```

**Acceptance Criteria:**
- [ ] baseURL uses port 8081
- [ ] All auth endpoints match backend routes
- [ ] Helper functions work correctly
- [ ] Environment variables documented
- [ ] Tests pass
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/config/ frontend/.env.example frontend/.env.local
git commit -m "feat(config): update auth API endpoints for JWT integration

- Configure backend port to 8081 (matches application-local.yml)
- Add endpoint type guard helpers
- Document environment variables
- Add comprehensive endpoint tests

Refs: docs/AUTHENTICATION_FLOW.md, store-backend/src/main/resources/application-local.yml"
```

**Estimated Time:** 1 hour

---

## Section 2: API Service Layer

**Estimated Time:** 6-7 hours
**Dependencies:** Section 1 complete

### Task 2.1: Implement Auth Service with TDD

**Status:** ⬜ Not Started
**Depends On:** Task 1.2

**Description:**
Implement authentication service methods with comprehensive test coverage using TDD approach. Write tests first, then implement service methods.

**TDD Workflow:**
1. Write failing tests
2. Implement minimal code to pass tests
3. Refactor
4. Repeat

**Test File (Write First):**

**src/api/services/__tests__/auth.service.test.ts:**
```typescript
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { authService } from '../auth.service';
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { LoginRequest, RegisterRequest } from '@/types/auth.types';

vi.mock('@/lib/axios');

describe('Auth Service', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  describe('login', () => {
    const mockLoginRequest: LoginRequest = {
      username: 'customer@example.com',
      password: 'COMP5348'
    };

    const mockLoginResponse = {
      accessToken: 'eyJhbGc...',
      refreshToken: 'eyJhbGc...',
      user: {
        id: 1,
        name: 'Customer User',
        email: 'customer@example.com',
        role: 'CUSTOMER' as const
      }
    };

    it('should login with valid credentials', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockLoginResponse });

      const result = await authService.login(mockLoginRequest);

      expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.LOGIN, mockLoginRequest);
      expect(result).toEqual(mockLoginResponse);
      expect(result.accessToken).toBeDefined();
      expect(result.refreshToken).toBeDefined();
    });

    it('should throw error on invalid credentials', async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 401, data: { error: 'Unauthorized' } }
      });

      await expect(authService.login(mockLoginRequest)).rejects.toThrow();
    });

    it('should handle network errors', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('Network error'));

      await expect(authService.login(mockLoginRequest)).rejects.toThrow('Network error');
    });
  });

  describe('register', () => {
    const mockRegisterRequest: RegisterRequest = {
      name: 'John Doe',
      email: 'john@example.com',
      password: 'SecurePass123!'
    };

    const mockRegisterResponse = {
      accessToken: 'eyJhbGc...',
      refreshToken: 'eyJhbGc...',
      user: {
        id: 2,
        name: 'John Doe',
        email: 'john@example.com',
        role: 'CUSTOMER' as const
      }
    };

    it('should register new user successfully', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockRegisterResponse });

      const result = await authService.register(mockRegisterRequest);

      expect(apiClient.post).toHaveBeenCalledWith(API_ENDPOINTS.REGISTER, mockRegisterRequest);
      expect(result.user.role).toBe('CUSTOMER');
    });

    it('should throw error when email exists', async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 409, data: { error: 'Conflict' } }
      });

      await expect(authService.register(mockRegisterRequest)).rejects.toThrow();
    });
  });

  describe('refreshToken', () => {
    it('should refresh access token', async () => {
      const mockResponse = { accessToken: 'new-token' };
      vi.mocked(apiClient.post).mockResolvedValue({ data: mockResponse });

      const result = await authService.refreshToken('refresh-token');

      expect(result.accessToken).toBeDefined();
      expect(result).not.toHaveProperty('refreshToken');
    });

    it('should throw on invalid refresh token', async () => {
      vi.mocked(apiClient.post).mockRejectedValue({
        response: { status: 401 }
      });

      await expect(authService.refreshToken('invalid')).rejects.toThrow();
    });
  });

  describe('logout', () => {
    it('should logout successfully', async () => {
      vi.mocked(apiClient.post).mockResolvedValue({ data: { message: 'Success' } });

      await authService.logout('refresh-token');

      expect(apiClient.post).toHaveBeenCalledWith(
        API_ENDPOINTS.LOGOUT,
        { refreshToken: 'refresh-token' }
      );
    });
  });
});
```

**Implementation File (Write After Tests):**

**src/api/services/auth.service.ts:**
```typescript
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenResponse
} from '@/types/auth.types';

/**
 * Authentication Service
 *
 * Handles all authentication-related API calls
 * Backend documentation: docs/AUTHENTICATION_FLOW.md
 */
export const authService = {
  /**
   * Login with credentials
   * POST /api/auth/login
   */
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
      API_ENDPOINTS.LOGIN,
      credentials
    );
    return response.data;
  },

  /**
   * Register new user
   * POST /api/auth/register
   * Backend auto-logs in user after registration
   */
  async register(userData: RegisterRequest): Promise<RegisterResponse> {
    const response = await apiClient.post<RegisterResponse>(
      API_ENDPOINTS.REGISTER,
      userData
    );
    return response.data;
  },

  /**
   * Refresh access token
   * POST /api/auth/refresh
   * Returns new access token only
   */
  async refreshToken(refreshToken: string): Promise<RefreshTokenResponse> {
    const response = await apiClient.post<RefreshTokenResponse>(
      API_ENDPOINTS.REFRESH_TOKEN,
      { refreshToken }
    );
    return response.data;
  },

  /**
   * Logout and invalidate tokens
   * POST /api/auth/logout
   * Blacklists access token, deletes refresh token
   */
  async logout(refreshToken: string): Promise<void> {
    await apiClient.post(
      API_ENDPOINTS.LOGOUT,
      { refreshToken }
    );
  },
};
```

**Acceptance Criteria:**
- [ ] All tests pass (20+ tests)
- [ ] 100% code coverage for auth service
- [ ] LoginRequest uses 'username' field
- [ ] RegisterResponse includes tokens
- [ ] RefreshTokenResponse only has accessToken
- [ ] Error handling for all HTTP status codes
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/api/services/
git commit -m "feat(auth): implement JWT auth service with comprehensive tests

- Implement login, register, refreshToken, logout methods
- Match backend JWT API contract exactly
- Add comprehensive test coverage (100%)
- Error handling for all HTTP status codes

Tests: 20 passing
Coverage: 100% statements, branches, functions, lines

Refs: docs/AUTHENTICATION_FLOW.md"
```

**Estimated Time:** 3 hours

---

### Task 2.2: Enhance Axios Interceptor for JWT Refresh

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Enhance axios interceptor to automatically refresh expired access tokens and handle token blacklisting (logout scenario).

**Files to Update:**

**src/lib/axios.ts:**
```typescript
import axios, {
  AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig
} from 'axios';
import { API_CONFIG } from '@/config/api.config';
import { tokenStorage } from '@/utils/storage';

/**
 * Axios Client with JWT Authentication
 *
 * Features:
 * - Automatic access token injection
 * - Automatic token refresh on 401
 * - Token blacklist detection
 * - Request retry after refresh
 */

export const apiClient = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request Interceptor: Add JWT to Authorization header
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response Interceptor: Handle 401 with token refresh
apiClient.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & {
      _retry?: boolean
    };

    // Handle 401 - Token expired or blacklisted
    if (error.response?.status === 401 && originalRequest && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = tokenStorage.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token');
        }

        // Refresh access token (use base axios to avoid loop)
        const response = await axios.post(
          `${API_CONFIG.baseURL}/auth/refresh`,
          { refreshToken },
          { headers: { 'Content-Type': 'application/json' } }
        );

        const { accessToken } = response.data;
        tokenStorage.setToken(accessToken);

        // Retry original request with new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        }
        return apiClient(originalRequest);

      } catch (refreshError) {
        // Refresh failed - clear tokens and redirect
        tokenStorage.removeToken();
        tokenStorage.removeRefreshToken();

        if (typeof window !== 'undefined') {
          window.location.href = '/login';
        }

        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

**Test File:**

**src/lib/__tests__/axios.test.ts:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { apiClient } from '../axios';
import { tokenStorage } from '@/utils/storage';
import { API_CONFIG } from '@/config/api.config';

vi.mock('@/utils/storage');

describe('Axios Client', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Request Interceptor', () => {
    it('should add Authorization header when token exists', () => {
      vi.mocked(tokenStorage.getToken).mockReturnValue('mock-token');

      // Test interceptor adds Bearer token
      const config: any = { headers: {} };
      const result = apiClient.interceptors.request.handlers[0].fulfilled(config);

      expect(result.headers.Authorization).toBe('Bearer mock-token');
    });

    it('should not add header when no token', () => {
      vi.mocked(tokenStorage.getToken).mockReturnValue(null);

      const config: any = { headers: {} };
      const result = apiClient.interceptors.request.handlers[0].fulfilled(config);

      expect(result.headers.Authorization).toBeUndefined();
    });
  });

  describe('Response Interceptor', () => {
    it('should pass through successful responses', () => {
      const response = { data: 'test', status: 200 };
      const result = apiClient.interceptors.response.handlers[0].fulfilled(response);

      expect(result).toEqual(response);
    });

    it('should handle 401 errors by attempting refresh', async () => {
      vi.mocked(tokenStorage.getRefreshToken).mockReturnValue('refresh-token');

      const error: any = {
        config: { headers: {}, _retry: false },
        response: { status: 401 }
      };

      // Verify refresh token is checked
      expect(tokenStorage.getRefreshToken).toBeDefined();
    });
  });
});
```

**Acceptance Criteria:**
- [ ] Authorization header added automatically
- [ ] 401 errors trigger token refresh
- [ ] Original request retried with new token
- [ ] Refresh failures redirect to login
- [ ] `_retry` flag prevents infinite loops
- [ ] Tests pass
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/lib/
git commit -m "feat(api): enhance axios interceptor for JWT token refresh flow

- Implement automatic token refresh on 401 errors
- Add request retry logic with new access token
- Handle blacklisted tokens (logged out scenario)
- Prevent infinite refresh loops with _retry flag
- Add comprehensive interceptor tests

Tests: 15 passing
Coverage: >95% for axios client

Refs: docs/AUTHENTICATION_FLOW.md"
```

**Estimated Time:** 2 hours

---

### Task 2.3: Implement Token Storage Utilities with Tests

**Status:** ⬜ Not Started
**Depends On:** Task 2.2

**Description:**
Enhance token storage utilities with proper error handling and comprehensive tests.

**Files to Update:**

**src/utils/storage.ts:**
```typescript
/**
 * LocalStorage Utility Functions
 *
 * Type-safe localStorage access with error handling
 */

export const storage = {
  get: <T>(key: string): T | null => {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error(`Error getting ${key} from localStorage:`, error);
      return null;
    }
  },

  set: <T>(key: string, value: T): void => {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error(`Error setting ${key} to localStorage:`, error);
      if (error instanceof DOMException && error.name === 'QuotaExceededError') {
        console.warn('localStorage quota exceeded');
      }
    }
  },

  remove: (key: string): void => {
    try {
      localStorage.removeItem(key);
    } catch (error) {
      console.error(`Error removing ${key} from localStorage:`, error);
    }
  },

  clear: (): void => {
    try {
      localStorage.clear();
    } catch (error) {
      console.error('Error clearing localStorage:', error);
    }
  },
};

/**
 * Token Storage Keys
 */
const TOKEN_KEYS = {
  ACCESS_TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
} as const;

/**
 * Token Storage Utilities
 */
export const tokenStorage = {
  getToken: (): string | null => storage.get<string>(TOKEN_KEYS.ACCESS_TOKEN),
  setToken: (token: string): void => storage.set(TOKEN_KEYS.ACCESS_TOKEN, token),
  removeToken: (): void => storage.remove(TOKEN_KEYS.ACCESS_TOKEN),

  getRefreshToken: (): string | null => storage.get<string>(TOKEN_KEYS.REFRESH_TOKEN),
  setRefreshToken: (token: string): void => storage.set(TOKEN_KEYS.REFRESH_TOKEN, token),
  removeRefreshToken: (): void => storage.remove(TOKEN_KEYS.REFRESH_TOKEN),

  clearAll: (): void => {
    tokenStorage.removeToken();
    tokenStorage.removeRefreshToken();
  },
};
```

**Test File:**

**src/utils/__tests__/storage.test.ts:**
```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { storage, tokenStorage } from '../storage';

describe('Storage Utilities', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  describe('storage.get', () => {
    it('should retrieve and parse value', () => {
      const data = { id: 1, name: 'Test' };
      localStorage.setItem('test', JSON.stringify(data));

      expect(storage.get('test')).toEqual(data);
    });

    it('should return null for non-existent key', () => {
      expect(storage.get('nonexistent')).toBeNull();
    });

    it('should return null for invalid JSON', () => {
      localStorage.setItem('invalid', 'invalid-json-{');
      expect(storage.get('invalid')).toBeNull();
    });
  });

  describe('tokenStorage', () => {
    it('should store and retrieve access token', () => {
      const token = 'eyJhbGc...';
      tokenStorage.setToken(token);
      expect(tokenStorage.getToken()).toBe(token);
    });

    it('should store and retrieve refresh token', () => {
      const token = 'eyJhbGc...';
      tokenStorage.setRefreshToken(token);
      expect(tokenStorage.getRefreshToken()).toBe(token);
    });

    it('should clear all tokens', () => {
      tokenStorage.setToken('access');
      tokenStorage.setRefreshToken('refresh');

      tokenStorage.clearAll();

      expect(tokenStorage.getToken()).toBeNull();
      expect(tokenStorage.getRefreshToken()).toBeNull();
    });
  });
});
```

**Acceptance Criteria:**
- [ ] All tests pass (20+ tests)
- [ ] 100% code coverage
- [ ] Type-safe operations
- [ ] Error handling for storage failures
- [ ] QuotaExceededError handling
- [ ] clearAll method works
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/utils/
git commit -m "feat(storage): implement secure token storage utilities with tests

- Add type-safe localStorage wrapper
- Centralize token storage keys
- Implement clearAll for logout
- Add QuotaExceededError handling
- Comprehensive test coverage (100%)

Tests: 20 passing
Coverage: 100%"
```

**Estimated Time:** 1.5 hours

---

## Section 3: React Query Integration

**Estimated Time:** 6-8 hours
**Dependencies:** Section 2 complete

### Task 3.1: Setup QueryProvider and Lightweight AuthContext

**Status:** ⬜ Not Started
**Depends On:** Task 2.3

**Description:**
Set up React Query provider and create lightweight AuthContext that manages client state (tokens, user) while React Query handles server state (mutations, queries).

**Files to Create:**

**src/providers/QueryProvider.tsx:**
```typescript
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import type { ReactNode } from 'react';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5, // 5 minutes
      retry: 1,
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 0,
    },
  },
});

interface QueryProviderProps {
  children: ReactNode;
}

export function QueryProvider({ children }: QueryProviderProps) {
  return (
    <QueryClientProvider client={queryClient}>
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  );
}

export { queryClient };
```

**src/context/AuthContext.tsx:**
```typescript
import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { tokenStorage, storage } from '@/utils/storage';
import type { User } from '@/types/auth.types';

/**
 * AuthContext - Client State Management
 *
 * Manages tokens and user state (client state)
 * Server state (login, register, logout) handled by React Query mutations
 */
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  setTokens: (accessToken: string, refreshToken: string) => void;
  clearAuth: () => void;
  setUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUserState] = useState<User | null>(() => storage.get<User>('user'));

  // Computed from token presence
  const isAuthenticated = !!tokenStorage.getToken();

  const setTokens = (accessToken: string, refreshToken: string) => {
    tokenStorage.setToken(accessToken);
    tokenStorage.setRefreshToken(refreshToken);
  };

  const clearAuth = () => {
    tokenStorage.removeToken();
    tokenStorage.removeRefreshToken();
    storage.remove('user');
    setUserState(null);
  };

  const setUser = (newUser: User) => {
    storage.set('user', newUser);
    setUserState(newUser);
  };

  useEffect(() => {
    const storedUser = storage.get<User>('user');
    if (storedUser) {
      setUserState(storedUser);
    }
  }, []);

  const value: AuthContextType = {
    user,
    isAuthenticated,
    setTokens,
    clearAuth,
    setUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

**Files to Update:**

**src/App.tsx (or main.tsx):**
```typescript
import { BrowserRouter } from 'react-router-dom';
import { QueryProvider } from '@/providers/QueryProvider';
import { AuthProvider } from '@/context/AuthContext';

function App() {
  return (
    <QueryProvider>
      <AuthProvider>
        <BrowserRouter>
          {/* Your routes */}
        </BrowserRouter>
      </AuthProvider>
    </QueryProvider>
  );
}

export default App;
```

**Test File:**

**src/context/__tests__/AuthContext.test.tsx:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider, useAuth } from '../AuthContext';
import { tokenStorage, storage } from '@/utils/storage';
import type { ReactNode } from 'react';

vi.mock('@/utils/storage');

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
};

describe('AuthContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(tokenStorage.getToken).mockReturnValue(null);
    vi.mocked(storage.get).mockReturnValue(null);
  });

  it('should provide isAuthenticated based on token', () => {
    vi.mocked(tokenStorage.getToken).mockReturnValue('mock-token');

    const { result } = renderHook(() => useAuth(), { wrapper: createWrapper() });

    expect(result.current.isAuthenticated).toBe(true);
  });

  it('should store tokens on setTokens', () => {
    const { result } = renderHook(() => useAuth(), { wrapper: createWrapper() });

    result.current.setTokens('access-token', 'refresh-token');

    expect(tokenStorage.setToken).toHaveBeenCalledWith('access-token');
    expect(tokenStorage.setRefreshToken).toHaveBeenCalledWith('refresh-token');
  });

  it('should clear auth on clearAuth', () => {
    const { result } = renderHook(() => useAuth(), { wrapper: createWrapper() });

    result.current.clearAuth();

    expect(tokenStorage.removeToken).toHaveBeenCalled();
    expect(tokenStorage.removeRefreshToken).toHaveBeenCalled();
    expect(storage.remove).toHaveBeenCalledWith('user');
  });
});
```

**Acceptance Criteria:**
- [ ] QueryProvider wraps app with devtools
- [ ] AuthContext provides token management
- [ ] isAuthenticated computed from token
- [ ] User loaded from storage on mount
- [ ] All tests pass (6+ tests)
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/context/ src/providers/ src/App.tsx
git commit -m "feat(auth): implement AuthContext with React Query integration

- Create lightweight AuthContext for client state
- Setup QueryProvider with devtools
- Separate token management from server state
- Compute isAuthenticated from token presence
- Comprehensive context tests

Architecture: Client state (Context) + Server state (React Query)
Tests: 6 passing
Coverage: >90%

Refs: docs/AUTHENTICATION_FLOW.md"
```

**Estimated Time:** 3 hours

---

### Task 3.2: Create Auth Mutation Hooks

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Create React Query mutation hooks for login, register, and logout with built-in states and automatic side effects.

**Files to Create:**

**src/hooks/useAuthMutations.ts:**
```typescript
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authService } from '@/api/services/auth.service';
import { useAuth } from '@/context/AuthContext';
import { tokenStorage } from '@/utils/storage';
import type { LoginRequest, RegisterRequest } from '@/types/auth.types';

/**
 * Login Mutation Hook
 */
export function useLoginMutation() {
  const { setTokens, setUser } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (credentials: LoginRequest) => authService.login(credentials),

    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      navigate('/');
    },

    onError: (error) => {
      console.error('Login failed:', error);
    },
  });
}

/**
 * Register Mutation Hook
 */
export function useRegisterMutation() {
  const { setTokens, setUser } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (userData: RegisterRequest) => authService.register(userData),

    onSuccess: (data) => {
      setTokens(data.accessToken, data.refreshToken);
      setUser(data.user);
      navigate('/');
    },

    onError: (error) => {
      console.error('Registration failed:', error);
    },
  });
}

/**
 * Logout Mutation Hook
 */
export function useLogoutMutation() {
  const { clearAuth } = useAuth();
  const navigate = useNavigate();

  return useMutation({
    mutationFn: (refreshToken: string) => authService.logout(refreshToken),

    onSuccess: () => {
      clearAuth();
      navigate('/login');
    },

    onError: (error) => {
      console.error('Logout failed, clearing local state:', error);
      clearAuth();
      navigate('/login');
    },
  });
}

/**
 * All auth mutations
 */
export function useAuthMutations() {
  return {
    loginMutation: useLoginMutation(),
    registerMutation: useRegisterMutation(),
    logoutMutation: useLogoutMutation(),
  };
}
```

**src/hooks/index.ts:**
```typescript
export {
  useLoginMutation,
  useRegisterMutation,
  useLogoutMutation,
  useAuthMutations
} from './useAuthMutations';
```

**Test File:**

**src/hooks/__tests__/useAuthMutations.test.tsx:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useLoginMutation, useRegisterMutation, useLogoutMutation } from '../useAuthMutations';
import { authService } from '@/api/services/auth.service';
import { AuthProvider } from '@/context/AuthContext';
import type { ReactNode } from 'react';

vi.mock('@/api/services/auth.service');
vi.mock('@/utils/storage');
vi.mock('react-router-dom', () => ({
  useNavigate: () => vi.fn(),
}));

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { mutations: { retry: false } },
  });

  return ({ children }: { children: ReactNode }) => (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>{children}</AuthProvider>
    </QueryClientProvider>
  );
};

describe('Auth Mutation Hooks', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('useLoginMutation', () => {
    it('should login successfully', async () => {
      const mockResponse = {
        accessToken: 'token',
        refreshToken: 'refresh',
        user: { id: 1, name: 'Test', email: 'test@e.com', role: 'CUSTOMER' as const },
      };

      vi.mocked(authService.login).mockResolvedValue(mockResponse);

      const { result } = renderHook(() => useLoginMutation(), {
        wrapper: createWrapper(),
      });

      result.current.mutate({
        username: 'test@e.com',
        password: 'pass',
      });

      await waitFor(() => {
        expect(result.current.isSuccess).toBe(true);
      });
    });
  });

  describe('useLogoutMutation', () => {
    it('should clear auth even if API fails', async () => {
      vi.mocked(authService.logout).mockRejectedValue(new Error('Network'));

      const { result } = renderHook(() => useLogoutMutation(), {
        wrapper: createWrapper(),
      });

      result.current.mutate('refresh-token');

      await waitFor(() => {
        expect(result.current.isError).toBe(true);
      });
    });
  });
});
```

**Acceptance Criteria:**
- [ ] All mutations integrate with AuthContext
- [ ] Built-in loading/error/success states
- [ ] Automatic navigation on success
- [ ] Logout clears state on API failure
- [ ] All tests pass (8+ tests)
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/hooks/
git commit -m "feat(auth): implement auth mutation hooks with React Query

- Create useLoginMutation with token storage
- Create useRegisterMutation with auto-login
- Create useLogoutMutation with error handling
- Built-in loading/error/success states
- Comprehensive mutation tests

Benefits: Eliminates manual state management
Tests: 8 passing
Coverage: >90%

Refs: docs/AUTHENTICATION_FLOW.md"
```

**Estimated Time:** 3 hours

---

## Section 4: UI Components

**Estimated Time:** 8-10 hours
**Dependencies:** Section 3 complete

### Task 4.1: Login Page with Mutations

**Status:** ⬜ Not Started
**Depends On:** Task 3.2

**Description:**
Implement login page using React Hook Form, Zod validation, and login mutation with built-in states.

**Files to Update:**

**src/pages/Login.tsx:**
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { useLoginMutation } from '@/hooks/useAuthMutations';
import { loginSchema, type LoginFormData } from '@/schemas/auth.schema';

export default function Login() {
  const loginMutation = useLoginMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = (data: LoginFormData) => {
    // Backend expects 'username' field
    loginMutation.mutate({
      username: data.email,
      password: data.password,
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
        <div>
          <h2 className="text-3xl font-bold text-center text-gray-900">
            Sign in to your account
          </h2>
        </div>

        {loginMutation.isError && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
            {loginMutation.error?.message || 'Login failed'}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
          <div className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email address
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                {...register('email')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.email && (
                <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                id="password"
                type="password"
                autoComplete="current-password"
                {...register('password')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loginMutation.isPending}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loginMutation.isPending ? 'Signing in...' : 'Sign in'}
            </button>
          </div>

          <div className="text-center text-sm">
            <span className="text-gray-600">Don't have an account? </span>
            <Link to="/register" className="font-medium text-blue-600 hover:text-blue-500">
              Register here
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
```

**Test File:**

**src/pages/__tests__/Login.test.tsx:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Login from '../Login';
import { useAuth } from '@/context/AuthContext';

vi.mock('@/context/AuthContext');
vi.mock('@/hooks/useAuthMutations', () => ({
  useLoginMutation: () => ({
    mutate: vi.fn(),
    isPending: false,
    isError: false,
    error: null,
  }),
}));

describe('Login Page', () => {
  beforeEach(() => {
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      isAuthenticated: false,
      setTokens: vi.fn(),
      clearAuth: vi.fn(),
      setUser: vi.fn(),
    });
  });

  it('should render login form', () => {
    render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );

    expect(screen.getByLabelText(/email/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/password/i)).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument();
  });

  it('should show validation errors', async () => {
    render(
      <BrowserRouter>
        <Login />
      </BrowserRouter>
    );

    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/email is required/i)).toBeInTheDocument();
    });
  });
});
```

**Acceptance Criteria:**
- [ ] Form validation with Zod
- [ ] Mutation loading state shown
- [ ] Error display from mutation
- [ ] Auto-navigation on success
- [ ] Link to register page
- [ ] Tests pass (6+ tests)
- [ ] Responsive design

**Git Commit:**
```bash
git add src/pages/Login.tsx src/pages/__tests__/Login.test.tsx
git commit -m "feat(auth): implement login page with React Query mutations

- Use useLoginMutation with built-in states
- Form validation with Zod schema
- Automatic error display
- Loading state from mutation.isPending
- Auto-navigation handled by mutation

Tests: 6 passing
Coverage: >85%"
```

**Estimated Time:** 3 hours

---

### Task 4.2: Register Page with Mutations

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Implement register page with password confirmation and auto-login support.

**Files to Update:**

**src/pages/Register.tsx:**
```typescript
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { useRegisterMutation } from '@/hooks/useAuthMutations';
import { registerSchema, type RegisterFormData } from '@/schemas/auth.schema';

export default function Register() {
  const registerMutation = useRegisterMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const onSubmit = (data: RegisterFormData) => {
    registerMutation.mutate({
      name: data.username,
      email: data.email,
      password: data.password,
    });
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
        <div>
          <h2 className="text-3xl font-bold text-center text-gray-900">
            Create your account
          </h2>
        </div>

        {registerMutation.isError && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded">
            {registerMutation.error?.message || 'Registration failed'}
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="mt-8 space-y-6">
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                Username
              </label>
              <input
                id="username"
                type="text"
                {...register('username')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.username && (
                <p className="mt-1 text-sm text-red-600">{errors.username.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                Email address
              </label>
              <input
                id="email"
                type="email"
                autoComplete="email"
                {...register('email')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.email && (
                <p className="mt-1 text-sm text-red-600">{errors.email.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                Password
              </label>
              <input
                id="password"
                type="password"
                autoComplete="new-password"
                {...register('password')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.password && (
                <p className="mt-1 text-sm text-red-600">{errors.password.message}</p>
              )}
            </div>

            <div>
              <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                Confirm Password
              </label>
              <input
                id="confirmPassword"
                type="password"
                autoComplete="new-password"
                {...register('confirmPassword')}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
              {errors.confirmPassword && (
                <p className="mt-1 text-sm text-red-600">{errors.confirmPassword.message}</p>
              )}
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={registerMutation.isPending}
              className="w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {registerMutation.isPending ? 'Creating account...' : 'Register'}
            </button>
          </div>

          <div className="text-center text-sm">
            <span className="text-gray-600">Already have an account? </span>
            <Link to="/login" className="font-medium text-blue-600 hover:text-blue-500">
              Login here
            </Link>
          </div>
        </form>
      </div>
    </div>
  );
}
```

**Acceptance Criteria:**
- [ ] Form validation including password confirmation
- [ ] Backend auto-login working
- [ ] Auto-navigation on success
- [ ] Error display
- [ ] Tests pass
- [ ] Responsive design

**Git Commit:**
```bash
git add src/pages/Register.tsx src/pages/__tests__/Register.test.tsx
git commit -m "feat(auth): implement register page with React Query mutations

- Use useRegisterMutation with auto-login
- Password confirmation validation
- Backend auto-login (returns tokens)
- Mutation-based error handling

Tests: 6 passing
Coverage: >85%"
```

**Estimated Time:** 3 hours

---

### Task 4.3: Logout Functionality

**Status:** ⬜ Not Started
**Depends On:** Task 4.2

**Description:**
Add logout functionality to header/navigation with mutation.

**Files to Update:**

**src/components/layout/Header.tsx (or similar):**
```typescript
import { useLogoutMutation } from '@/hooks/useAuthMutations';
import { useAuth } from '@/context/AuthContext';
import { tokenStorage } from '@/utils/storage';

export function Header() {
  const { user, isAuthenticated } = useAuth();
  const logoutMutation = useLogoutMutation();

  const handleLogout = () => {
    const refreshToken = tokenStorage.getRefreshToken();
    if (refreshToken) {
      logoutMutation.mutate(refreshToken);
    }
  };

  return (
    <header className="bg-white shadow">
      <nav className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            {/* Logo */}
          </div>

          {isAuthenticated && user && (
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-700">
                Welcome, {user.name}
              </span>
              <button
                onClick={handleLogout}
                disabled={logoutMutation.isPending}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
              >
                {logoutMutation.isPending ? 'Logging out...' : 'Logout'}
              </button>
            </div>
          )}
        </div>
      </nav>
    </header>
  );
}
```

**Acceptance Criteria:**
- [ ] Logout button shows loading state
- [ ] Clears tokens and redirects
- [ ] Graceful error handling
- [ ] Tests pass

**Git Commit:**
```bash
git add src/components/layout/Header.tsx
git commit -m "feat(auth): implement logout with mutation

- Use useLogoutMutation for logout flow
- Graceful error handling
- Loading state during logout

Tests: 3 passing"
```

**Estimated Time:** 1.5 hours

---

## Section 5: Protection & E2E Testing

**Estimated Time:** 4-6 hours
**Dependencies:** Section 4 complete

### Task 5.1: Protected Routes Component

**Status:** ⬜ Not Started
**Depends On:** Task 4.3

**Description:**
Create protected route wrapper component with role-based access control.

**Files to Create:**

**src/components/auth/ProtectedRoute.tsx:**
```typescript
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import type { ReactNode } from 'react';

interface ProtectedRouteProps {
  children: ReactNode;
  requireRole?: string;
}

export function ProtectedRoute({ children, requireRole }: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (requireRole && user?.role !== requireRole) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
```

**Usage Example:**

**src/App.tsx:**
```typescript
import { Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';
import Home from '@/pages/Home';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import Profile from '@/pages/Profile';
import AdminDashboard from '@/pages/AdminDashboard';

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        }
      />

      <Route
        path="/admin"
        element={
          <ProtectedRoute requireRole="ADMIN">
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}
```

**Test File:**

**src/components/auth/__tests__/ProtectedRoute.test.tsx:**
```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProtectedRoute } from '../ProtectedRoute';
import { useAuth } from '@/context/AuthContext';

vi.mock('@/context/AuthContext');

describe('ProtectedRoute', () => {
  it('should redirect to login when not authenticated', () => {
    vi.mocked(useAuth).mockReturnValue({
      user: null,
      isAuthenticated: false,
      setTokens: vi.fn(),
      clearAuth: vi.fn(),
      setUser: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
  });

  it('should render protected content when authenticated', () => {
    vi.mocked(useAuth).mockReturnValue({
      user: { id: 1, name: 'Test', email: 'test@e.com', role: 'CUSTOMER' },
      isAuthenticated: true,
      setTokens: vi.fn(),
      clearAuth: vi.fn(),
      setUser: vi.fn(),
    });

    render(
      <BrowserRouter>
        <Routes>
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <div>Protected Content</div>
              </ProtectedRoute>
            }
          />
        </Routes>
      </BrowserRouter>
    );

    expect(screen.getByText('Protected Content')).toBeInTheDocument();
  });
});
```

**Acceptance Criteria:**
- [ ] Redirects to login when not authenticated
- [ ] Role-based access control works
- [ ] Preserves route with replace
- [ ] Tests pass (6+ tests)
- [ ] Type checking passes

**Git Commit:**
```bash
git add src/components/auth/
git commit -m "feat(auth): implement protected routes with role-based access

- Create ProtectedRoute wrapper
- Support role-based access control
- Redirect to login if not authenticated
- Comprehensive tests

Tests: 6 passing
Coverage: >90%"
```

**Estimated Time:** 2 hours

---

### Task 5.2: E2E Authentication Flow Tests

**Status:** ⬜ Not Started
**Depends On:** Task 5.1

**Description:**
Create end-to-end tests covering complete authentication flows.

**Test File:**

**src/__tests__/e2e/auth-flow.test.tsx:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '@/context/AuthContext';
import App from '@/App';
import { authService } from '@/api/services/auth.service';

vi.mock('@/api/services/auth.service');

const createTestWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });

  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

describe('E2E Authentication Flow', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('should complete full auth flow: register → login → logout', async () => {
    const mockUser = {
      id: 1,
      name: 'Test User',
      email: 'test@example.com',
      role: 'CUSTOMER' as const,
    };

    const mockAuthResponse = {
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      user: mockUser,
    };

    // Step 1: Register
    vi.mocked(authService.register).mockResolvedValue(mockAuthResponse);

    render(createTestWrapper());

    fireEvent.click(screen.getByText(/register/i));

    fireEvent.change(screen.getByLabelText(/username/i), {
      target: { value: 'testuser' },
    });
    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: 'test@example.com' },
    });
    fireEvent.change(screen.getByLabelText(/^password/i), {
      target: { value: 'Password123!' },
    });
    fireEvent.change(screen.getByLabelText(/confirm password/i), {
      target: { value: 'Password123!' },
    });

    fireEvent.click(screen.getByRole('button', { name: /register/i }));

    await waitFor(() => {
      expect(screen.getByText(/welcome/i)).toBeInTheDocument();
    });

    // Step 2: Logout
    vi.mocked(authService.logout).mockResolvedValue();

    fireEvent.click(screen.getByText(/logout/i));

    await waitFor(() => {
      expect(screen.getByText(/sign in/i)).toBeInTheDocument();
    });

    // Step 3: Login again
    vi.mocked(authService.login).mockResolvedValue(mockAuthResponse);

    fireEvent.change(screen.getByLabelText(/email/i), {
      target: { value: 'test@example.com' },
    });
    fireEvent.change(screen.getByLabelText(/password/i), {
      target: { value: 'Password123!' },
    });

    fireEvent.click(screen.getByRole('button', { name: /sign in/i }));

    await waitFor(() => {
      expect(screen.getByText(/welcome/i)).toBeInTheDocument();
    });

    expect(localStorage.getItem('accessToken')).toBeDefined();
  });
});
```

**Acceptance Criteria:**
- [ ] Complete user journey tested
- [ ] Token persistence verified
- [ ] All authentication states covered
- [ ] Tests pass
- [ ] >80% E2E coverage

**Git Commit:**
```bash
git add src/__tests__/e2e/
git commit -m "test(auth): add end-to-end authentication flow tests

- Complete user journey testing
- Token persistence validation
- React Query integration testing

Tests: 5 passing
Coverage: >80% E2E"
```

**Estimated Time:** 3 hours

---

## Testing & Validation

### Test Commands

```bash
# Run all auth tests
npm test src/types/__tests__/auth.types.test.ts
npm test src/api/services/__tests__/auth.service.test.ts
npm test src/context/__tests__/AuthContext.test.tsx
npm test src/hooks/__tests__/useAuthMutations.test.tsx
npm test src/pages/__tests__/Login.test.tsx
npm test src/pages/__tests__/Register.test.tsx
npm test src/__tests__/e2e/auth-flow.test.tsx

# Coverage report
npm test --coverage

# Type checking
npm run type-check

# Linting
npm run lint
```

### Coverage Requirements

| Category | Target | Critical |
|----------|--------|----------|
| Overall | >85% | >80% |
| Auth Service | 100% | 100% |
| Storage Utils | 100% | 100% |
| Mutations | >90% | >85% |
| Components | >80% | >75% |
| E2E Flows | >80% | >70% |

### Quality Gates

Before marking Phase 4 complete:

- [ ] All tests passing (100+ tests)
- [ ] >85% code coverage achieved
- [ ] Type checking passing
- [ ] Linting passing
- [ ] Backend integration tested manually
- [ ] Token refresh working
- [ ] Logout blacklisting verified
- [ ] Protected routes enforcing auth
- [ ] All Git commits follow conventions
- [ ] React Query DevTools functional
- [ ] No console errors in browser
- [ ] Responsive design verified

### Manual Integration Testing

1. **Start Backend**:
   ```bash
   cd store-backend
   ../gradlew bootRun
   # Backend runs on http://localhost:8081
   ```

2. **Start Frontend**:
   ```bash
   cd frontend
   npm run dev
   # Frontend runs on http://localhost:3000
   ```

3. **Test Flow**:
   - Register new account
   - Verify auto-login
   - Navigate to protected route
   - Logout
   - Try accessing protected route (should redirect)
   - Login with created account
   - Wait 1 hour (or modify token TTL) to test refresh
   - Verify React Query DevTools shows mutations

### Git Workflow

**Commit Convention**:
```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**: `feat`, `fix`, `test`, `refactor`, `docs`, `chore`
**Scopes**: `auth`, `api`, `types`, `storage`, `config`

**Example**:
```bash
git commit -m "feat(auth): implement login page with React Query

- Use useLoginMutation with built-in states
- Form validation with Zod schema
- Automatic error display

Tests: 6 passing
Coverage: >85%"
```

---

## Architecture Summary

```
┌─────────────────────────────────────────────┐
│        React Application (Port 3000)        │
│                                             │
│  ┌───────────────────────────────────────┐ │
│  │       QueryProvider (TanStack)        │ │
│  │                                       │ │
│  │  ┌─────────────────────────────────┐ │ │
│  │  │     AuthProvider (Context)      │ │ │
│  │  │   - Token management            │ │ │
│  │  │   - User state                  │ │ │
│  │  │                                 │ │ │
│  │  │  Components use:                │ │ │
│  │  │  - useLoginMutation()           │ │ │
│  │  │  - useRegisterMutation()        │ │ │
│  │  │  - useLogoutMutation()          │ │ │
│  │  │  - useAuth()                    │ │ │
│  │  └─────────────────────────────────┘ │ │
│  └───────────────────────────────────────┘ │
└─────────────────────────────────────────────┘
                    ↕ HTTP + JWT
┌─────────────────────────────────────────────┐
│   Backend Spring Boot (Port 8081)          │
│   - POST /api/auth/login                   │
│   - POST /api/auth/register                │
│   - POST /api/auth/refresh                 │
│   - POST /api/auth/logout                  │
│   - Redis: Token blacklist + refresh store │
└─────────────────────────────────────────────┘
```

---

## Phase 4 Completion Checklist

### Section 1: Foundation ✅
- [ ] Task 1.1: Auth types updated
- [ ] Task 1.2: API endpoints configured

### Section 2: API Layer ✅
- [ ] Task 2.1: Auth service implemented
- [ ] Task 2.2: Axios interceptor enhanced
- [ ] Task 2.3: Token storage ready

### Section 3: React Query ✅
- [ ] Task 3.1: QueryProvider setup
- [ ] Task 3.2: Mutation hooks created

### Section 4: UI Components ✅
- [ ] Task 4.1: Login page complete
- [ ] Task 4.2: Register page complete
- [ ] Task 4.3: Logout implemented

### Section 5: Protection & Testing ✅
- [ ] Task 5.1: Protected routes working
- [ ] Task 5.2: E2E tests passing

### Quality Assurance ✅
- [ ] >85% test coverage
- [ ] All tests passing
- [ ] Type checking passing
- [ ] Linting passing
- [ ] Manual testing complete
- [ ] Backend integration verified

---

## Success! 🎉

You now have a production-ready authentication system with:
- ✅ React Query for server state management
- ✅ Automatic loading/error/success states
- ✅ JWT token management with auto-refresh
- ✅ Protected routes with role-based access
- ✅ Comprehensive test coverage
- ✅ Type safety throughout
- ✅ Modern developer experience

**Estimated Total Time**: 6-8 days
**Total Test Count**: 100+ tests
**Test Coverage**: >85%

**Next Steps**: Proceed to Phase 5 (Shopping Cart & Checkout) or other features!
