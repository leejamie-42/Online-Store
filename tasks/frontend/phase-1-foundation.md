# Phase 1: Foundation Setup - Implementation Tasks
## Online Store Application - Frontend

**Phase Duration:** 3-5 days
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 2025

---

## Overview

This document contains detailed, actionable tasks for Phase 1 of the frontend implementation. Phase 1 focuses on building core application components after the initial setup.

**Prerequisites:** Phase 0 must be completed before starting Phase 1. Phase 0 covers dependency installation and basic project structure setup.

### Phase 1 Goals
- ✅ Create utility functions and base UI components
- ✅ Build API integration layer
- ✅ Implement authentication system
- ✅ Set up protected routes

---

## Table of Contents

1. [Section 1: Utility Functions & UI Components](#section-1-utility-functions--ui-components)
2. [Section 2: API Integration Layer](#section-2-api-integration-layer)
3. [Section 3: Authentication System](#section-3-authentication-system)
4. [Testing & Validation](#testing--validation)

---

## Section 1: Utility Functions & UI Components

**Estimated Time:** 3-4 hours
**Dependencies:** Phase 0 complete

### Task 1.1: Create Utility Functions

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create common utility functions.

**Files to Create:**

**src/utils/storage.ts:**
```typescript
// LocalStorage utility functions

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

export const tokenStorage = {
  getToken: (): string | null => storage.get<string>('authToken'),
  setToken: (token: string): void => storage.set('authToken', token),
  removeToken: (): void => storage.remove('authToken'),

  getRefreshToken: (): string | null => storage.get<string>('refreshToken'),
  setRefreshToken: (token: string): void => storage.set('refreshToken', token),
  removeRefreshToken: (): void => storage.remove('refreshToken'),
};
```

**src/utils/formatters.ts:**
```typescript
import { format } from 'date-fns';

export const formatCurrency = (amount: number, currency: string = 'USD'): string => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
  }).format(amount);
};

export const formatDate = (date: string | Date, formatString: string = 'PPP'): string => {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  return format(dateObj, formatString);
};

export const formatDateTime = (date: string | Date): string => {
  return formatDate(date, 'PPP p');
};

export const truncateString = (str: string, maxLength: number): string => {
  if (str.length <= maxLength) return str;
  return str.slice(0, maxLength - 3) + '...';
};
```

**src/utils/validators.ts:**
```typescript
export const isValidEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const isValidPassword = (password: string): boolean => {
  // At least 8 characters, 1 uppercase, 1 lowercase, 1 number
  return password.length >= 8 &&
         /[A-Z]/.test(password) &&
         /[a-z]/.test(password) &&
         /[0-9]/.test(password);
};

export const isValidPhone = (phone: string): boolean => {
  const phoneRegex = /^\+?[\d\s-()]+$/;
  return phoneRegex.test(phone) && phone.replace(/\D/g, '').length >= 10;
};
```

**src/utils/constants.ts:**
```typescript
export const API_TIMEOUT = 15000; // 15 seconds

export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  REFRESH_TOKEN: 'refreshToken',
  USER: 'user',
  CART: 'cart',
} as const;

export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  PRODUCTS: '/products',
  PRODUCT_DETAIL: '/products/:id',
  CART: '/cart',
  CHECKOUT: '/checkout',
  ORDERS: '/orders',
  ORDER_DETAIL: '/orders/:orderId',
  PROFILE: '/profile',
} as const;
```

**Acceptance Criteria:**
- [ ] All utility files created
- [ ] Functions properly typed
- [ ] Functions tested manually
- [ ] No TypeScript errors

**Estimated Time:** 40 minutes

---

### Task 1.2: Create Base UI Components - Button

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create the foundational Button component.

**Files to Create:**

**src/components/ui/Button/Button.tsx:**
```typescript
import React from 'react';
import { Loader2 } from 'react-icons/lu';

export interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  fullWidth?: boolean;
  children: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  fullWidth = false,
  disabled,
  className = '',
  children,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed';

  const variantStyles = {
    primary: 'bg-primary-600 text-white hover:bg-primary-700 focus:ring-primary-500',
    secondary: 'bg-gray-200 text-gray-900 hover:bg-gray-300 focus:ring-gray-500',
    outline: 'border-2 border-gray-300 bg-transparent hover:bg-gray-50 focus:ring-gray-500',
    ghost: 'bg-transparent hover:bg-gray-100 focus:ring-gray-500',
    danger: 'bg-red-600 text-white hover:bg-red-700 focus:ring-red-500',
  };

  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-base',
    lg: 'px-6 py-3 text-lg',
  };

  const widthStyle = fullWidth ? 'w-full' : '';

  return (
    <button
      className={`${baseStyles} ${variantStyles[variant]} ${sizeStyles[size]} ${widthStyle} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
      {children}
    </button>
  );
};
```

**src/components/ui/Button/index.ts:**
```typescript
export { Button } from './Button';
export type { ButtonProps } from './Button';
```

**Acceptance Criteria:**
- [ ] Button component created with all variants
- [ ] Loading state works
- [ ] Styles applied correctly
- [ ] TypeScript types correct
- [ ] Can be imported and used

**Estimated Time:** 30 minutes

---

### Task 1.3: Create Base UI Components - Input

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create the foundational Input component.

**Files to Create:**

**src/components/ui/Input/Input.tsx:**
```typescript
import React, { forwardRef } from 'react';

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  fullWidth?: boolean;
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, fullWidth = false, className = '', ...props }, ref) => {
    const hasError = Boolean(error);

    return (
      <div className={`${fullWidth ? 'w-full' : ''}`}>
        {label && (
          <label className="block text-sm font-medium text-gray-700 mb-1">
            {label}
            {props.required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}

        <input
          ref={ref}
          className={`
            w-full px-3 py-2
            border ${hasError ? 'border-red-500' : 'border-gray-300'}
            rounded-lg
            focus:outline-none focus:ring-2
            ${hasError ? 'focus:ring-red-500' : 'focus:ring-primary-500'}
            focus:border-transparent
            disabled:bg-gray-100 disabled:cursor-not-allowed
            ${className}
          `}
          {...props}
        />

        {error && (
          <p className="mt-1 text-sm text-red-600">{error}</p>
        )}

        {helperText && !error && (
          <p className="mt-1 text-sm text-gray-500">{helperText}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
```

**src/components/ui/Input/index.ts:**
```typescript
export { Input } from './Input';
export type { InputProps } from './Input';
```

**Acceptance Criteria:**
- [ ] Input component created
- [ ] Error states work
- [ ] Label and helper text display correctly
- [ ] Properly typed with forwardRef

**Estimated Time:** 25 minutes

---

### Task 1.4: Create Base UI Components - Card

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create Card component for content containers.

**Files to Create:**

**src/components/ui/Card/Card.tsx:**
```typescript
import React from 'react';

export interface CardProps {
  children: React.ReactNode;
  className?: string;
  padding?: 'none' | 'sm' | 'md' | 'lg';
  hover?: boolean;
}

export const Card: React.FC<CardProps> = ({
  children,
  className = '',
  padding = 'md',
  hover = false,
}) => {
  const paddingStyles = {
    none: '',
    sm: 'p-3',
    md: 'p-4',
    lg: 'p-6',
  };

  const hoverStyle = hover ? 'hover:shadow-md transition-shadow duration-200' : '';

  return (
    <div className={`bg-white rounded-lg shadow-sm border border-gray-200 ${paddingStyles[padding]} ${hoverStyle} ${className}`}>
      {children}
    </div>
  );
};

export const CardHeader: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = ''
}) => (
  <div className={`mb-4 ${className}`}>{children}</div>
);

export const CardContent: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = ''
}) => (
  <div className={className}>{children}</div>
);

export const CardFooter: React.FC<{ children: React.ReactNode; className?: string }> = ({
  children,
  className = ''
}) => (
  <div className={`mt-4 ${className}`}>{children}</div>
);
```

**src/components/ui/Card/index.ts:**
```typescript
export { Card, CardHeader, CardContent, CardFooter } from './Card';
export type { CardProps } from './Card';
```

**Acceptance Criteria:**
- [ ] Card components created
- [ ] Composition pattern works
- [ ] Hover effects optional
- [ ] Proper styling applied

**Estimated Time:** 20 minutes

---

### Task 1.5: Create Base UI Components - Spinner

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create loading spinner component.

**Files to Create:**

**src/components/ui/Spinner/Spinner.tsx:**
```typescript
import React from 'react';

export interface SpinnerProps {
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

export const Spinner: React.FC<SpinnerProps> = ({ size = 'md', className = '' }) => {
  const sizeStyles = {
    sm: 'h-4 w-4',
    md: 'h-8 w-8',
    lg: 'h-12 w-12',
  };

  return (
    <div className={`animate-spin rounded-full border-4 border-gray-200 border-t-primary-600 ${sizeStyles[size]} ${className}`} />
  );
};

export const LoadingScreen: React.FC<{ message?: string }> = ({ message = 'Loading...' }) => (
  <div className="flex flex-col items-center justify-center min-h-screen">
    <Spinner size="lg" />
    <p className="mt-4 text-gray-600">{message}</p>
  </div>
);
```

**src/components/ui/Spinner/index.ts:**
```typescript
export { Spinner, LoadingScreen } from './Spinner';
export type { SpinnerProps } from './Spinner';
```

**Acceptance Criteria:**
- [ ] Spinner component created
- [ ] Animation works smoothly
- [ ] Different sizes work
- [ ] LoadingScreen component works

**Estimated Time:** 15 minutes

---

### Task 1.6: Create Base UI Components - Modal

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create modal/dialog component.

**Files to Create:**

**src/components/ui/Modal/Modal.tsx:**
```typescript
import React, { useEffect } from 'react';
import { X } from 'react-icons/lu';
import { Button } from '../Button';

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  showCloseButton?: boolean;
}

export const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  children,
  size = 'md',
  showCloseButton = true,
}) => {
  useEffect(() => {
    if (isOpen) {
      document.body.style.overflow = 'hidden';
    } else {
      document.body.style.overflow = 'unset';
    }

    return () => {
      document.body.style.overflow = 'unset';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const sizeStyles = {
    sm: 'max-w-sm',
    md: 'max-w-md',
    lg: 'max-w-lg',
    xl: 'max-w-xl',
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={onClose}
      />

      {/* Modal Container */}
      <div className="flex min-h-full items-center justify-center p-4">
        {/* Modal Content */}
        <div className={`relative bg-white rounded-xl shadow-xl ${sizeStyles[size]} w-full p-6`}>
          {/* Header */}
          {(title || showCloseButton) && (
            <div className="flex items-center justify-between mb-4">
              {title && (
                <h2 className="text-2xl font-semibold text-gray-900">{title}</h2>
              )}
              {showCloseButton && (
                <button
                  onClick={onClose}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
                  aria-label="Close modal"
                >
                  <X className="h-6 w-6" />
                </button>
              )}
            </div>
          )}

          {/* Content */}
          <div>{children}</div>
        </div>
      </div>
    </div>
  );
};
```

**src/components/ui/Modal/index.ts:**
```typescript
export { Modal } from './Modal';
export type { ModalProps } from './Modal';
```

**Acceptance Criteria:**
- [ ] Modal component created
- [ ] Opens and closes correctly
- [ ] Backdrop click closes modal
- [ ] Body scroll locked when open
- [ ] ESC key closes modal (optional enhancement)

**Estimated Time:** 30 minutes

---

### Task 1.7: Create Configuration Files

**Status:** ⬜ Not Started
**Depends On:** Phase 0 complete

**Description:**
Create configuration files for API and routes.

**Files to Create:**

**src/config/api.config.ts:**
```typescript
export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 15000,
} as const;

export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  LOGOUT: '/auth/logout',
  REFRESH_TOKEN: '/auth/refresh',

  // Products
  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  // Cart
  CART: '/cart',
  CART_ITEMS: '/cart/items',
  CART_ITEM: (itemId: string) => `/cart/items/${itemId}`,

  // Orders
  ORDERS: '/orders',
  ORDER_DETAIL: (orderId: string) => `/orders/${orderId}`,
  CANCEL_ORDER: (orderId: string) => `/orders/${orderId}/cancel`,

  // User
  USER_PROFILE: '/users/me',
  USER_ADDRESSES: '/users/me/addresses',
  USER_ADDRESS: (addressId: string) => `/users/me/addresses/${addressId}`,
} as const;
```

**src/config/routes.ts:**
```typescript
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  FORGOT_PASSWORD: '/forgot-password',

  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  CART: '/cart',
  CHECKOUT: '/checkout',
  ORDER_CONFIRMATION: (orderId: string) => `/order/confirmation/${orderId}`,

  ORDERS: '/orders',
  ORDER_DETAIL: (orderId: string) => `/orders/${orderId}`,

  PROFILE: '/profile',
  SETTINGS: '/settings',

  NOT_FOUND: '*',
} as const;
```

**Acceptance Criteria:**
- [ ] Config files created
- [ ] All endpoints defined
- [ ] All routes defined
- [ ] Proper TypeScript typing

**Estimated Time:** 20 minutes

---

## Section 2: API Integration Layer

**Estimated Time:** 2-3 hours
**Dependencies:** Section 1 complete

### Task 2.1: Create Axios Client Instance

**Status:** ⬜ Not Started
**Depends On:** Task 1.7

**Description:**
Create configured Axios instance with interceptors.

**File to Create:**

**src/lib/axios.ts:**
```typescript
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { API_CONFIG } from '@/config/api.config';
import { tokenStorage } from '@/utils/storage';

// Create axios instance
export const apiClient = axios.create({
  baseURL: API_CONFIG.baseURL,
  timeout: API_CONFIG.timeout,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor - Add auth token
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = tokenStorage.getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  }
);

// Response interceptor - Handle errors and token refresh
apiClient.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Handle 401 - Unauthorized (token expired)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Try to refresh token
        const refreshToken = tokenStorage.getRefreshToken();
        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        const response = await axios.post(
          `${API_CONFIG.baseURL}/auth/refresh`,
          { refreshToken }
        );

        const { token } = response.data;
        tokenStorage.setToken(token);

        // Retry original request with new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, clear tokens and redirect to login
        tokenStorage.removeToken();
        tokenStorage.removeRefreshToken();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);
```

**Acceptance Criteria:**
- [ ] Axios instance created with config
- [ ] Request interceptor adds auth token
- [ ] Response interceptor handles errors
- [ ] Token refresh mechanism works
- [ ] Redirects to login on auth failure

**Estimated Time:** 45 minutes

---

### Task 2.2: Create Auth Service

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Create service layer for authentication API calls.

**File to Create:**

**src/api/services/auth.service.ts:**
```typescript
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { User, LoginCredentials, RegisterData } from '@/types/user.types';

export interface LoginResponse {
  token: string;
  refreshToken: string;
  user: User;
}

export interface RegisterResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  createdAt: string;
}

export const authService = {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    const response = await apiClient.post<LoginResponse>(
      API_ENDPOINTS.LOGIN,
      credentials
    );
    return response.data;
  },

  async register(userData: RegisterData): Promise<RegisterResponse> {
    const response = await apiClient.post<RegisterResponse>(
      API_ENDPOINTS.REGISTER,
      userData
    );
    return response.data;
  },

  async logout(): Promise<void> {
    await apiClient.post(API_ENDPOINTS.LOGOUT);
  },

  async refreshToken(refreshToken: string): Promise<{ token: string }> {
    const response = await apiClient.post<{ token: string }>(
      API_ENDPOINTS.REFRESH_TOKEN,
      { refreshToken }
    );
    return response.data;
  },
};
```

**Acceptance Criteria:**
- [ ] Auth service created
- [ ] All auth methods implemented
- [ ] Proper TypeScript typing
- [ ] Error handling in place

**Estimated Time:** 30 minutes

---

### Task 2.3: Create User Service

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Create service layer for user-related API calls.

**File to Create:**

**src/api/services/user.service.ts:**
```typescript
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { User, Address } from '@/types/user.types';

export const userService = {
  async getProfile(): Promise<User> {
    const response = await apiClient.get<User>(API_ENDPOINTS.USER_PROFILE);
    return response.data;
  },

  async updateProfile(data: Partial<User>): Promise<User> {
    const response = await apiClient.put<User>(API_ENDPOINTS.USER_PROFILE, data);
    return response.data;
  },

  async getAddresses(): Promise<Address[]> {
    const response = await apiClient.get<Address[]>(API_ENDPOINTS.USER_ADDRESSES);
    return response.data;
  },

  async addAddress(address: Address): Promise<{ addressId: string; message: string }> {
    const response = await apiClient.post(API_ENDPOINTS.USER_ADDRESSES, address);
    return response.data;
  },

  async updateAddress(addressId: string, address: Address): Promise<void> {
    await apiClient.put(API_ENDPOINTS.USER_ADDRESS(addressId), address);
  },

  async deleteAddress(addressId: string): Promise<void> {
    await apiClient.delete(API_ENDPOINTS.USER_ADDRESS(addressId));
  },
};
```

**Acceptance Criteria:**
- [ ] User service created
- [ ] All user methods implemented
- [ ] Proper TypeScript typing

**Estimated Time:** 25 minutes

---

### Task 2.4: Create Error Handling Utility

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Create utility for handling API errors consistently.

**File to Create:**

**src/utils/error-handler.ts:**
```typescript
import { AxiosError } from 'axios';
import type { ApiError } from '@/types/api.types';

export const handleApiError = (error: unknown): string => {
  if (error instanceof AxiosError) {
    const apiError = error.response?.data as ApiError | undefined;

    if (apiError?.message) {
      return apiError.message;
    }

    switch (error.response?.status) {
      case 400:
        return 'Invalid request. Please check your input.';
      case 401:
        return 'You are not authorized. Please log in.';
      case 403:
        return 'You do not have permission to perform this action.';
      case 404:
        return 'The requested resource was not found.';
      case 500:
        return 'Server error. Please try again later.';
      default:
        return 'An unexpected error occurred. Please try again.';
    }
  }

  if (error instanceof Error) {
    return error.message;
  }

  return 'An unknown error occurred.';
};

export const getApiErrorDetails = (error: unknown): Record<string, string> | undefined => {
  if (error instanceof AxiosError) {
    const apiError = error.response?.data as ApiError | undefined;
    return apiError?.details;
  }
  return undefined;
};
```

**Acceptance Criteria:**
- [ ] Error handler created
- [ ] Handles different error types
- [ ] Returns user-friendly messages
- [ ] Extracts error details

**Estimated Time:** 20 minutes

---

## Section 3: Authentication System

**Estimated Time:** 3-4 hours
**Dependencies:** Section 2 complete

### Task 3.1: Create Auth Context

**Status:** ⬜ Not Started
**Depends On:** Task 2.2

**Description:**
Create React Context for authentication state management.

**File to Create:**

**src/context/AuthContext.tsx:**
```typescript
import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService } from '@/api/services/auth.service';
import { userService } from '@/api/services/user.service';
import { tokenStorage, storage } from '@/utils/storage';
import { handleApiError } from '@/utils/error-handler';
import type { User, LoginCredentials, RegisterData } from '@/types/user.types';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  register: (userData: RegisterData) => Promise<void>;
  logout: () => Promise<void>;
  updateUser: (user: User) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const isAuthenticated = Boolean(user);

  // Load user from storage on mount
  useEffect(() => {
    const loadUser = async () => {
      const token = tokenStorage.getToken();
      const storedUser = storage.get<User>('user');

      if (token && storedUser) {
        setUser(storedUser);
        // Optionally: Fetch fresh user data from API
        try {
          const freshUser = await userService.getProfile();
          setUser(freshUser);
          storage.set('user', freshUser);
        } catch (error) {
          console.error('Failed to fetch user profile:', error);
        }
      }

      setIsLoading(false);
    };

    loadUser();
  }, []);

  const login = async (credentials: LoginCredentials) => {
    try {
      const response = await authService.login(credentials);

      tokenStorage.setToken(response.token);
      tokenStorage.setRefreshToken(response.refreshToken);
      storage.set('user', response.user);
      setUser(response.user);
    } catch (error) {
      throw new Error(handleApiError(error));
    }
  };

  const register = async (userData: RegisterData) => {
    try {
      await authService.register(userData);
      // After successful registration, automatically log in
      await login({ email: userData.email, password: userData.password });
    } catch (error) {
      throw new Error(handleApiError(error));
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      tokenStorage.removeToken();
      tokenStorage.removeRefreshToken();
      storage.remove('user');
      setUser(null);
    }
  };

  const updateUser = (updatedUser: User) => {
    setUser(updatedUser);
    storage.set('user', updatedUser);
  };

  const value: AuthContextType = {
    user,
    isAuthenticated,
    isLoading,
    login,
    register,
    logout,
    updateUser,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

**Acceptance Criteria:**
- [ ] AuthContext created
- [ ] AuthProvider wraps app
- [ ] useAuth hook works
- [ ] Login/logout functionality works
- [ ] User state persists across refreshes
- [ ] Loading state handled

**Estimated Time:** 60 minutes

---

### Task 3.2: Create Protected Route Component

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Create component to protect routes that require authentication.

**File to Create:**

**src/components/common/ProtectedRoute/ProtectedRoute.tsx:**
```typescript
import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { LoadingScreen } from '@/components/ui/Spinner';
import { ROUTES } from '@/config/routes';

interface ProtectedRouteProps {
  children: React.ReactNode;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();
  const location = useLocation();

  if (isLoading) {
    return <LoadingScreen message="Checking authentication..." />;
  }

  if (!isAuthenticated) {
    // Redirect to login and save the attempted location
    return <Navigate to={ROUTES.LOGIN} state={{ from: location }} replace />;
  }

  return <>{children}</>;
};
```

**src/components/common/ProtectedRoute/index.ts:**
```typescript
export { ProtectedRoute } from './ProtectedRoute';
```

**Acceptance Criteria:**
- [ ] ProtectedRoute component created
- [ ] Redirects to login if not authenticated
- [ ] Preserves intended route for redirect after login
- [ ] Shows loading state while checking auth

**Estimated Time:** 20 minutes

---

### Task 3.3: Update App.tsx with AuthProvider

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Wrap application with AuthProvider.

**File to Modify:**

**src/App.tsx:**
```typescript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import './App.css';

// Import pages (you'll create these in Phase 2)
import Home from '@/pages/Home';
import About from '@/pages/About';
import Contact from '@/pages/Contact';
import NotFound from '@/pages/NotFound';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="App">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
```

**Acceptance Criteria:**
- [ ] App wrapped with AuthProvider
- [ ] Auth context available throughout app
- [ ] No errors in console

**Estimated Time:** 10 minutes

---

### Task 3.4: Create useAuth Hook Test

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Create a simple test component to verify auth functionality.

**File to Create (Temporary):**

**src/components/common/AuthTest.tsx:**
```typescript
import React from 'react';
import { useAuth } from '@/context/AuthContext';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';

export const AuthTest: React.FC = () => {
  const { user, isAuthenticated, isLoading, login, logout } = useAuth();

  const handleTestLogin = async () => {
    try {
      await login({ email: 'test@example.com', password: 'password123' });
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  if (isLoading) {
    return <div>Loading...</div>;
  }

  return (
    <Card className="max-w-md mx-auto mt-8">
      <h2 className="text-2xl font-bold mb-4">Auth Test Component</h2>

      <div className="space-y-4">
        <div>
          <strong>Authenticated:</strong> {isAuthenticated ? 'Yes' : 'No'}
        </div>

        {user && (
          <div>
            <strong>User:</strong> {user.fullName} ({user.email})
          </div>
        )}

        <div className="flex gap-2">
          <Button onClick={handleTestLogin} disabled={isAuthenticated}>
            Test Login
          </Button>
          <Button onClick={logout} disabled={!isAuthenticated} variant="secondary">
            Logout
          </Button>
        </div>
      </div>
    </Card>
  );
};
```

**Usage:** Add to Home page temporarily to test auth

**Acceptance Criteria:**
- [ ] Test component created
- [ ] Can test login/logout
- [ ] Auth state updates correctly
- [ ] Remove after testing

**Estimated Time:** 20 minutes

---

## Testing & Validation

### Task 4.1: Manual Testing Checklist

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Manually verify all Phase 1 functionality.

**Testing Steps:**

1. **Dependencies:**
   - [ ] Run `npm install` - no errors
   - [ ] Run `npm run dev` - dev server starts
   - [ ] Run `npm run type-check` - no TypeScript errors
   - [ ] Run `npm run lint` - no critical errors

2. **UI Components:**
   - [ ] Button renders with all variants
   - [ ] Input accepts text and shows errors
   - [ ] Card displays content properly
   - [ ] Spinner animates smoothly
   - [ ] Modal opens and closes

3. **API Integration:**
   - [ ] Axios client configured
   - [ ] Request interceptor adds token
   - [ ] Response interceptor catches errors

4. **Authentication:**
   - [ ] Can test login (even if backend not ready, check network request)
   - [ ] Token stored in localStorage
   - [ ] User state updates
   - [ ] Logout clears tokens
   - [ ] ProtectedRoute redirects when not authenticated

**Estimated Time:** 45 minutes

---

### Task 4.2: Create Phase 1 Completion Report

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Document Phase 1 completion and any issues.

**Create file:** `tasks/frontend/phase-1-completion-report.md`

**Contents:**
- Date completed
- All tasks completed (checkboxes)
- Any deviations from plan
- Issues encountered and solutions
- Recommendations for Phase 2
- Screenshots (optional)

**Acceptance Criteria:**
- [ ] Report created
- [ ] All tasks verified
- [ ] Ready to begin Phase 2

**Estimated Time:** 20 minutes

---

## Summary

### Total Tasks: 17
### Estimated Total Time: 8-11 hours
### Priority: HIGH
### Prerequisites: Phase 0 must be completed first

### Deliverables Checklist:

**Utility Functions & Components:**
- [ ] Utility functions created (storage, formatters, validators, constants)
- [ ] Configuration files created (API config, routes)

**UI Components:**
- [ ] Button component
- [ ] Input component
- [ ] Card component
- [ ] Spinner component
- [ ] Modal component

**API Layer:**
- [ ] Axios client with interceptors
- [ ] Auth service
- [ ] User service
- [ ] Error handling utilities

**Authentication:**
- [ ] AuthContext and Provider
- [ ] useAuth hook
- [ ] ProtectedRoute component
- [ ] Login/logout functionality

---

## Next Steps

After completing Phase 1:
1. Review this document and mark all tasks complete
2. Create Phase 1 completion report
3. Proceed to `phase-2-layout-navigation.md`
4. Begin implementing layout components

---

**Document Created:** October 2025
**Phase Status:** Not Started
**Target Completion:** 3-5 days from start
