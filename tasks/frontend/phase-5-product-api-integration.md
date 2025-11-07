# Phase 5: Product API Integration - Implementation Tasks
## Online Store Application - Frontend

**Phase Duration:** 4-5 hours
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 23, 2025

---

## Overview

This phase focuses on integrating the frontend with the **real Product API** implemented in the store-backend. The backend now provides two REST endpoints for product data, and we need to align the frontend implementation to work seamlessly with these APIs.

### Backend API Endpoints

The store-backend (`ProductController.java`) provides:

```java
GET /api/products           // Returns List<ProductResponseDto>
GET /api/products/{id}      // Returns ProductResponseDto
```

### Backend DTO Structure

```java
public class ProductResponseDto {
    private Long id;              // ⚠️ Number, not String
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;      // ⚠️ camelCase, not snake_case
    private Integer stock;
    private Boolean published;
}
```

### Current Frontend State

**Already Implemented ✅:**
- Product types defined in `src/types/product.types.ts`
- Product service with mock data fallback in `src/api/services/product.service.ts`
- React Query hooks (`useProducts`, `useProduct`)
- Product components (ProductCard, ProductImage, PriceDisplay, StockBadge)
- Pages consuming products (Home.tsx, ProductDetail.tsx)

**Issues to Fix 🔧:**
1. **Type Mismatch**: Frontend uses `string` for id, backend uses `Long` (number)
2. **Field Naming**: Backend uses `imageUrl` (camelCase), frontend expects `image_url` (snake_case)
3. **Mock Data Enabled**: Currently using `VITE_USE_MOCK_DATA=true`
4. **Error Handling**: Need better error messages for real API failures

---

## Table of Contents

1. [Section 1: Type System Alignment](#section-1-type-system-alignment)
2. [Section 2: API Response Mapping](#section-2-api-response-mapping)
3. [Section 3: Mock Data Transition](#section-3-mock-data-transition)
4. [Section 4: Error Handling Enhancement](#section-4-error-handling-enhancement)
5. [Section 5: Component Updates](#section-5-component-updates)
6. [Section 6: Testing & Validation](#section-6-testing--validation)

---

## Section 1: Type System Alignment

**Estimated Time:** 30-45 minutes
**Dependencies:** None

### Task 1.1: Update Product Type Definitions

**Status:** ⬜ Not Started
**Depends On:** None

**Description:**
Align frontend types with backend DTO structure. The primary change is `id` field type from `string` to `number`.

**Current Type (Incorrect):**
```typescript
export interface Product {
  id: string;           // ❌ Backend uses Long (number)
  name: string;
  price: number;
  stock: number;
  image_url: string;    // ❌ Backend uses imageUrl
  published: boolean;
}
```

**Updated Type (Correct):**
```typescript
export interface Product {
  id: number;           // ✅ Matches backend Long
  name: string;
  price: number;
  stock: number;
  imageUrl: string;     // ✅ Matches backend camelCase
  published: boolean;
}

export interface ProductDetail extends Product {
  description: string;
  // Note: available_quantity is calculated from stock on frontend
}
```

**File to Modify:**

**src/types/product.types.ts:**
```typescript
/**
 * Product Type Definitions
 * Aligned with backend ProductResponseDto
 * Backend source: com.comp5348.store.dto.ProductResponseDto
 */

// Base product interface (from GET /api/products)
export interface Product {
  id: number;              // Changed from string to number
  name: string;
  price: number;
  stock: number;
  imageUrl: string;        // Changed from image_url to imageUrl
  published: boolean;
}

// Extended product interface (from GET /api/products/{id})
export interface ProductDetail extends Product {
  description: string;
}

// Product filters for API query params
export interface ProductFilters {
  published?: boolean;
  minPrice?: number;
  maxPrice?: number;
  search?: string;
  inStock?: boolean;
}

// Product sort options
export type ProductSortField = 'name' | 'price' | 'stock';
export type ProductSortOrder = 'asc' | 'desc';

export interface ProductSort {
  field: ProductSortField;
  order: ProductSortOrder;
}

// API Response types
export interface ProductListResponse {
  products: Product[];
  total: number;
}

// Component props types
export interface ProductCardProps {
  product: Product;
  onClick?: (product: Product) => void;
  className?: string;
}

export interface ProductImageProps {
  src: string;
  alt: string;
  className?: string;
  imageClassName?: string;
}

export interface PriceDisplayProps {
  price: number;
  currency?: string;
  className?: string;
}

export interface StockBadgeProps {
  stock: number;
  showCount?: boolean;
  className?: string;
}

export interface ProductSkeletonProps {
  className?: string;
}
```

**Acceptance Criteria:**
- [ ] `Product.id` type changed from `string` to `number`
- [ ] `Product.image_url` renamed to `Product.imageUrl`
- [ ] `ProductDetail` extends `Product` correctly
- [ ] All component prop types updated
- [ ] No TypeScript errors after changes
- [ ] File compiles successfully

**Estimated Time:** 15 minutes

---

### Task 1.2: Update Mock Data to Match New Types

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Update mock data files to use the new type structure for development and testing purposes.

**File to Modify:**

**src/mocks/products.mock.ts:**

Find and replace all instances:
- `id: 'p1'` → `id: 1`
- `id: 'p2'` → `id: 2`
- `image_url:` → `imageUrl:`

**Example Change:**
```typescript
// Before
export const mockProducts: Product[] = [
  {
    id: 'p1',
    name: 'Wireless Mouse',
    price: 49.99,
    stock: 25,
    image_url: 'https://images.unsplash.com/...',
    published: true,
  },
];

// After
export const mockProducts: Product[] = [
  {
    id: 1,
    name: 'Wireless Mouse',
    price: 49.99,
    stock: 25,
    imageUrl: 'https://images.unsplash.com/...',
    published: true,
  },
];
```

**Also Update:**
- `mockProductDetails` object keys from string to number
- All references to product IDs in mock data

**Acceptance Criteria:**
- [ ] All mock product IDs are numbers
- [ ] All `image_url` changed to `imageUrl`
- [ ] Mock data matches `Product` type exactly
- [ ] No TypeScript errors in mock files
- [ ] Mock data still usable for testing

**Estimated Time:** 15 minutes

---

## Section 2: API Response Mapping

**Estimated Time:** 45 minutes - 1 hour
**Dependencies:** Section 1 complete

### Task 2.1: Update Product Service for Real API

**Status:** ⬜ Not Started
**Depends On:** Task 1.2

**Description:**
Since we've aligned our types with the backend DTO, we can remove any response mapping logic. The service should now work directly with the backend response.

**File to Modify:**

**src/api/services/product.service.ts:**

The current service already has the correct structure. We just need to ensure it works with the updated types:

```typescript
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { Product, ProductDetail, ProductFilters } from '@/types/product.types';
import {
  mockProducts,
  mockProductDetails,
  delay,
  filterMockProducts,
} from '@/mocks/products.mock';

/**
 * Feature flag to use mock data instead of API
 * Set to false when backend API is available
 */
export const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === 'true';

/**
 * Product Service
 * Handles all product-related API calls
 * Falls back to mock data when USE_MOCK_DATA is true
 */
export const productService = {
  /**
   * Get list of products with optional filters
   * @param filters - Optional filters for products
   * @returns Promise<Product[]>
   */
  async getProducts(filters?: ProductFilters): Promise<Product[]> {
    if (USE_MOCK_DATA) {
      await delay(800);
      return filterMockProducts(mockProducts, filters);
    }

    // Real API call - backend returns Product[] directly
    const response = await apiClient.get<Product[]>(API_ENDPOINTS.PRODUCTS, {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get detailed information about a specific product
   * @param productId - Product ID (number)
   * @returns Promise<ProductDetail>
   */
  async getProduct(productId: number): Promise<ProductDetail> {
    if (USE_MOCK_DATA) {
      await delay(600);
      const mockDetail = mockProductDetails[productId];
      if (!mockDetail) {
        throw new Error(`Product with id ${productId} not found`);
      }
      return mockDetail;
    }

    // Real API call - backend returns ProductDetail directly
    const response = await apiClient.get<ProductDetail>(
      API_ENDPOINTS.PRODUCT_DETAIL(productId.toString())
    );
    return response.data;
  },
};
```

**Key Changes:**
1. `getProduct` parameter changed from `string` to `number`
2. Convert number to string for URL: `productId.toString()`
3. Backend response types already match our interfaces

**Acceptance Criteria:**
- [ ] `getProduct` accepts `number` for productId
- [ ] Product ID converted to string for URL construction
- [ ] Type annotations match updated interfaces
- [ ] Mock data path still works
- [ ] No TypeScript errors

**Estimated Time:** 20 minutes

---

### Task 2.2: Update React Query Hooks

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Update the useProducts hooks to work with the new number-based IDs.

**File to Modify:**

**src/hooks/useProducts.ts:**

```typescript
import { useQuery, type UseQueryResult } from '@tanstack/react-query';
import { productService } from '@/api/services/product.service';
import type { Product, ProductDetail, ProductFilters } from '@/types/product.types';

/**
 * Hook to fetch list of products
 * @param filters - Optional filters for products
 * @returns React Query result with products data
 */
export const useProducts = (
  filters?: ProductFilters
): UseQueryResult<Product[], Error> => {
  return useQuery({
    queryKey: ['products', filters],
    queryFn: () => productService.getProducts(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

/**
 * Hook to fetch single product detail
 * @param productId - Product ID (number)
 * @returns React Query result with product detail
 */
export const useProduct = (
  productId: number | undefined
): UseQueryResult<ProductDetail, Error> => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: () => {
      if (!productId) {
        throw new Error('Product ID is required');
      }
      return productService.getProduct(productId);
    },
    enabled: !!productId, // Only fetch if productId exists
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
```

**Key Changes:**
1. `useProduct` parameter type: `string` → `number | undefined`
2. Add type guard in `queryFn` to ensure productId exists
3. Query key uses number directly (React Query handles this)

**Acceptance Criteria:**
- [ ] `useProduct` accepts `number | undefined`
- [ ] Query only executes when productId is valid
- [ ] Query keys use number type correctly
- [ ] No TypeScript errors
- [ ] Hooks work with both mock and real data

**Estimated Time:** 15 minutes

---

### Task 2.3: Update API Endpoints Configuration

**Status:** ⬜ Not Started
**Depends On:** None (can be done in parallel)

**Description:**
Verify API endpoints are correctly configured for the backend.

**File to Verify:**

**src/config/api.config.ts:**

The current configuration should already be correct:

```typescript
export const API_ENDPOINTS = {
  // ... other endpoints

  // Product endpoints
  PRODUCTS: "/products",
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  // ... other endpoints
} as const;
```

**Note:** The `PRODUCT_DETAIL` function accepts string (for URL construction) which is correct. We convert number to string in the service layer.

**Verification Steps:**
1. Confirm `PRODUCTS` endpoint is `/products` (not `/api/products` - baseURL includes `/api`)
2. Confirm `PRODUCT_DETAIL` constructs `/products/{id}`
3. Verify `API_CONFIG.baseURL` is `http://localhost:8081/api`

**Acceptance Criteria:**
- [ ] Product endpoints match backend routes
- [ ] baseURL includes `/api` prefix
- [ ] Backend is running on port 8081
- [ ] No trailing slashes in endpoints

**Estimated Time:** 10 minutes

---

## Section 3: Mock Data Transition

**Estimated Time:** 30 minutes
**Dependencies:** Section 2 complete

### Task 3.1: Configure Environment for Real API

**Status:** ⬜ Not Started
**Depends On:** Tasks 2.1, 2.2

**Description:**
Update environment configuration to use the real backend API instead of mock data.

**File to Modify:**

**frontend/.env.local:**

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8081/api
VITE_API_TIMEOUT=15000

# Mock Data Configuration
# Set to 'false' to use real backend API
# Set to 'true' for development when backend is unavailable
VITE_USE_MOCK_DATA=false

# App Configuration
VITE_APP_NAME=Online Store
VITE_APP_VERSION=1.0.0

# Port Configuration
VITE_PORT=3000
```

**Verification Steps:**
1. Set `VITE_USE_MOCK_DATA=false`
2. Ensure backend is running: `cd store-backend && ../gradlew bootRun`
3. Verify backend is accessible: `curl http://localhost:8081/api/products`
4. Start frontend: `npm run dev`
5. Check browser console for API calls

**Acceptance Criteria:**
- [ ] `VITE_USE_MOCK_DATA=false` in .env.local
- [ ] Backend running on port 8081
- [ ] Backend `/api/products` endpoint responds
- [ ] Frontend makes real API calls (check Network tab)
- [ ] No CORS errors in console

**Estimated Time:** 15 minutes

---

### Task 3.2: Document Mock Data Usage

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Update documentation to explain when and how to use mock data.

**File to Modify:**

**frontend/README.md:**

Add section about mock data:

```markdown
## Mock Data vs Real API

The frontend can work with either mock data or the real backend API.

### Using Real Backend API (Default)

1. Ensure backend is running:
   ```bash
   cd store-backend
   ../gradlew bootRun
   ```

2. Set environment variable in `.env.local`:
   ```env
   VITE_USE_MOCK_DATA=false
   ```

3. Start frontend:
   ```bash
   npm run dev
   ```

### Using Mock Data (Development/Testing)

Useful when backend is unavailable or for frontend-only development:

1. Set environment variable in `.env.local`:
   ```env
   VITE_USE_MOCK_DATA=true
   ```

2. Start frontend:
   ```bash
   npm run dev
   ```

Mock data is defined in `src/mocks/products.mock.ts`.

### Switching Between Modes

You can toggle between mock and real API without code changes:
- Update `VITE_USE_MOCK_DATA` in `.env.local`
- Restart dev server: `npm run dev`
```

**Acceptance Criteria:**
- [ ] README documents mock data usage
- [ ] Clear instructions for both modes
- [ ] Instructions include backend startup
- [ ] Environment variable clearly explained

**Estimated Time:** 15 minutes

---

## Section 4: Error Handling Enhancement

**Estimated Time:** 45 minutes
**Dependencies:** Section 3 complete

### Task 4.1: Add HTTP Error Handling

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Enhance error handling to provide better user feedback for different API error scenarios.

**Create New File:**

**src/utils/errorHandling.ts:**

```typescript
/**
 * Error Handling Utilities
 * Provides consistent error messages for API failures
 */

export interface ApiError {
  message: string;
  status?: number;
  details?: string;
}

/**
 * Extract error message from various error types
 */
export const getErrorMessage = (error: unknown): string => {
  if (error instanceof Error) {
    return error.message;
  }
  if (typeof error === 'string') {
    return error;
  }
  return 'An unexpected error occurred';
};

/**
 * Handle API errors and return user-friendly messages
 */
export const handleApiError = (error: unknown): ApiError => {
  // Axios error with response
  if (error && typeof error === 'object' && 'response' in error) {
    const axiosError = error as any;
    const status = axiosError.response?.status;
    const data = axiosError.response?.data;

    switch (status) {
      case 404:
        return {
          message: 'Product not found',
          status: 404,
          details: 'The requested product does not exist or has been removed.',
        };
      case 500:
        return {
          message: 'Server error',
          status: 500,
          details: 'An error occurred on the server. Please try again later.',
        };
      case 503:
        return {
          message: 'Service unavailable',
          status: 503,
          details: 'The service is temporarily unavailable. Please try again later.',
        };
      default:
        return {
          message: data?.message || 'Request failed',
          status,
          details: 'An error occurred while processing your request.',
        };
    }
  }

  // Network error
  if (error && typeof error === 'object' && 'message' in error) {
    const errorMessage = (error as Error).message;
    if (errorMessage.includes('Network Error') || errorMessage.includes('ERR_CONNECTION_REFUSED')) {
      return {
        message: 'Cannot connect to server',
        details: 'Please ensure the backend server is running on http://localhost:8081',
      };
    }
  }

  // Generic error
  return {
    message: getErrorMessage(error),
    details: 'Please try again or contact support if the problem persists.',
  };
};

/**
 * Product-specific error messages
 */
export const getProductErrorMessage = (error: unknown, productId?: number): string => {
  const apiError = handleApiError(error);

  if (apiError.status === 404 && productId) {
    return `Product #${productId} not found`;
  }

  return apiError.message;
};
```

**Acceptance Criteria:**
- [ ] Error utility functions created
- [ ] Handles different HTTP status codes
- [ ] Provides user-friendly messages
- [ ] Handles network errors specifically
- [ ] TypeScript types defined
- [ ] No compilation errors

**Estimated Time:** 20 minutes

---

### Task 4.2: Update Components with Better Error Display

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Update Home and ProductDetail pages to use enhanced error handling.

**File to Modify:**

**src/pages/Home.tsx:**

```typescript
import { useNavigate } from 'react-router-dom';
import { useProducts } from '@/hooks/useProducts';
import { ProductCard } from '@/components/features/product/ProductCard';
import { ProductSkeleton } from '@/components/features/product/ProductSkeleton';
import { handleApiError } from '@/utils/errorHandling';

function Home() {
  const navigate = useNavigate();
  const { data: products, isLoading, isError, error } = useProducts();

  const errorInfo = isError ? handleApiError(error) : null;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <div className="mb-12 text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Welcome to Our Store
        </h1>
        <p className="text-lg text-gray-600 max-w-2xl mx-auto">
          Discover our collection of quality products at great prices
        </p>
      </div>

      {/* Products Section */}
      <div className="mb-8">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Featured Products
        </h2>

        {/* Loading State */}
        {isLoading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {[...Array(8)].map((_, index) => (
              <ProductSkeleton key={index} />
            ))}
          </div>
        )}

        {/* Error State - Enhanced */}
        {isError && errorInfo && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-red-800 mb-2">
              {errorInfo.message}
            </h3>
            <p className="text-red-600 mb-4">
              {errorInfo.details}
            </p>
            {errorInfo.status === undefined && (
              <div className="bg-white rounded-md p-3 text-sm text-gray-700">
                <p className="font-medium mb-1">💡 Quick Fix:</p>
                <ol className="list-decimal list-inside space-y-1">
                  <li>Ensure backend is running: <code className="bg-gray-100 px-1 rounded">cd store-backend && ../gradlew bootRun</code></li>
                  <li>Verify backend is accessible: <code className="bg-gray-100 px-1 rounded">curl http://localhost:8081/api/products</code></li>
                  <li>Check CORS configuration in backend</li>
                </ol>
              </div>
            )}
          </div>
        )}

        {/* Products Grid */}
        {!isLoading && !isError && products && (
          <>
            {products.length === 0 ? (
              <div className="bg-gray-50 border border-gray-200 rounded-lg p-12 text-center">
                <p className="text-gray-600 text-lg">No products available</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onClick={(product) => {
                      navigate(`/products/${product.id}`);
                    }}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default Home;
```

**Acceptance Criteria:**
- [ ] Enhanced error display implemented
- [ ] Shows specific error messages
- [ ] Includes troubleshooting steps for connection errors
- [ ] User-friendly error UI
- [ ] No TypeScript errors

**Estimated Time:** 15 minutes

---

### Task 4.3: Update ProductDetail Error Handling

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Add enhanced error handling to the ProductDetail page.

**File to Modify:**

**src/pages/ProductDetail.tsx:**

Add import and update error display:

```typescript
import { getProductErrorMessage, handleApiError } from '@/utils/errorHandling';

// ... in component

  const { data: product, isLoading, isError, error } = useProduct(
    id ? parseInt(id) : undefined  // ⚠️ Convert string param to number
  );

  // ... later in render

  if (isError || !product) {
    const errorInfo = handleApiError(error);

    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            {errorInfo.message}
          </h3>
          <p className="text-red-600 mb-4">
            {errorInfo.details}
          </p>
          {id && errorInfo.status === 404 && (
            <p className="text-sm text-gray-600 mb-4">
              Product ID: <code className="bg-white px-2 py-1 rounded">{id}</code>
            </p>
          )}
          <button
            onClick={handleBackToProducts}
            className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            Back to Products
          </button>
        </div>
      </div>
    );
  }
```

**Key Change:** Convert URL parameter (string) to number for `useProduct`:
```typescript
const { id } = useParams<{ id: string }>();

// Convert to number for the hook
const { data: product, isLoading, isError, error } = useProduct(
  id ? parseInt(id, 10) : undefined
);
```

**Acceptance Criteria:**
- [ ] URL parameter converted to number
- [ ] Enhanced error display
- [ ] 404 errors show product ID
- [ ] User-friendly error messages
- [ ] No TypeScript errors

**Estimated Time:** 10 minutes

---

## Section 5: Component Updates

**Estimated Time:** 30 minutes
**Dependencies:** Section 1, 2 complete

### Task 5.1: Update ProductCard Component

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Update ProductCard to use `imageUrl` instead of `image_url`.

**File to Modify:**

**src/components/features/product/ProductCard/ProductCard.tsx:**

```typescript
import React from 'react';
import { Card } from '@/components/ui/Card';
import { ProductImage } from '../ProductImage';
import { PriceDisplay } from '../PriceDisplay';
import { StockBadge } from '../StockBadge';
import type { ProductCardProps } from '@/types/product.types';

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onClick,
  className = '',
}) => {
  const handleClick = () => {
    if (onClick) {
      onClick(product);
    }
  };

  const isClickable = !!onClick;

  return (
    <div
      className={`h-fit overflow-hidden transition-shadow ${
        isClickable ? 'cursor-pointer hover:shadow-lg' : ''
      } ${className}`}
      onClick={isClickable ? handleClick : undefined}
    >
      <Card padding="none">
        {/* Product Image - use imageUrl */}
        <ProductImage
          src={product.imageUrl}  {/* Changed from image_url */}
          alt={product.name}
          className="aspect-square"
        />

        <div className="p-4">
          <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
            {product.name}
          </h3>

          <div className="flex items-center justify-between mt-4">
            <PriceDisplay price={product.price} />
            <StockBadge stock={product.stock} />
          </div>
        </div>
      </Card>
    </div>
  );
};
```

**Acceptance Criteria:**
- [ ] Uses `product.imageUrl` instead of `product.image_url`
- [ ] Component compiles without errors
- [ ] Renders correctly with updated types
- [ ] No runtime errors

**Estimated Time:** 5 minutes

---

### Task 5.2: Update ProductDetail Page Image Reference

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Update ProductDetail page to use `imageUrl` field.

**File to Modify:**

**src/pages/ProductDetail.tsx:**

Find the ProductGallery component:

```typescript
<ProductGallery
  mainImage={product.imageUrl}  {/* Changed from image_url */}
  altText={product.name}
/>
```

**Acceptance Criteria:**
- [ ] Uses `product.imageUrl`
- [ ] No TypeScript errors
- [ ] Product images display correctly

**Estimated Time:** 5 minutes

---

### Task 5.3: Update Component Tests

**Status:** ⬜ Not Started
**Depends On:** Tasks 5.1, 5.2

**Description:**
Update component tests to use new type structure.

**Files to Modify:**

**src/components/features/product/ProductCard/ProductCard.test.tsx:**

Update mock product in tests:

```typescript
const mockProduct: Product = {
  id: 1,              // Changed from 'p123'
  name: 'Wireless Mouse',
  price: 49.99,
  stock: 12,
  imageUrl: 'https://example.com/mouse.jpg',  // Changed from image_url
  published: true,
};
```

**src/hooks/useProducts.test.ts:**

Update all mock products to use number IDs and imageUrl field.

**Acceptance Criteria:**
- [ ] All test mock data uses number IDs
- [ ] All test mock data uses `imageUrl`
- [ ] All tests pass: `npm test`
- [ ] No TypeScript errors in test files

**Estimated Time:** 20 minutes

---

## Section 6: Testing & Validation

**Estimated Time:** 1 hour
**Dependencies:** All previous sections complete

### Task 6.1: Backend Integration Testing

**Status:** ⬜ Not Started
**Depends On:** Tasks 3.1, 4.2, 4.3

**Description:**
Perform comprehensive testing with the real backend API.

**Prerequisites:**
1. Backend must be running with seed data:
   ```bash
   cd store-backend
   ../gradlew bootRun
   ```

2. Verify backend has products:
   ```bash
   curl http://localhost:8081/api/products | jq
   ```

3. Frontend configured for real API:
   ```env
   VITE_USE_MOCK_DATA=false
   ```

**Manual Testing Checklist:**

**Product List (Home Page):**
- [ ] Navigate to http://localhost:3000
- [ ] Products load and display correctly
- [ ] Product images display (check `imageUrl` field)
- [ ] Prices display correctly
- [ ] Stock badges show correct status
- [ ] Click on product card navigates to detail page
- [ ] No console errors
- [ ] Network tab shows `GET /api/products` request
- [ ] Response status is 200

**Product Detail Page:**
- [ ] Navigate to specific product (e.g., http://localhost:3000/products/1)
- [ ] Product detail loads correctly
- [ ] All product information displays
- [ ] Image displays correctly
- [ ] Price and stock show correctly
- [ ] Back button works
- [ ] No console errors
- [ ] Network tab shows `GET /api/products/1` request
- [ ] Response status is 200

**Error Scenarios:**
- [ ] Stop backend server
- [ ] Refresh product list page
- [ ] Verify connection error message displays
- [ ] Verify troubleshooting steps appear
- [ ] Start backend server
- [ ] Refresh page - products load
- [ ] Navigate to invalid product ID (e.g., /products/99999)
- [ ] Verify 404 error message displays
- [ ] Verify "Back to Products" button works

**React Query Behavior:**
- [ ] Open React Query DevTools (bottom of page)
- [ ] Verify `['products']` query exists
- [ ] Verify query status is 'success'
- [ ] Navigate to product detail
- [ ] Verify `['product', 1]` query exists
- [ ] Return to home
- [ ] Verify products load from cache (no new request)
- [ ] Wait 5 minutes or invalidate cache
- [ ] Verify refetch occurs

**Acceptance Criteria:**
- [ ] All product list tests pass
- [ ] All product detail tests pass
- [ ] All error scenarios handled gracefully
- [ ] React Query caching works correctly
- [ ] No console errors during normal operation
- [ ] Network requests use correct endpoints
- [ ] UI displays all product data correctly

**Estimated Time:** 30 minutes

---

### Task 6.2: Automated Test Updates and Execution

**Status:** ⬜ Not Started
**Depends On:** Task 5.3

**Description:**
Ensure all automated tests pass with the new type structure.

**Commands to Run:**

```bash
# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage

# Type check
npm run type-check

# Lint
npm run lint
```

**Test Files to Verify:**
- [ ] `src/api/services/product.service.test.ts`
- [ ] `src/hooks/useProducts.test.ts`
- [ ] `src/components/features/product/ProductCard/ProductCard.test.tsx`
- [ ] `src/pages/Home.test.tsx` (if exists)

**Fix Common Issues:**

1. **Type mismatches in tests:**
   - Update all mock data to use number IDs
   - Update all `image_url` to `imageUrl`

2. **Service tests failing:**
   - Ensure mock axios responses match new types
   - Update test expectations

3. **Hook tests failing:**
   - Update test wrappers if needed
   - Verify query keys are correct

**Acceptance Criteria:**
- [ ] All unit tests pass
- [ ] All integration tests pass
- [ ] Test coverage maintained or improved
- [ ] No TypeScript errors: `npm run type-check`
- [ ] No lint errors: `npm run lint`

**Estimated Time:** 20 minutes

---

### Task 6.3: Cross-Browser Testing

**Status:** ⬜ Not Started
**Depends On:** Task 6.1

**Description:**
Verify the application works across different browsers.

**Browsers to Test:**
- [ ] Chrome (primary development browser)
- [ ] Firefox
- [ ] Safari (macOS)
- [ ] Edge

**Test Scenarios per Browser:**
1. Product list loads
2. Product detail loads
3. Navigation works
4. Images display correctly
5. No console errors

**Acceptance Criteria:**
- [ ] Works in Chrome
- [ ] Works in Firefox
- [ ] Works in Safari (if on macOS)
- [ ] Works in Edge
- [ ] Consistent behavior across browsers
- [ ] No browser-specific errors

**Estimated Time:** 10 minutes

---

## Documentation & Completion

### Task 7.1: Update Project Documentation

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Update project documentation to reflect the API integration.

**Files to Update:**

**1. frontend/README.md:**
- Add section on Product API integration
- Document type changes made
- Update troubleshooting section

**2. Create Integration Report:**

**tasks/frontend/completion-reports/phase-5-product-api-integration-report.md:**

```markdown
# Phase 5: Product API Integration - Completion Report
**Completed:** [Date]
**Completed By:** [Your Name]

## Overview
Successfully integrated frontend with backend Product API.

## Key Changes Made

### Type System Updates
- Changed Product.id from string to number
- Renamed image_url to imageUrl to match backend DTO
- Updated all components and hooks

### API Integration
- Configured real API endpoints
- Removed mock data dependency (configurable)
- Enhanced error handling

### Component Updates
- Updated ProductCard to use imageUrl
- Updated ProductDetail page
- Fixed all type references

## Testing Summary
- ✅ All unit tests passing
- ✅ Integration tests with real API passing
- ✅ Manual testing completed
- ✅ Error scenarios validated

## Issues Encountered
[List any issues and how they were resolved]

## Deviations from Plan
[Note any changes from the original task plan]

## Time Taken
- Estimated: 4-5 hours
- Actual: [Actual time]

## Lessons Learned
[Key takeaways from this phase]

## Next Steps
- Phase 6: Cart API Integration (when available)
- Performance optimization
- Additional error recovery strategies
```

**Acceptance Criteria:**
- [ ] README updated
- [ ] Completion report created
- [ ] All changes documented
- [ ] Issues and solutions noted

**Estimated Time:** 20 minutes

---

## Summary

### Total Tasks: 19
### Estimated Total Time: 4-5 hours
### Priority: HIGH
### Prerequisites: Backend Product API must be running

### Deliverables Checklist:

**Type System:**
- [ ] Product types aligned with backend DTO
- [ ] ID type changed from string to number
- [ ] Field names match backend (imageUrl)

**API Integration:**
- [ ] Product service updated
- [ ] React Query hooks updated
- [ ] Real API calls working
- [ ] Mock data still available for development

**Error Handling:**
- [ ] Enhanced error utilities created
- [ ] Better user-facing error messages
- [ ] Troubleshooting guidance included
- [ ] Network errors handled gracefully

**Components:**
- [ ] ProductCard updated
- [ ] ProductDetail updated
- [ ] All tests updated and passing

**Testing:**
- [ ] Manual integration testing complete
- [ ] Automated tests passing
- [ ] Cross-browser testing complete

**Documentation:**
- [ ] README updated
- [ ] Completion report created
- [ ] Mock data usage documented

---

## Troubleshooting Guide

### Common Issues

**Issue 1: "Cannot find module" errors**
- **Cause:** Path aliases not configured correctly
- **Solution:** Verify `tsconfig.json` and `vite.config.ts` have `@/` alias

**Issue 2: "Property 'imageUrl' does not exist on type 'Product'"**
- **Cause:** Type definition not updated
- **Solution:** Ensure Task 1.1 is complete, restart TypeScript server

**Issue 3: Network error when loading products**
- **Cause:** Backend not running or CORS issue
- **Solution:**
  1. Start backend: `cd store-backend && ../gradlew bootRun`
  2. Verify: `curl http://localhost:8081/api/products`
  3. Check CORS configuration in backend

**Issue 4: Products show as undefined or null**
- **Cause:** Type mismatch or response mapping issue
- **Solution:** Check Network tab in DevTools, verify response structure

**Issue 5: Tests failing with type errors**
- **Cause:** Mock data not updated
- **Solution:** Update all test mock data to use new types (Task 5.3)

**Issue 6: Product ID not working in URLs**
- **Cause:** Number/string conversion issue
- **Solution:** Ensure `parseInt(id)` in ProductDetail page (Task 4.3)

---

## Quick Reference

### Environment Variables
```env
VITE_USE_MOCK_DATA=false  # Use real API
VITE_API_BASE_URL=http://localhost:8081/api
```

### Backend Commands
```bash
# Start backend
cd store-backend
../gradlew bootRun

# Test backend
curl http://localhost:8081/api/products
curl http://localhost:8081/api/products/1
```

### Frontend Commands
```bash
# Start frontend
npm run dev

# Run tests
npm test

# Type check
npm run type-check
```

### Key Type Changes
```typescript
// Old
interface Product {
  id: string;
  image_url: string;
}

// New
interface Product {
  id: number;
  imageUrl: string;
}
```

---

## Next Phase

After completing this phase, proceed to:
- **Phase 6**: Cart & Checkout API Integration (when backend is ready)
- **Alternative**: UI/UX improvements and performance optimization

---

**Document Created:** October 23, 2025
**Phase Status:** Ready to Start
**Backend Dependency:** Product API must be running on port 8081

---

**Good luck with the integration! 🚀**
