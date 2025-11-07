# API Integration Guide
## Online Store Application

**Version:** 1.0
**Last Updated:** October 2025
**Base URL:** `http://localhost:8080/api` (Development)

---

## Table of Contents

1. [Overview](#overview)
2. [Authentication](#authentication)
3. [API Client Setup](#api-client-setup)
4. [Endpoints](#endpoints)
5. [Request/Response Formats](#requestresponse-formats)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)

---

## Overview

This document describes the integration between the React frontend and the Spring Boot backend API. All endpoints follow RESTful conventions and use JSON for data exchange.

### API Principles

- **RESTful Design** - Resource-based URLs with standard HTTP methods
- **JSON Format** - All requests and responses use JSON
- **JWT Authentication** - Secure token-based authentication
- **Error Standards** - Consistent error response format
- **Pagination** - Large datasets use cursor or offset pagination

---

## Authentication

### Authentication Flow

1. User submits login credentials
2. Backend validates and returns JWT token
3. Frontend stores token in localStorage
4. All subsequent requests include token in Authorization header
5. Token expires after 24 hours (configurable)
6. Refresh token mechanism available

### Token Storage

```typescript
// utils/storage.ts
export const tokenStorage = {
  getToken: (): string | null => {
    return localStorage.getItem('authToken');
  },

  setToken: (token: string): void => {
    localStorage.setItem('authToken', token);
  },

  removeToken: (): void => {
    localStorage.removeItem('authToken');
  },

  getRefreshToken: (): string | null => {
    return localStorage.getItem('refreshToken');
  },

  setRefreshToken: (token: string): void => {
    localStorage.setItem('refreshToken', token);
  },
};
```

---

## API Client Setup

### Axios Configuration

```typescript
// lib/axios.ts
import axios, { AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import { tokenStorage } from '@/utils/storage';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Create axios instance
export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 seconds
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

// Response interceptor - Handle errors
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
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        });

        const { token } = response.data;
        tokenStorage.setToken(token);

        // Retry original request with new token
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${token}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh failed, redirect to login
        tokenStorage.removeToken();
        tokenStorage.removeRefreshToken();
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }

    // Handle other errors
    return Promise.reject(error);
  }
);
```

---

## Endpoints

### Authentication Endpoints

#### POST /auth/register

Register a new user account.

**Request:**
```typescript
interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

// Example
await apiClient.post('/auth/register', {
  username: 'johndoe',
  email: 'john@example.com',
  password: 'SecurePass123!',
  fullName: 'John Doe',
});
```

**Response:**
```typescript
interface RegisterResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  createdAt: string;
}

// Example
{
  "id": "user-123",
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "createdAt": "2025-10-15T10:30:00Z"
}
```

---

#### POST /auth/login

Authenticate user and receive JWT token.

**Request:**
```typescript
interface LoginRequest {
  email: string;
  password: string;
}

// Example
await apiClient.post('/auth/login', {
  email: 'john@example.com',
  password: 'SecurePass123!',
});
```

**Response:**
```typescript
interface LoginResponse {
  token: string;
  refreshToken: string;
  user: {
    id: string;
    username: string;
    email: string;
    fullName: string;
  };
}

// Example
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "user-123",
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe"
  }
}
```

---

#### POST /auth/refresh

Refresh expired JWT token.

**Request:**
```typescript
interface RefreshTokenRequest {
  refreshToken: string;
}
```

**Response:**
```typescript
interface RefreshTokenResponse {
  token: string;
}
```

---

#### POST /auth/logout

Invalidate current session.

**Request:** No body required (token in header)

**Response:**
```typescript
{
  "message": "Logout successful"
}
```

---

### Product Endpoints

#### GET /products

Get list of products with optional filtering and pagination.

**Query Parameters:**
```typescript
interface ProductQueryParams {
  page?: number;        // Default: 0
  size?: number;        // Default: 20
  sort?: string;        // e.g., "price,asc" or "name,desc"
  category?: string;    // Filter by category
  minPrice?: number;    // Minimum price filter
  maxPrice?: number;    // Maximum price filter
  search?: string;      // Search by name or description
  inStock?: boolean;    // Only show in-stock items
}

// Example
await apiClient.get('/products', {
  params: {
    page: 0,
    size: 20,
    sort: 'price,asc',
    category: 'electronics',
    minPrice: 10,
    maxPrice: 500,
    search: 'laptop',
    inStock: true,
  },
});
```

**Response:**
```typescript
interface ProductListResponse {
  content: Product[];
  page: {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
  };
}

interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  category: string;
  imageUrl: string;
  stockQuantity: number;
  inStock: boolean;
  createdAt: string;
  updatedAt: string;
}

// Example
{
  "content": [
    {
      "id": "prod-123",
      "name": "Laptop Pro 15",
      "description": "High-performance laptop for professionals",
      "price": 1299.99,
      "category": "electronics",
      "imageUrl": "https://example.com/images/laptop.jpg",
      "stockQuantity": 25,
      "inStock": true,
      "createdAt": "2025-10-01T10:00:00Z",
      "updatedAt": "2025-10-15T08:30:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

#### GET /products/:id

Get detailed information about a specific product.

**Path Parameters:**
- `id` - Product ID

**Response:**
```typescript
interface ProductDetailResponse extends Product {
  specifications: Record<string, string>;
  reviews: ProductReview[];
  relatedProducts: Product[];
}

interface ProductReview {
  id: string;
  userId: string;
  userName: string;
  rating: number;
  comment: string;
  createdAt: string;
}

// Example
{
  "id": "prod-123",
  "name": "Laptop Pro 15",
  "description": "High-performance laptop for professionals",
  "price": 1299.99,
  "category": "electronics",
  "imageUrl": "https://example.com/images/laptop.jpg",
  "stockQuantity": 25,
  "inStock": true,
  "specifications": {
    "processor": "Intel i7",
    "ram": "16GB",
    "storage": "512GB SSD",
    "display": "15.6\" Retina"
  },
  "reviews": [
    {
      "id": "review-1",
      "userId": "user-456",
      "userName": "Jane Smith",
      "rating": 5,
      "comment": "Excellent laptop!",
      "createdAt": "2025-10-10T14:30:00Z"
    }
  ],
  "relatedProducts": [...]
}
```

---

### Cart Endpoints

#### GET /cart

Get current user's shopping cart.

**Authentication:** Required

**Response:**
```typescript
interface CartResponse {
  items: CartItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
}

interface CartItem {
  id: string;
  productId: string;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  subtotal: number;
}

// Example
{
  "items": [
    {
      "id": "cart-item-1",
      "productId": "prod-123",
      "productName": "Laptop Pro 15",
      "productImage": "https://example.com/images/laptop.jpg",
      "price": 1299.99,
      "quantity": 1,
      "subtotal": 1299.99
    }
  ],
  "subtotal": 1299.99,
  "tax": 130.00,
  "shipping": 15.00,
  "total": 1444.99
}
```

---

#### POST /cart/items

Add item to cart.

**Authentication:** Required

**Request:**
```typescript
interface AddToCartRequest {
  productId: string;
  quantity: number;
}

// Example
await apiClient.post('/cart/items', {
  productId: 'prod-123',
  quantity: 1,
});
```

**Response:**
```typescript
// Returns updated cart
interface AddToCartResponse extends CartResponse {}
```

---

#### PUT /cart/items/:itemId

Update cart item quantity.

**Authentication:** Required

**Path Parameters:**
- `itemId` - Cart item ID

**Request:**
```typescript
interface UpdateCartItemRequest {
  quantity: number;
}

// Example
await apiClient.put('/cart/items/cart-item-1', {
  quantity: 3,
});
```

**Response:**
```typescript
// Returns updated cart
interface UpdateCartItemResponse extends CartResponse {}
```

---

#### DELETE /cart/items/:itemId

Remove item from cart.

**Authentication:** Required

**Path Parameters:**
- `itemId` - Cart item ID

**Response:**
```typescript
// Returns updated cart
interface RemoveCartItemResponse extends CartResponse {}
```

---

#### DELETE /cart

Clear entire cart.

**Authentication:** Required

**Response:**
```typescript
{
  "message": "Cart cleared successfully"
}
```

---

### Order Endpoints

#### POST /orders

Create a new order from cart.

**Authentication:** Required

**Request:**
```typescript
interface CreateOrderRequest {
  shippingAddress: Address;
  paymentMethod: string;
  notes?: string;
}

interface Address {
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone: string;
}

// Example
await apiClient.post('/orders', {
  shippingAddress: {
    fullName: "John Doe",
    addressLine1: "123 Main St",
    city: "Sydney",
    state: "NSW",
    postalCode: "2000",
    country: "Australia",
    phone: "+61 400 000 000"
  },
  paymentMethod: "credit_card",
  notes: "Please deliver after 5 PM"
});
```

**Response:**
```typescript
interface CreateOrderResponse {
  orderId: string;
  orderNumber: string;
  status: OrderStatus;
  total: number;
  createdAt: string;
}

type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED';

// Example
{
  "orderId": "order-789",
  "orderNumber": "ORD-2025-001234",
  "status": "PENDING",
  "total": 1444.99,
  "createdAt": "2025-10-15T12:00:00Z"
}
```

---

#### GET /orders

Get user's order history.

**Authentication:** Required

**Query Parameters:**
```typescript
interface OrderQueryParams {
  page?: number;
  size?: number;
  status?: OrderStatus;
}
```

**Response:**
```typescript
interface OrderListResponse {
  content: OrderSummary[];
  page: PageInfo;
}

interface OrderSummary {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  total: number;
  itemCount: number;
  createdAt: string;
  updatedAt: string;
}

// Example
{
  "content": [
    {
      "id": "order-789",
      "orderNumber": "ORD-2025-001234",
      "status": "DELIVERED",
      "total": 1444.99,
      "itemCount": 2,
      "createdAt": "2025-10-15T12:00:00Z",
      "updatedAt": "2025-10-18T16:30:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 15,
    "totalPages": 1
  }
}
```

---

#### GET /orders/:orderId

Get detailed order information.

**Authentication:** Required

**Path Parameters:**
- `orderId` - Order ID

**Response:**
```typescript
interface OrderDetailResponse {
  id: string;
  orderNumber: string;
  status: OrderStatus;
  items: OrderItem[];
  shippingAddress: Address;
  subtotal: number;
  tax: number;
  shipping: number;
  total: number;
  paymentMethod: string;
  notes?: string;
  statusHistory: OrderStatusUpdate[];
  trackingNumber?: string;
  estimatedDelivery?: string;
  createdAt: string;
  updatedAt: string;
}

interface OrderItem {
  id: string;
  productId: string;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  subtotal: number;
}

interface OrderStatusUpdate {
  status: OrderStatus;
  timestamp: string;
  notes?: string;
}

// Example
{
  "id": "order-789",
  "orderNumber": "ORD-2025-001234",
  "status": "DELIVERED",
  "items": [
    {
      "id": "order-item-1",
      "productId": "prod-123",
      "productName": "Laptop Pro 15",
      "productImage": "https://example.com/images/laptop.jpg",
      "price": 1299.99,
      "quantity": 1,
      "subtotal": 1299.99
    }
  ],
  "shippingAddress": {
    "fullName": "John Doe",
    "addressLine1": "123 Main St",
    "city": "Sydney",
    "state": "NSW",
    "postalCode": "2000",
    "country": "Australia",
    "phone": "+61 400 000 000"
  },
  "subtotal": 1299.99,
  "tax": 130.00,
  "shipping": 15.00,
  "total": 1444.99,
  "paymentMethod": "credit_card",
  "statusHistory": [
    {
      "status": "PENDING",
      "timestamp": "2025-10-15T12:00:00Z"
    },
    {
      "status": "CONFIRMED",
      "timestamp": "2025-10-15T12:15:00Z"
    },
    {
      "status": "SHIPPED",
      "timestamp": "2025-10-16T09:00:00Z",
      "notes": "Package shipped via Express Delivery"
    },
    {
      "status": "DELIVERED",
      "timestamp": "2025-10-18T16:30:00Z"
    }
  ],
  "trackingNumber": "TRACK-123456789",
  "estimatedDelivery": "2025-10-18T18:00:00Z",
  "createdAt": "2025-10-15T12:00:00Z",
  "updatedAt": "2025-10-18T16:30:00Z"
}
```

---

#### POST /orders/:orderId/cancel

Cancel an order and initiate refund.

**Authentication:** Required

**Path Parameters:**
- `orderId` - Order ID

**Request:**
```typescript
interface CancelOrderRequest {
  reason?: string;
}

// Example
await apiClient.post('/orders/order-789/cancel', {
  reason: "Changed my mind"
});
```

**Response:**
```typescript
interface CancelOrderResponse {
  orderId: string;
  status: OrderStatus; // Will be 'CANCELLED'
  refundAmount: number;
  refundStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED';
  message: string;
}

// Example
{
  "orderId": "order-789",
  "status": "CANCELLED",
  "refundAmount": 1444.99,
  "refundStatus": "PROCESSING",
  "message": "Your order has been cancelled and refund is being processed"
}
```

---

### User Endpoints

#### GET /users/me

Get current user's profile information.

**Authentication:** Required

**Response:**
```typescript
interface UserProfileResponse {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  avatar?: string;
  addresses: Address[];
  createdAt: string;
  updatedAt: string;
}

// Example
{
  "id": "user-123",
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "+61 400 000 000",
  "avatar": "https://example.com/avatars/user-123.jpg",
  "addresses": [
    {
      "fullName": "John Doe",
      "addressLine1": "123 Main St",
      "city": "Sydney",
      "state": "NSW",
      "postalCode": "2000",
      "country": "Australia",
      "phone": "+61 400 000 000"
    }
  ],
  "createdAt": "2025-01-01T00:00:00Z",
  "updatedAt": "2025-10-15T10:30:00Z"
}
```

---

#### PUT /users/me

Update user profile.

**Authentication:** Required

**Request:**
```typescript
interface UpdateProfileRequest {
  fullName?: string;
  phone?: string;
  avatar?: string;
}

// Example
await apiClient.put('/users/me', {
  fullName: "John Michael Doe",
  phone: "+61 400 111 222"
});
```

**Response:**
```typescript
// Returns updated user profile
interface UpdateProfileResponse extends UserProfileResponse {}
```

---

#### POST /users/me/addresses

Add new address to user profile.

**Authentication:** Required

**Request:**
```typescript
interface AddAddressRequest extends Address {}
```

**Response:**
```typescript
interface AddAddressResponse {
  addressId: string;
  message: string;
}
```

---

#### PUT /users/me/addresses/:addressId

Update existing address.

**Authentication:** Required

**Path Parameters:**
- `addressId` - Address ID

**Request:**
```typescript
interface UpdateAddressRequest extends Address {}
```

**Response:**
```typescript
{
  "message": "Address updated successfully"
}
```

---

#### DELETE /users/me/addresses/:addressId

Delete an address.

**Authentication:** Required

**Path Parameters:**
- `addressId` - Address ID

**Response:**
```typescript
{
  "message": "Address deleted successfully"
}
```

---

## Request/Response Formats

### Standard Success Response

```typescript
{
  "data": {...},
  "message": "Operation successful",
  "timestamp": "2025-10-15T12:00:00Z"
}
```

### Standard Error Response

```typescript
interface ErrorResponse {
  error: {
    code: string;
    message: string;
    details?: any;
  };
  timestamp: string;
}

// Example
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request data",
    "details": {
      "email": "Email is required",
      "password": "Password must be at least 8 characters"
    }
  },
  "timestamp": "2025-10-15T12:00:00Z"
}
```

---

## Error Handling

### HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST (resource created) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Authenticated but not authorized |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Resource conflict (e.g., duplicate email) |
| 422 | Unprocessable Entity | Validation errors |
| 500 | Internal Server Error | Server error |
| 503 | Service Unavailable | Server temporarily unavailable |

### Error Codes

```typescript
export const ERROR_CODES = {
  // Authentication errors
  AUTH_INVALID_CREDENTIALS: 'INVALID_CREDENTIALS',
  AUTH_TOKEN_EXPIRED: 'TOKEN_EXPIRED',
  AUTH_TOKEN_INVALID: 'TOKEN_INVALID',
  AUTH_UNAUTHORIZED: 'UNAUTHORIZED',

  // Validation errors
  VALIDATION_ERROR: 'VALIDATION_ERROR',
  VALIDATION_EMAIL_INVALID: 'EMAIL_INVALID',
  VALIDATION_PASSWORD_WEAK: 'PASSWORD_WEAK',

  // Resource errors
  RESOURCE_NOT_FOUND: 'NOT_FOUND',
  RESOURCE_CONFLICT: 'CONFLICT',

  // Business logic errors
  ORDER_INSUFFICIENT_STOCK: 'INSUFFICIENT_STOCK',
  ORDER_CANNOT_CANCEL: 'CANNOT_CANCEL_ORDER',
  CART_EMPTY: 'CART_EMPTY',

  // Server errors
  SERVER_ERROR: 'INTERNAL_ERROR',
  SERVICE_UNAVAILABLE: 'SERVICE_UNAVAILABLE',
} as const;
```

### Error Handling in Services

```typescript
// api/services/product.service.ts
import { apiClient } from '@/lib/axios';
import { AxiosError } from 'axios';

export const productService = {
  async getProducts(params?: ProductQueryParams): Promise<ProductListResponse> {
    try {
      const response = await apiClient.get<ProductListResponse>('/products', { params });
      return response.data;
    } catch (error) {
      handleApiError(error);
      throw error;
    }
  },

  async getProduct(productId: string): Promise<ProductDetailResponse> {
    try {
      const response = await apiClient.get<ProductDetailResponse>(`/products/${productId}`);
      return response.data;
    } catch (error) {
      handleApiError(error);
      throw error;
    }
  },
};

// Error handler utility
function handleApiError(error: unknown): void {
  if (error instanceof AxiosError) {
    const errorResponse = error.response?.data as ErrorResponse;

    switch (error.response?.status) {
      case 400:
        console.error('Bad Request:', errorResponse?.error?.message);
        break;
      case 401:
        console.error('Unauthorized - redirecting to login');
        // Handled by interceptor
        break;
      case 404:
        console.error('Resource not found');
        break;
      case 500:
        console.error('Server error - please try again later');
        break;
      default:
        console.error('An error occurred:', errorResponse?.error?.message);
    }
  } else {
    console.error('Unknown error:', error);
  }
}
```

---

## Best Practices

### 1. Use Service Layer

Create service modules for each API resource:

```typescript
// api/services/auth.service.ts
export const authService = {
  login: (credentials) => apiClient.post('/auth/login', credentials),
  register: (userData) => apiClient.post('/auth/register', userData),
  logout: () => apiClient.post('/auth/logout'),
};

// Usage in component
import { authService } from '@/api/services/auth.service';

const handleLogin = async () => {
  try {
    const response = await authService.login({ email, password });
    // Handle success
  } catch (error) {
    // Handle error
  }
};
```

### 2. Use React Query for Data Fetching

```typescript
// hooks/useProducts.ts
import { useQuery } from '@tanstack/react-query';
import { productService } from '@/api/services/product.service';

export const useProducts = (params?: ProductQueryParams) => {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => productService.getProducts(params),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

// Usage in component
const { data, isLoading, error } = useProducts({ category: 'electronics' });
```

### 3. Handle Loading and Error States

```typescript
const ProductList: React.FC = () => {
  const { data, isLoading, error } = useProducts();

  if (isLoading) return <LoadingSpinner />;
  if (error) return <ErrorMessage error={error} />;
  if (!data?.content.length) return <EmptyState />;

  return (
    <div>
      {data.content.map(product => (
        <ProductCard key={product.id} product={product} />
      ))}
    </div>
  );
};
```

### 4. Type Safety

Always define TypeScript types for requests and responses:

```typescript
// types/api.types.ts
export interface ApiResponse<T> {
  data: T;
  message: string;
  timestamp: string;
}

export interface PageInfo {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: PageInfo;
}
```

### 5. Environment Variables

```bash
# .env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=15000
```

```typescript
// config/api.config.ts
export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: parseInt(import.meta.env.VITE_API_TIMEOUT) || 15000,
} as const;
```

---

**Document Maintained By:** Backend & Frontend Teams
**Last Reviewed:** October 2025
