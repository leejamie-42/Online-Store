# Phase 3a: Product Catalog Foundation - Implementation Tasks
## Online Store Application - Frontend (PR #1)

**Phase Duration:** 2-3 days
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 2025

---

## Overview

This document contains detailed, actionable tasks for Phase 3a of the frontend implementation. Phase 3a focuses on building the foundation for the product catalog using **Test-Driven Development (TDD)** methodology and **React Query** for server state management.

**This phase corresponds to PR #1 of the Product Catalog feature.**

### Phase 3a Goals
- ✅ Set up React Query for server state management
- ✅ Create type definitions aligned with backend API
- ✅ Build API service layer with React Query hooks
- ✅ Implement base product components using TDD
- ✅ Establish testing patterns for product features

---

## Table of Contents

1. [Section 1: Setup & Dependencies](#section-1-setup--dependencies)
2. [Section 2: Type Definitions](#section-2-type-definitions)
3. [Section 3: API Service Layer (TDD)](#section-3-api-service-layer-tdd)
4. [Section 4: Base Product Components (TDD)](#section-4-base-product-components-tdd)
5. [Testing & Validation](#testing--validation)

---

## Section 1: Setup & Dependencies

**Estimated Time:** 45 minutes
**Dependencies:** Phase 1 complete

### Task 1.1: Install React Query and Configure

**Status:** ⬜ Not Started
**Depends On:** Phase 1 complete

**Description:**
Install TanStack Query (React Query) and set up the query client configuration.

**Commands:**
```bash
cd frontend
npm install @tanstack/react-query @tanstack/react-query-devtools
```

**Files to Create:**

**src/lib/queryClient.ts:**
```typescript
import { QueryClient } from '@tanstack/react-query';

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 5 * 60 * 1000, // 5 minutes
      gcTime: 10 * 60 * 1000, // 10 minutes (formerly cacheTime)
      retry: 3,
      retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
      refetchOnWindowFocus: false,
    },
    mutations: {
      retry: 1,
    },
  },
});
```

**Acceptance Criteria:**
- [ ] @tanstack/react-query installed
- [ ] Query client configured with default options
- [ ] No TypeScript errors
- [ ] Development tools available

**Estimated Time:** 15 minutes

---

### Task 1.2: Update App.tsx with QueryClientProvider

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Wrap the application with QueryClientProvider to enable React Query throughout the app.

**File to Modify:**

**src/App.tsx:**
```typescript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { AuthProvider } from '@/context/AuthContext';
import { queryClient } from '@/lib/queryClient';
import './App.css';

// Import pages
import Home from '@/pages/Home';
import About from '@/pages/About';
import Contact from '@/pages/Contact';
import NotFound from '@/pages/NotFound';

function App() {
  return (
    <QueryClientProvider client={queryClient}>
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
      {/* React Query Devtools - only in development */}
      {import.meta.env.DEV && <ReactQueryDevtools initialIsOpen={false} />}
    </QueryClientProvider>
  );
}

export default App;
```

**Acceptance Criteria:**
- [ ] QueryClientProvider wraps the app
- [ ] React Query DevTools available in development
- [ ] No console errors
- [ ] Existing functionality still works

**Estimated Time:** 10 minutes

---

## Section 2: Type Definitions

**Estimated Time:** 30 minutes
**Dependencies:** Section 1 complete

### Task 2.1: Create Product Type Definitions

**Status:** ⬜ Not Started
**Depends On:** Task 1.2

**Description:**
Define TypeScript types for products based on the API specification from SYSTEM_INTERFACE_SPEC.md.

**Files to Create:**

**src/types/product.types.ts:**
```typescript
/**
 * Product Type Definitions
 * Based on API spec: GET /api/products and GET /api/products/{id}
 */

// Base product interface (from GET /api/products)
export interface Product {
  id: string;
  name: string;
  price: number;
  stock: number;
  image_url: string;
  published: boolean;
}

// Extended product interface (from GET /api/products/{id})
export interface ProductDetail extends Product {
  description: string;
  available_quantity: number;
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

export interface ProductDetailResponse extends ProductDetail {}

// Component props types
export interface ProductCardProps {
  product: Product;
  onAddToCart?: (productId: string) => void;
  onViewDetails?: (productId: string) => void;
}

export interface ProductImageProps {
  src: string;
  alt: string;
  className?: string;
}

export interface PriceDisplayProps {
  price: number;
  currency?: string;
  className?: string;
}

export interface StockBadgeProps {
  stock: number;
  available_quantity?: number;
  showQuantity?: boolean;
}
```

**Acceptance Criteria:**
- [ ] All product types defined
- [ ] Types match API specification
- [ ] Component prop types included
- [ ] No TypeScript errors
- [ ] Types are properly exported

**Estimated Time:** 30 minutes

---

## Section 3: API Service Layer (TDD)

**Estimated Time:** 2-3 hours
**Dependencies:** Section 2 complete

### Task 3.1: Write Tests for Product Service

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Following TDD, write tests FIRST for the product service before implementation.

**Files to Create:**

**src/api/services/product.service.test.ts:**
```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { apiClient } from '@/lib/axios';
import { productService } from './product.service';
import type { Product, ProductDetail, ProductFilters } from '@/types/product.types';

// Mock axios
vi.mock('@/lib/axios');

describe('productService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getProducts', () => {
    it('should fetch products successfully', async () => {
      const mockProducts: Product[] = [
        {
          id: 'p123',
          name: 'Wireless Mouse',
          price: 49.99,
          stock: 12,
          image_url: 'https://example.com/mouse.jpg',
          published: true,
        },
      ];

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProducts });

      const result = await productService.getProducts();

      expect(apiClient.get).toHaveBeenCalledWith('/products', { params: undefined });
      expect(result).toEqual(mockProducts);
    });

    it('should fetch products with filters', async () => {
      const filters: ProductFilters = {
        published: true,
        minPrice: 10,
        maxPrice: 100,
        search: 'mouse',
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: [] });

      await productService.getProducts(filters);

      expect(apiClient.get).toHaveBeenCalledWith('/products', { params: filters });
    });

    it('should handle errors when fetching products', async () => {
      vi.mocked(apiClient.get).mockRejectedValue(new Error('Network error'));

      await expect(productService.getProducts()).rejects.toThrow('Network error');
    });
  });

  describe('getProduct', () => {
    it('should fetch product detail successfully', async () => {
      const mockProduct: ProductDetail = {
        id: 'p123',
        name: 'Wireless Mouse',
        description: 'Ergonomic and precise',
        price: 49.99,
        stock: 25,
        available_quantity: 12,
        image_url: 'https://example.com/mouse.jpg',
        published: true,
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProduct });

      const result = await productService.getProduct('p123');

      expect(apiClient.get).toHaveBeenCalledWith('/products/p123');
      expect(result).toEqual(mockProduct);
    });

    it('should handle 404 when product not found', async () => {
      vi.mocked(apiClient.get).mockRejectedValue({
        response: { status: 404 },
      });

      await expect(productService.getProduct('invalid-id')).rejects.toThrow();
    });
  });
});
```

**Acceptance Criteria:**
- [ ] Tests written for all service methods
- [ ] Tests cover success and error cases
- [ ] Mock data matches type definitions
- [ ] Tests fail initially (no implementation yet)
- [ ] Tests use Vitest and follow testing standards

**Estimated Time:** 45 minutes

---

### Task 3.2: Implement Product Service

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Implement the product service to make the tests pass.

**Files to Create:**

**src/api/services/product.service.ts:**
```typescript
import { apiClient } from '@/lib/axios';
import { API_ENDPOINTS } from '@/config/api.config';
import type { Product, ProductDetail, ProductFilters } from '@/types/product.types';

/**
 * Product Service
 * Handles all product-related API calls
 */
export const productService = {
  /**
   * Get list of products with optional filters
   * @param filters - Optional filters for products
   * @returns Promise<Product[]>
   */
  async getProducts(filters?: ProductFilters): Promise<Product[]> {
    const response = await apiClient.get<Product[]>(API_ENDPOINTS.PRODUCTS, {
      params: filters,
    });
    return response.data;
  },

  /**
   * Get detailed information about a specific product
   * @param productId - Product ID
   * @returns Promise<ProductDetail>
   */
  async getProduct(productId: string): Promise<ProductDetail> {
    const response = await apiClient.get<ProductDetail>(
      API_ENDPOINTS.PRODUCT_DETAIL(productId)
    );
    return response.data;
  },
};
```

**Update src/config/api.config.ts:**
```typescript
export const API_ENDPOINTS = {
  // Auth
  LOGIN: '/auth/login',
  REGISTER: '/auth/register',
  LOGOUT: '/auth/logout',
  REFRESH_TOKEN: '/auth/refresh',

  // Products - ADD THESE
  PRODUCTS: '/products',
  PRODUCT_DETAIL: (id: string) => `/products/${id}`,

  // ... rest of endpoints
} as const;
```

**Acceptance Criteria:**
- [ ] Product service implemented
- [ ] All tests pass
- [ ] Proper error handling
- [ ] TypeScript types correct
- [ ] API endpoints added to config

**Estimated Time:** 30 minutes

---

### Task 3.3: Create React Query Hooks (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 3.2

**Description:**
Create custom React Query hooks for products. Write tests first, then implement.

**Files to Create:**

**src/hooks/useProducts.test.ts:**
```typescript
import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { useProducts, useProduct } from './useProducts';
import { productService } from '@/api/services/product.service';
import type { Product } from '@/types/product.types';

// Mock the product service
vi.mock('@/api/services/product.service');

// Test wrapper with QueryClient
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe('useProducts', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('should fetch products successfully', async () => {
    const mockProducts: Product[] = [
      {
        id: 'p123',
        name: 'Wireless Mouse',
        price: 49.99,
        stock: 12,
        image_url: 'https://example.com/mouse.jpg',
        published: true,
      },
    ];

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);

    const { result } = renderHook(() => useProducts(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockProducts);
  });

  it('should handle errors', async () => {
    vi.mocked(productService.getProducts).mockRejectedValue(
      new Error('Failed to fetch')
    );

    const { result } = renderHook(() => useProducts(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeDefined();
  });

  it('should accept filters parameter', async () => {
    const filters = { published: true, minPrice: 10 };

    vi.mocked(productService.getProducts).mockResolvedValue([]);

    renderHook(() => useProducts(filters), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(productService.getProducts).toHaveBeenCalledWith(filters);
    });
  });
});

describe('useProduct', () => {
  it('should fetch product detail successfully', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 25,
      available_quantity: 12,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    const { result } = renderHook(() => useProduct('p123'), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockProduct);
  });

  it('should not fetch if productId is empty', () => {
    const { result } = renderHook(() => useProduct(''), {
      wrapper: createWrapper(),
    });

    expect(result.current.isFetching).toBe(false);
  });
});
```

**src/hooks/useProducts.ts:**
```typescript
import { useQuery, UseQueryResult } from '@tanstack/react-query';
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
 * @param productId - Product ID
 * @returns React Query result with product detail
 */
export const useProduct = (
  productId: string
): UseQueryResult<ProductDetail, Error> => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: () => productService.getProduct(productId),
    enabled: !!productId, // Only fetch if productId exists
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
```

**Acceptance Criteria:**
- [ ] Tests written for all hooks
- [ ] Hooks implemented and tests pass
- [ ] Proper query key structure
- [ ] Error handling works
- [ ] Enabled option works correctly
- [ ] Stale time configured

**Estimated Time:** 1 hour

---

## Section 4: Base Product Components (TDD)

**Estimated Time:** 4-5 hours
**Dependencies:** Section 3 complete

### Task 4.1: Create PriceDisplay Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 3.3

**Description:**
Create a reusable component for displaying product prices with currency formatting. **Write tests first!**

**Files to Create:**

**src/components/features/product/PriceDisplay/PriceDisplay.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PriceDisplay } from './PriceDisplay';

describe('PriceDisplay', () => {
  it('renders price with default currency (USD)', () => {
    render(<PriceDisplay price={49.99} />);

    expect(screen.getByText('$49.99')).toBeInTheDocument();
  });

  it('renders price with custom currency', () => {
    render(<PriceDisplay price={49.99} currency="EUR" />);

    expect(screen.getByText(/49.99/)).toBeInTheDocument();
  });

  it('handles whole numbers correctly', () => {
    render(<PriceDisplay price={50} />);

    expect(screen.getByText('$50.00')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(
      <PriceDisplay price={49.99} className="text-red-500" />
    );

    expect(container.firstChild).toHaveClass('text-red-500');
  });

  it('formats large numbers correctly', () => {
    render(<PriceDisplay price={1299.99} />);

    expect(screen.getByText('$1,299.99')).toBeInTheDocument();
  });
});
```

**src/components/features/product/PriceDisplay/PriceDisplay.tsx:**
```typescript
import React from 'react';
import { formatCurrency } from '@/utils/formatters';
import type { PriceDisplayProps } from '@/types/product.types';

/**
 * PriceDisplay Component
 * Displays formatted price with currency symbol
 */
export const PriceDisplay: React.FC<PriceDisplayProps> = ({
  price,
  currency = 'USD',
  className = '',
}) => {
  const formattedPrice = formatCurrency(price, currency);

  return (
    <span className={`font-semibold text-gray-900 ${className}`}>
      {formattedPrice}
    </span>
  );
};
```

**src/components/features/product/PriceDisplay/index.ts:**
```typescript
export { PriceDisplay } from './PriceDisplay';
export type { PriceDisplayProps } from '@/types/product.types';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Uses formatCurrency utility from Phase 1
- [ ] Proper TypeScript typing
- [ ] Handles edge cases (zero, large numbers)
- [ ] Responsive styling

**Estimated Time:** 30 minutes

---

### Task 4.2: Create StockBadge Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Create a badge component to display product stock status. **Write tests first!**

**Files to Create:**

**src/components/features/product/StockBadge/StockBadge.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StockBadge } from './StockBadge';

describe('StockBadge', () => {
  it('shows "In Stock" when stock is available', () => {
    render(<StockBadge stock={10} />);

    expect(screen.getByText('In Stock')).toBeInTheDocument();
  });

  it('shows "Out of Stock" when stock is zero', () => {
    render(<StockBadge stock={0} />);

    expect(screen.getByText('Out of Stock')).toBeInTheDocument();
  });

  it('shows "Low Stock" when stock is less than 5', () => {
    render(<StockBadge stock={3} />);

    expect(screen.getByText('Low Stock')).toBeInTheDocument();
  });

  it('displays quantity when showQuantity is true', () => {
    render(<StockBadge stock={10} showQuantity />);

    expect(screen.getByText('10 Available')).toBeInTheDocument();
  });

  it('uses available_quantity if provided', () => {
    render(<StockBadge stock={25} available_quantity={12} showQuantity />);

    expect(screen.getByText('12 Available')).toBeInTheDocument();
  });

  it('applies correct color classes for in stock', () => {
    const { container } = render(<StockBadge stock={10} />);

    expect(container.querySelector('.bg-green-100')).toBeInTheDocument();
  });

  it('applies correct color classes for out of stock', () => {
    const { container } = render(<StockBadge stock={0} />);

    expect(container.querySelector('.bg-red-100')).toBeInTheDocument();
  });
});
```

**src/components/features/product/StockBadge/StockBadge.tsx:**
```typescript
import React from 'react';
import type { StockBadgeProps } from '@/types/product.types';

/**
 * StockBadge Component
 * Displays product stock status with color-coded badge
 */
export const StockBadge: React.FC<StockBadgeProps> = ({
  stock,
  available_quantity,
  showQuantity = false,
}) => {
  const quantity = available_quantity ?? stock;

  const getStockInfo = () => {
    if (quantity === 0) {
      return {
        label: 'Out of Stock',
        bgColor: 'bg-red-100',
        textColor: 'text-red-800',
      };
    }
    if (quantity < 5) {
      return {
        label: 'Low Stock',
        bgColor: 'bg-yellow-100',
        textColor: 'text-yellow-800',
      };
    }
    return {
      label: 'In Stock',
      bgColor: 'bg-green-100',
      textColor: 'text-green-800',
    };
  };

  const stockInfo = getStockInfo();
  const displayText = showQuantity && quantity > 0
    ? `${quantity} Available`
    : stockInfo.label;

  return (
    <span
      className={`
        inline-flex items-center px-2.5 py-0.5
        rounded-full text-xs font-medium
        ${stockInfo.bgColor} ${stockInfo.textColor}
      `}
    >
      {displayText}
    </span>
  );
};
```

**src/components/features/product/StockBadge/index.ts:**
```typescript
export { StockBadge } from './StockBadge';
export type { StockBadgeProps } from '@/types/product.types';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Shows correct status based on stock level
- [ ] Color coding works (green/yellow/red)
- [ ] Optional quantity display works
- [ ] Accessibility attributes included

**Estimated Time:** 45 minutes

---

### Task 4.3: Create ProductImage Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 4.2

**Description:**
Create an optimized image component for product images with lazy loading. **Write tests first!**

**Files to Create:**

**src/components/features/product/ProductImage/ProductImage.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductImage } from './ProductImage';

describe('ProductImage', () => {
  it('renders image with src and alt', () => {
    render(
      <ProductImage
        src="https://example.com/product.jpg"
        alt="Test Product"
      />
    );

    const img = screen.getByAltText('Test Product');
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute('src', 'https://example.com/product.jpg');
  });

  it('applies lazy loading attribute', () => {
    render(
      <ProductImage
        src="https://example.com/product.jpg"
        alt="Test Product"
      />
    );

    expect(screen.getByAltText('Test Product')).toHaveAttribute('loading', 'lazy');
  });

  it('applies custom className', () => {
    render(
      <ProductImage
        src="https://example.com/product.jpg"
        alt="Test Product"
        className="custom-class"
      />
    );

    expect(screen.getByAltText('Test Product')).toHaveClass('custom-class');
  });

  it('maintains aspect ratio', () => {
    const { container } = render(
      <ProductImage
        src="https://example.com/product.jpg"
        alt="Test Product"
      />
    );

    expect(container.querySelector('.aspect-square')).toBeInTheDocument();
  });

  it('renders placeholder for missing image', () => {
    render(
      <ProductImage
        src=""
        alt="Test Product"
      />
    );

    expect(screen.getByText('No Image')).toBeInTheDocument();
  });
});
```

**src/components/features/product/ProductImage/ProductImage.tsx:**
```typescript
import React, { useState } from 'react';
import type { ProductImageProps } from '@/types/product.types';

/**
 * ProductImage Component
 * Optimized image display with lazy loading and fallback
 */
export const ProductImage: React.FC<ProductImageProps> = ({
  src,
  alt,
  className = '',
}) => {
  const [imageError, setImageError] = useState(false);

  if (!src || imageError) {
    return (
      <div
        className={`
          aspect-square w-full
          bg-gray-200
          flex items-center justify-center
          rounded-lg
          ${className}
        `}
      >
        <span className="text-gray-500 text-sm">No Image</span>
      </div>
    );
  }

  return (
    <div className={`aspect-square w-full overflow-hidden rounded-lg ${className}`}>
      <img
        src={src}
        alt={alt}
        loading="lazy"
        onError={() => setImageError(true)}
        className="w-full h-full object-cover transition-transform duration-300 hover:scale-105"
      />
    </div>
  );
};
```

**src/components/features/product/ProductImage/index.ts:**
```typescript
export { ProductImage } from './ProductImage';
export type { ProductImageProps } from '@/types/product.types';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Lazy loading enabled
- [ ] Error handling with fallback UI
- [ ] Maintains aspect ratio
- [ ] Hover effect works
- [ ] Accessible alt text

**Estimated Time:** 45 minutes

---

### Task 4.4: Create ProductSkeleton Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 4.3

**Description:**
Create skeleton loading state component for product cards. **Write tests first!**

**Files to Create:**

**src/components/features/product/ProductSkeleton/ProductSkeleton.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { ProductSkeleton } from './ProductSkeleton';

describe('ProductSkeleton', () => {
  it('renders skeleton structure', () => {
    const { container } = render(<ProductSkeleton />);

    // Should have skeleton elements
    const skeletonElements = container.querySelectorAll('.animate-pulse');
    expect(skeletonElements.length).toBeGreaterThan(0);
  });

  it('renders image placeholder', () => {
    const { container } = render(<ProductSkeleton />);

    const imageSkeleton = container.querySelector('.aspect-square');
    expect(imageSkeleton).toBeInTheDocument();
  });

  it('renders text placeholders', () => {
    const { container } = render(<ProductSkeleton />);

    const textSkeletons = container.querySelectorAll('.h-4, .h-6');
    expect(textSkeletons.length).toBeGreaterThan(0);
  });

  it('applies custom className', () => {
    const { container } = render(<ProductSkeleton className="custom-skeleton" />);

    expect(container.firstChild).toHaveClass('custom-skeleton');
  });
});
```

**src/components/features/product/ProductSkeleton/ProductSkeleton.tsx:**
```typescript
import React from 'react';

interface ProductSkeletonProps {
  className?: string;
}

/**
 * ProductSkeleton Component
 * Loading skeleton for product card
 */
export const ProductSkeleton: React.FC<ProductSkeletonProps> = ({ className = '' }) => {
  return (
    <div className={`animate-pulse ${className}`}>
      {/* Image skeleton */}
      <div className="aspect-square w-full bg-gray-200 rounded-lg mb-3" />

      {/* Title skeleton */}
      <div className="h-6 bg-gray-200 rounded w-3/4 mb-2" />

      {/* Price skeleton */}
      <div className="h-4 bg-gray-200 rounded w-1/2 mb-2" />

      {/* Stock badge skeleton */}
      <div className="h-5 bg-gray-200 rounded w-1/3 mb-3" />

      {/* Button skeleton */}
      <div className="h-10 bg-gray-200 rounded w-full" />
    </div>
  );
};
```

**src/components/features/product/ProductSkeleton/index.ts:**
```typescript
export { ProductSkeleton } from './ProductSkeleton';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Skeleton matches product card layout
- [ ] Animation works
- [ ] Responsive design
- [ ] Custom className support

**Estimated Time:** 30 minutes

---

### Task 4.5: Create ProductCard Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 4.4

**Description:**
Create the main product card component that composes all sub-components. **Write tests first!**

**Files to Create:**

**src/components/features/product/ProductCard/ProductCard.test.tsx:**
```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductCard } from './ProductCard';
import type { Product } from '@/types/product.types';

const mockProduct: Product = {
  id: 'p123',
  name: 'Wireless Mouse',
  price: 49.99,
  stock: 12,
  image_url: 'https://example.com/mouse.jpg',
  published: true,
};

describe('ProductCard', () => {
  it('renders product information correctly', () => {
    render(<ProductCard product={mockProduct} />);

    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    expect(screen.getByText('$49.99')).toBeInTheDocument();
    expect(screen.getByText('In Stock')).toBeInTheDocument();
  });

  it('renders product image', () => {
    render(<ProductCard product={mockProduct} />);

    const img = screen.getByAltText('Wireless Mouse');
    expect(img).toHaveAttribute('src', 'https://example.com/mouse.jpg');
  });

  it('calls onAddToCart when Add to Cart button clicked', () => {
    const handleAddToCart = vi.fn();
    render(<ProductCard product={mockProduct} onAddToCart={handleAddToCart} />);

    fireEvent.click(screen.getByText('Add to Cart'));
    expect(handleAddToCart).toHaveBeenCalledWith('p123');
  });

  it('calls onViewDetails when card is clicked', () => {
    const handleViewDetails = vi.fn();
    render(<ProductCard product={mockProduct} onViewDetails={handleViewDetails} />);

    fireEvent.click(screen.getByText('Wireless Mouse'));
    expect(handleViewDetails).toHaveBeenCalledWith('p123');
  });

  it('disables Add to Cart button when out of stock', () => {
    const outOfStockProduct = { ...mockProduct, stock: 0 };
    render(<ProductCard product={outOfStockProduct} />);

    const button = screen.getByText('Out of Stock');
    expect(button).toBeDisabled();
  });

  it('applies hover effect', () => {
    const { container } = render(<ProductCard product={mockProduct} />);

    const card = container.querySelector('.hover\\:shadow-md');
    expect(card).toBeInTheDocument();
  });

  it('is keyboard accessible', () => {
    const handleViewDetails = vi.fn();
    render(<ProductCard product={mockProduct} onViewDetails={handleViewDetails} />);

    const card = screen.getByRole('article');
    fireEvent.keyDown(card, { key: 'Enter' });

    expect(handleViewDetails).toHaveBeenCalled();
  });
});
```

**src/components/features/product/ProductCard/ProductCard.tsx:**
```typescript
import React from 'react';
import { Card } from '@/components/ui/Card';
import { Button } from '@/components/ui/Button';
import { ProductImage } from '../ProductImage';
import { PriceDisplay } from '../PriceDisplay';
import { StockBadge } from '../StockBadge';
import type { ProductCardProps } from '@/types/product.types';

/**
 * ProductCard Component
 * Displays product information in a card layout
 */
export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onAddToCart,
  onViewDetails,
}) => {
  const handleCardClick = () => {
    onViewDetails?.(product.id);
  };

  const handleAddToCart = (e: React.MouseEvent) => {
    e.stopPropagation();
    onAddToCart?.(product.id);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      handleCardClick();
    }
  };

  const isOutOfStock = product.stock === 0;

  return (
    <Card
      hover
      className="cursor-pointer"
      onClick={handleCardClick}
      onKeyDown={handleKeyDown}
      tabIndex={0}
      role="article"
      aria-label={`Product: ${product.name}`}
    >
      {/* Product Image */}
      <ProductImage src={product.image_url} alt={product.name} />

      {/* Product Info */}
      <div className="mt-3">
        <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
          {product.name}
        </h3>

        <PriceDisplay price={product.price} className="text-xl mb-2" />

        <StockBadge stock={product.stock} className="mb-3" />

        <Button
          fullWidth
          variant={isOutOfStock ? 'secondary' : 'primary'}
          disabled={isOutOfStock}
          onClick={handleAddToCart}
          aria-label={`Add ${product.name} to cart`}
        >
          {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
        </Button>
      </div>
    </Card>
  );
};
```

**src/components/features/product/ProductCard/index.ts:**
```typescript
export { ProductCard } from './ProductCard';
export type { ProductCardProps } from '@/types/product.types';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Composes all sub-components correctly
- [ ] Event handlers work properly
- [ ] Keyboard accessible
- [ ] ARIA labels present
- [ ] Responsive design
- [ ] Hover states work

**Estimated Time:** 1 hour 30 minutes

---

## Testing & Validation

### Task 5.1: Integration Testing

**Status:** ⬜ Not Started
**Depends On:** Task 4.5

**Description:**
Create integration tests for the complete product flow with React Query.

**File to Create:**

**src/components/features/product/ProductCard/ProductCard.integration.test.tsx:**
```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ProductCard } from './ProductCard';
import { productService } from '@/api/services/product.service';

vi.mock('@/api/services/product.service');

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

describe('ProductCard Integration Tests', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    queryClient = createTestQueryClient();
    vi.clearAllMocks();
  });

  it('displays product data from React Query', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      price: 49.99,
      stock: 12,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProducts).mockResolvedValue([mockProduct]);

    render(
      <QueryClientProvider client={queryClient}>
        <ProductCard product={mockProduct} />
      </QueryClientProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    });
  });

  it('handles error states gracefully', async () => {
    vi.mocked(productService.getProducts).mockRejectedValue(
      new Error('Network error')
    );

    // Test error handling
  });
});
```

**Acceptance Criteria:**
- [ ] Integration tests pass
- [ ] Tests cover React Query integration
- [ ] Error scenarios tested
- [ ] Loading states tested

**Estimated Time:** 45 minutes

---

### Task 5.2: Manual Testing Checklist

**Status:** ⬜ Not Started
**Depends On:** Task 5.1

**Description:**
Manually verify all Phase 3a functionality.

**Testing Steps:**

1. **Dependencies:**
   - [ ] Run `npm install` - no errors
   - [ ] Run `npm run dev` - dev server starts
   - [ ] React Query DevTools visible
   - [ ] Run `npm run type-check` - no TypeScript errors
   - [ ] Run `npm run lint` - no critical errors

2. **Type Definitions:**
   - [ ] All product types compile
   - [ ] Types match API specification
   - [ ] No any types used

3. **API Service:**
   - [ ] Service tests pass (`npm test`)
   - [ ] getProducts returns data
   - [ ] getProduct returns detail
   - [ ] Error handling works

4. **React Query Hooks:**
   - [ ] useProducts hook works
   - [ ] useProduct hook works
   - [ ] Query keys correct
   - [ ] Caching works
   - [ ] Refetch on stale works

5. **Components:**
   - [ ] PriceDisplay formats correctly
   - [ ] StockBadge shows correct status
   - [ ] ProductImage loads and has fallback
   - [ ] ProductSkeleton animates
   - [ ] ProductCard displays all info
   - [ ] ProductCard is clickable
   - [ ] ProductCard is keyboard accessible

6. **Accessibility:**
   - [ ] All components have ARIA labels
   - [ ] Keyboard navigation works
   - [ ] Focus indicators visible
   - [ ] Screen reader compatible

**Estimated Time:** 1 hour

---

## Summary

### Total Tasks: 13
### Estimated Total Time: 12-16 hours (2-3 days)
### Priority: HIGH
### Prerequisites: Phase 1 must be completed first

### Deliverables Checklist:

**Setup:**
- [ ] React Query installed and configured
- [ ] Query client configured with defaults
- [ ] DevTools available

**Type Definitions:**
- [ ] Product types defined
- [ ] Types match API spec

**API Layer:**
- [ ] Product service implemented with tests
- [ ] React Query hooks created with tests
- [ ] Error handling works

**Components:**
- [ ] PriceDisplay component (TDD)
- [ ] StockBadge component (TDD)
- [ ] ProductImage component (TDD)
- [ ] ProductSkeleton component (TDD)
- [ ] ProductCard component (TDD)

**Testing:**
- [ ] All unit tests passing
- [ ] Integration tests passing
- [ ] Manual testing complete

---

## Next Steps

After completing Phase 3a:
1. Review all tasks and mark complete
2. Run full test suite: `npm test`
3. Create PR #1 with title: "feat(products): Add product catalog foundation with React Query"
4. Wait for PR approval
5. Proceed to Phase 3b: `phase-3b-product-catalog-pages.md`

---

**Document Created:** October 2025
**Phase Status:** Not Started
**Target Completion:** 2-3 days from start
**PR Number:** #1
