# Phase 3b: Product Detail Page - Implementation Tasks
## Online Store Application - Frontend (PR #2)

**Phase Duration:** 1-2 days
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 2025

---

## Overview

This document contains detailed, actionable tasks for Phase 3b of the frontend implementation. Phase 3b builds on Phase 3a to create a complete product detail page with navigation from the home page using **Test-Driven Development (TDD)** methodology and **React Query** for server state management.

**This phase corresponds to PR #2 of the Product Catalog feature.**

**Prerequisites:** Phase 3a must be completed and merged first.

### Phase 3b Goals
- ✅ Implement navigation from Home page to Product Detail page
- ✅ Create Product Detail page with full product information
- ✅ Build product gallery component for images
- ✅ Create quantity selector for cart functionality
- ✅ Add breadcrumb navigation for better UX
- ✅ Optimize performance and accessibility

### API Alignment
This phase uses the following API endpoint from `docs/SYSTEM_INTERFACE_SPEC.md`:

**GET /api/products/{id}**
```json
{
  "id": "p123",
  "name": "Wireless Mouse",
  "description": "Ergonomic and precise",
  "price": 49.99,
  "stock": 25,
  "image_url": "https://cdn.com/images/wireless-mouse.png",
  "published": true
}
```

---

## Table of Contents

1. [Section 1: Navigation & Routing](#section-1-navigation--routing)
2. [Section 2: Product Detail Page Components](#section-2-product-detail-page-components-tdd)
3. [Section 3: Advanced Features](#section-3-advanced-features)
4. [Section 4: Testing & Validation](#section-4-testing--validation)

---

## Section 1: Navigation & Routing

**Estimated Time:** 1-1.5 hours
**Dependencies:** Phase 3a complete

### Task 1.1: Implement Home Page Navigation to Product Detail

**Status:** ⬜ Not Started
**Depends On:** Phase 3a complete

**Description:**
Update the Home page to navigate to Product Detail when a product card is clicked.

**Files to Modify:**

**frontend/src/pages/Home.tsx:**

Replace the TODO navigation logic with actual implementation:

```typescript
import { useNavigate } from 'react-router-dom';

export const Home: React.FC = () => {
  const navigate = useNavigate();
  const { data: products, isLoading, isError, error } = useProducts({ published: true });

  const handleProductClick = (product: Product) => {
    navigate(`/products/${product.id}`);
  };

  const handleAddToCart = (productId: string) => {
    console.log('Add to cart:', productId);
    // TODO: Implement cart functionality in Phase 4
  };

  // ... rest of component

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {/* ... header content ... */}

      {products && products.length > 0 ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {products.map((product) => (
            <ProductCard
              key={product.id}
              product={product}
              onClick={handleProductClick}  // Updated from TODO
              onAddToCart={handleAddToCart}
            />
          ))}
        </div>
      ) : (
        // ... empty state ...
      )}
    </div>
  );
};
```

**Test Updates:**

Add navigation test to **frontend/src/pages/Home.test.tsx:**

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { vi } from 'vitest';

const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  ...vi.importActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

describe('Home', () => {
  // ... existing tests ...

  it('navigates to product detail when product card clicked', async () => {
    const mockProducts = [
      {
        id: 'p123',
        name: 'Test Product',
        price: 49.99,
        stock: 10,
        image_url: 'http://example.com/test.jpg',
        published: true,
      },
    ];

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);

    renderWithProviders(<Home />);

    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument();
    });

    // Click on product card
    fireEvent.click(screen.getByText('Test Product'));

    expect(mockNavigate).toHaveBeenCalledWith('/products/p123');
  });
});
```

**Acceptance Criteria:**
- [ ] Home page navigation implemented
- [ ] TODO comment removed
- [ ] Navigation test added
- [ ] Test passes
- [ ] Clicking product card navigates to detail page

**Estimated Time:** 30 minutes

---

### Task 1.2: Configure Product Detail Route

**Status:** ⬜ Not Started
**Depends On:** Task 1.1

**Description:**
Add the Product Detail page route to the application router.

**Files to Modify:**

**frontend/src/App.tsx:**

Add Product Detail route:

```typescript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Home from '@/pages/Home';
import ProductDetail from '@/pages/Products/ProductDetail';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/products/:id" element={<ProductDetail />} />
          {/* Other routes */}
        </Routes>
      </div>
    </Router>
  );
}

export default App;
```

**Create placeholder page:**

**frontend/src/pages/Products/ProductDetail.tsx:**

```typescript
import React from 'react';
import { useParams } from 'react-router-dom';

/**
 * ProductDetail Page
 * Display detailed product information
 * (Will be fully implemented in Task 2.4)
 */
export const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1>Product Detail Page</h1>
      <p>Product ID: {id}</p>
      <p>(Full implementation in Task 2.4)</p>
    </div>
  );
};

export default ProductDetail;
```

**frontend/src/pages/Products/index.ts:**

```typescript
export { ProductDetail } from './ProductDetail';
export { default } from './ProductDetail';
```

**Acceptance Criteria:**
- [ ] Route configured in App.tsx
- [ ] Placeholder ProductDetail page created
- [ ] Navigation from Home to Product Detail works
- [ ] URL parameter `:id` is accessible
- [ ] Page renders without errors

**Estimated Time:** 30 minutes

---

## Section 2: Product Detail Page Components (TDD)

**Estimated Time:** 5-6 hours
**Dependencies:** Section 1 complete

### Task 2.1: Create ProductGallery Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 1.2

**Description:**
Create an image gallery component for product detail page. **Write tests first!**

**Files to Create:**

**frontend/src/components/features/product/ProductGallery/ProductGallery.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductGallery } from './ProductGallery';

describe('ProductGallery', () => {
  it('renders main image', () => {
    render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
      />
    );

    const img = screen.getByAltText('Product');
    expect(img).toHaveAttribute('src', 'https://example.com/main.jpg');
  });

  it('renders with aspect ratio container', () => {
    const { container } = render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
      />
    );

    expect(container.querySelector('.aspect-square')).toBeInTheDocument();
  });

  it('handles missing image gracefully', () => {
    render(<ProductGallery mainImage="" altText="Product" />);

    expect(screen.getByText('No Image')).toBeInTheDocument();
  });

  it('displays fallback on image error', () => {
    render(
      <ProductGallery
        mainImage="https://example.com/broken.jpg"
        altText="Product"
      />
    );

    const img = screen.getByAltText('Product') as HTMLImageElement;

    // Simulate image error
    img.dispatchEvent(new Event('error'));

    expect(screen.getByText('No Image')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
        className="custom-gallery"
      />
    );

    expect(container.firstChild).toHaveClass('custom-gallery');
  });
});
```

**frontend/src/components/features/product/ProductGallery/ProductGallery.tsx:**
```typescript
import React, { useState } from 'react';

interface ProductGalleryProps {
  mainImage: string;
  altText: string;
  className?: string;
}

/**
 * ProductGallery Component
 * Image gallery for product detail page
 */
export const ProductGallery: React.FC<ProductGalleryProps> = ({
  mainImage,
  altText,
  className = '',
}) => {
  const [imageError, setImageError] = useState(false);

  if (!mainImage || imageError) {
    return (
      <div
        className={`
          aspect-square w-full
          bg-gray-200
          flex items-center justify-center
          rounded-xl
          ${className}
        `}
      >
        <span className="text-gray-500 text-lg">No Image</span>
      </div>
    );
  }

  return (
    <div className={`aspect-square w-full overflow-hidden rounded-xl ${className}`}>
      <img
        src={mainImage}
        alt={altText}
        onError={() => setImageError(true)}
        className="w-full h-full object-cover"
      />
    </div>
  );
};
```

**frontend/src/components/features/product/ProductGallery/index.ts:**
```typescript
export { ProductGallery } from './ProductGallery';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Main image displays correctly
- [ ] Error handling works
- [ ] Aspect ratio maintained
- [ ] Fallback UI shown for missing/broken images

**Estimated Time:** 1 hour

---

### Task 2.2: Create ProductInfo Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Create component to display detailed product information using fields from the API spec. **Write tests first!**

**Files to Create:**

**frontend/src/components/features/product/ProductInfo/ProductInfo.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductInfo } from './ProductInfo';
import type { ProductDetail } from '@/types/product.types';

const mockProduct: ProductDetail = {
  id: 'p123',
  name: 'Wireless Mouse',
  description: 'Ergonomic and precise wireless mouse',
  price: 49.99,
  stock: 25,
  image_url: 'https://example.com/mouse.jpg',
  published: true,
};

describe('ProductInfo', () => {
  it('renders product name', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
  });

  it('renders product description', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText(/ergonomic and precise/i)).toBeInTheDocument();
  });

  it('renders product price', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText('$49.99')).toBeInTheDocument();
  });

  it('renders stock badge when in stock', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText(/in stock/i)).toBeInTheDocument();
  });

  it('renders out of stock badge when stock is 0', () => {
    const outOfStockProduct = { ...mockProduct, stock: 0 };
    render(<ProductInfo product={outOfStockProduct} />);

    expect(screen.getByText(/out of stock/i)).toBeInTheDocument();
  });

  it('renders product ID', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText(/SKU: p123/i)).toBeInTheDocument();
  });

  it('displays stock quantity', () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText(/25 available/i)).toBeInTheDocument();
  });
});
```

**frontend/src/components/features/product/ProductInfo/ProductInfo.tsx:**
```typescript
import React from 'react';
import { PriceDisplay } from '../PriceDisplay';
import { StockBadge } from '../StockBadge';
import type { ProductDetail } from '@/types/product.types';

interface ProductInfoProps {
  product: ProductDetail;
}

/**
 * ProductInfo Component
 * Detailed product information display
 */
export const ProductInfo: React.FC<ProductInfoProps> = ({ product }) => {
  return (
    <div className="space-y-4">
      <div>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">
          {product.name}
        </h1>
        <p className="text-sm text-gray-500">SKU: {product.id}</p>
      </div>

      <PriceDisplay price={product.price} className="text-3xl" />

      <StockBadge stock={product.stock} showQuantity />

      <div className="border-t border-gray-200 pt-4">
        <h2 className="text-lg font-semibold text-gray-900 mb-2">
          Description
        </h2>
        <p className="text-gray-700 leading-relaxed">
          {product.description}
        </p>
      </div>
    </div>
  );
};
```

**frontend/src/components/features/product/ProductInfo/index.ts:**
```typescript
export { ProductInfo } from './ProductInfo';
```

**Update Type Definition:**

**frontend/src/types/product.types.ts:**

Ensure ProductDetail type matches API spec (no `available_quantity` field):

```typescript
export interface ProductDetail {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  image_url: string;
  published: boolean;
}
```

**Update StockBadge Component:**

**frontend/src/components/features/product/StockBadge/StockBadge.tsx:**

Remove `available_quantity` prop, use `stock` instead:

```typescript
interface StockBadgeProps {
  stock: number;
  showQuantity?: boolean;
  className?: string;
}

export const StockBadge: React.FC<StockBadgeProps> = ({
  stock,
  showQuantity = false,
  className = '',
}) => {
  const isInStock = stock > 0;

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <span
        className={`
          px-3 py-1 rounded-full text-sm font-medium
          ${isInStock
            ? 'bg-green-100 text-green-800'
            : 'bg-red-100 text-red-800'
          }
        `}
      >
        {isInStock ? 'In Stock' : 'Out of Stock'}
      </span>
      {showQuantity && isInStock && (
        <span className="text-sm text-gray-600">
          {stock} available
        </span>
      )}
    </div>
  );
};
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] All product info displayed correctly
- [ ] API field alignment verified (using `stock`, not `available_quantity`)
- [ ] Proper typography hierarchy
- [ ] Responsive layout
- [ ] Stock badge shows correct status

**Estimated Time:** 1 hour 30 minutes

---

### Task 2.3: Create QuantitySelector Component (TDD)

**Status:** ⬜ Not Started
**Depends On:** Task 2.2

**Description:**
Create quantity selector for adding products to cart. **Write tests first!**

**Files to Create:**

**frontend/src/components/common/QuantitySelector/QuantitySelector.test.tsx:**
```typescript
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { QuantitySelector } from './QuantitySelector';

describe('QuantitySelector', () => {
  it('renders with initial quantity', () => {
    render(<QuantitySelector value={1} onChange={vi.fn()} />);

    expect(screen.getByDisplayValue('1')).toBeInTheDocument();
  });

  it('increments quantity when plus button clicked', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} max={10} />);

    fireEvent.click(screen.getByLabelText('Increase quantity'));
    expect(handleChange).toHaveBeenCalledWith(2);
  });

  it('decrements quantity when minus button clicked', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={2} onChange={handleChange} />);

    fireEvent.click(screen.getByLabelText('Decrease quantity'));
    expect(handleChange).toHaveBeenCalledWith(1);
  });

  it('does not decrement below min value', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} min={1} />);

    const decrementButton = screen.getByLabelText('Decrease quantity');
    expect(decrementButton).toBeDisabled();
  });

  it('does not increment above max value', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={10} onChange={handleChange} max={10} />);

    const incrementButton = screen.getByLabelText('Increase quantity');
    expect(incrementButton).toBeDisabled();
  });

  it('allows direct input of quantity', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} />);

    const input = screen.getByDisplayValue('1');
    fireEvent.change(input, { target: { value: '5' } });

    expect(handleChange).toHaveBeenCalledWith(5);
  });

  it('clamps invalid input to max value', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} max={10} />);

    const input = screen.getByDisplayValue('1');
    fireEvent.change(input, { target: { value: '20' } });

    expect(handleChange).toHaveBeenCalledWith(10);
  });

  it('clamps invalid input to min value', () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={5} onChange={handleChange} min={1} />);

    const input = screen.getByDisplayValue('5');
    fireEvent.change(input, { target: { value: '0' } });

    expect(handleChange).toHaveBeenCalledWith(1);
  });

  it('is keyboard accessible', () => {
    render(<QuantitySelector value={1} onChange={vi.fn()} />);

    const input = screen.getByDisplayValue('1');
    expect(input).toHaveAttribute('type', 'number');
    expect(input).toHaveAttribute('aria-label', 'Quantity');
  });
});
```

**frontend/src/components/common/QuantitySelector/QuantitySelector.tsx:**
```typescript
import React from 'react';
import { LuMinus, LuPlus } from 'react-icons/lu';

interface QuantitySelectorProps {
  value: number;
  onChange: (quantity: number) => void;
  min?: number;
  max?: number;
  className?: string;
}

/**
 * QuantitySelector Component
 * Input for selecting product quantity
 */
export const QuantitySelector: React.FC<QuantitySelectorProps> = ({
  value,
  onChange,
  min = 1,
  max = 99,
  className = '',
}) => {
  const handleDecrement = () => {
    if (value > min) {
      onChange(value - 1);
    }
  };

  const handleIncrement = () => {
    if (value < max) {
      onChange(value + 1);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = parseInt(e.target.value);
    if (isNaN(newValue)) return;

    // Clamp value between min and max
    const clampedValue = Math.min(Math.max(newValue, min), max);
    onChange(clampedValue);
  };

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <button
        type="button"
        onClick={handleDecrement}
        disabled={value <= min}
        className="
          p-2 border border-gray-300 rounded-lg
          hover:bg-gray-50
          disabled:opacity-50 disabled:cursor-not-allowed
        "
        aria-label="Decrease quantity"
      >
        <LuMinus className="w-4 h-4" />
      </button>

      <input
        type="number"
        value={value}
        onChange={handleInputChange}
        min={min}
        max={max}
        className="
          w-16 px-3 py-2
          text-center
          border border-gray-300 rounded-lg
          focus:ring-2 focus:ring-primary-500 focus:border-transparent
        "
        aria-label="Quantity"
      />

      <button
        type="button"
        onClick={handleIncrement}
        disabled={value >= max}
        className="
          p-2 border border-gray-300 rounded-lg
          hover:bg-gray-50
          disabled:opacity-50 disabled:cursor-not-allowed
        "
        aria-label="Increase quantity"
      >
        <LuPlus className="w-4 h-4" />
      </button>
    </div>
  );
};
```

**frontend/src/components/common/QuantitySelector/index.ts:**
```typescript
export { QuantitySelector } from './QuantitySelector';
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Component implemented and tests pass
- [ ] Increment/decrement work
- [ ] Min/max constraints enforced
- [ ] Direct input works
- [ ] Input validation clamps values
- [ ] Accessible controls with ARIA labels

**Estimated Time:** 1 hour 30 minutes

---

### Task 2.4: Create ProductDetail Page Integration (TDD)

**Status:** ⬜ Not Started
**Depends On:** Tasks 2.1, 2.2, 2.3

**Description:**
Complete the product detail page integration with all components using API spec fields. **Write tests first!**

**Files to Modify:**

**frontend/src/pages/Products/ProductDetail.test.tsx:**
```typescript
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { ProductDetail } from './ProductDetail';
import { productService } from '@/api/services/product.service';

vi.mock('@/api/services/product.service');

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

const renderWithProviders = (initialRoute = '/products/p123') => {
  const queryClient = createTestQueryClient();
  window.history.pushState({}, '', initialRoute);

  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/products/:id" element={<ProductDetail />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
};

describe('ProductDetail', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays loading state initially', () => {
    vi.mocked(productService.getProduct).mockImplementation(
      () => new Promise(() => {})
    );

    renderWithProviders();

    expect(screen.getByText(/loading product details/i)).toBeInTheDocument();
  });

  it('displays product details after loading', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic wireless mouse',
      price: 49.99,
      stock: 25,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    });

    expect(screen.getByText(/ergonomic wireless mouse/i)).toBeInTheDocument();
    expect(screen.getByText('$49.99')).toBeInTheDocument();
  });

  it('displays error message on fetch failure', async () => {
    vi.mocked(productService.getProduct).mockRejectedValue(
      new Error('Product not found')
    );

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText(/error loading product/i)).toBeInTheDocument();
    });
  });

  it('renders Add to Cart button when in stock', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 25,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('Add to Cart')).toBeInTheDocument();
    });
  });

  it('disables Add to Cart when out of stock', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 0,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      const button = screen.getByText('Out of Stock');
      expect(button).toBeDisabled();
    });
  });

  it('renders quantity selector when in stock', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 25,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByLabelText('Quantity')).toBeInTheDocument();
    });
  });

  it('does not render quantity selector when out of stock', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 0,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    });

    expect(screen.queryByLabelText('Quantity')).not.toBeInTheDocument();
  });

  it('renders back button', async () => {
    const mockProduct = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic',
      price: 49.99,
      stock: 25,
      image_url: 'https://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    renderWithProviders();

    await waitFor(() => {
      expect(screen.getByText(/back to products/i)).toBeInTheDocument();
    });
  });
});
```

**frontend/src/pages/Products/ProductDetail.tsx:**

Replace placeholder with full implementation:

```typescript
import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useProduct } from '@/hooks/useProducts';
import { ProductGallery } from '@/components/features/product/ProductGallery';
import { ProductInfo } from '@/components/features/product/ProductInfo';
import { QuantitySelector } from '@/components/common/QuantitySelector';
import { Button } from '@/components/ui/Button';
import { Spinner } from '@/components/ui/Spinner';
import { LuArrowLeft } from 'react-icons/lu';

/**
 * ProductDetail Page
 * Display detailed product information
 */
export const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [quantity, setQuantity] = useState(1);

  const { data: product, isLoading, isError, error } = useProduct(id || '');

  const handleAddToCart = () => {
    console.log('Add to cart:', id, 'quantity:', quantity);
    // TODO: Implement cart functionality in Phase 4
  };

  const handleBackToProducts = () => {
    navigate('/');
  };

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <Spinner size="lg" />
            <p className="text-gray-600 mt-4">Loading product details...</p>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !product) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <p className="text-red-800">
            Error loading product: {error?.message || 'Product not found'}
          </p>
          <Button onClick={handleBackToProducts} variant="outline" className="mt-4">
            Back to Products
          </Button>
        </div>
      </div>
    );
  }

  const isOutOfStock = product.stock === 0;
  const maxQuantity = Math.min(product.stock, 99);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back Button */}
      <button
        onClick={handleBackToProducts}
        className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-6"
      >
        <LuArrowLeft className="w-5 h-5" />
        Back to Products
      </button>

      {/* Product Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Product Gallery */}
        <div>
          <ProductGallery
            mainImage={product.image_url}
            altText={product.name}
          />
        </div>

        {/* Product Info */}
        <div>
          <ProductInfo product={product} />

          {/* Purchase Section */}
          <div className="mt-8 space-y-4">
            {!isOutOfStock && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  Quantity
                </label>
                <QuantitySelector
                  value={quantity}
                  onChange={setQuantity}
                  max={maxQuantity}
                />
              </div>
            )}

            <Button
              fullWidth
              size="lg"
              variant={isOutOfStock ? 'secondary' : 'primary'}
              disabled={isOutOfStock}
              onClick={handleAddToCart}
            >
              {isOutOfStock ? 'Out of Stock' : 'Add to Cart'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
```

**Add useProduct hook:**

**frontend/src/hooks/useProducts.ts:**

Add single product query hook:

```typescript
import { useQuery } from '@tanstack/react-query';
import { productService } from '@/api/services/product.service';

// ... existing useProducts hook ...

/**
 * Hook for fetching single product by ID
 */
export const useProduct = (id: string) => {
  return useQuery({
    queryKey: ['products', id],
    queryFn: () => productService.getProduct(id),
    enabled: !!id, // Only fetch if ID is provided
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};
```

**Acceptance Criteria:**
- [ ] Tests written first and fail
- [ ] Page fully implemented and tests pass
- [ ] All components integrated correctly
- [ ] Uses `stock` field from API (not `available_quantity`)
- [ ] Loading states shown
- [ ] Error states handled
- [ ] Quantity selector works
- [ ] Add to cart button works
- [ ] Back navigation works
- [ ] Out of stock handling correct

**Estimated Time:** 2 hours

---

## Section 3: Advanced Features

**Estimated Time:** 45 minutes
**Dependencies:** Section 2 complete

### Task 3.1: Add Breadcrumb Navigation

**Status:** ⬜ Not Started
**Depends On:** Task 2.4

**Description:**
Add breadcrumb navigation to Product Detail page for better user experience.

**Files to Create:**

**frontend/src/components/common/Breadcrumb/Breadcrumb.test.tsx:**
```typescript
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { Breadcrumb } from './Breadcrumb';

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe('Breadcrumb', () => {
  it('renders all breadcrumb items', () => {
    const items = [
      { label: 'Home', path: '/' },
      { label: 'Products', path: '/products' },
      { label: 'Wireless Mouse' },
    ];

    renderWithRouter(<Breadcrumb items={items} />);

    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Products')).toBeInTheDocument();
    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
  });

  it('renders separators between items', () => {
    const items = [
      { label: 'Home', path: '/' },
      { label: 'Products' },
    ];

    const { container } = renderWithRouter(<Breadcrumb items={items} />);
    const separators = container.querySelectorAll('svg');

    expect(separators.length).toBeGreaterThan(0);
  });

  it('last item is not a link', () => {
    const items = [
      { label: 'Home', path: '/' },
      { label: 'Current Page' },
    ];

    renderWithRouter(<Breadcrumb items={items} />);

    const currentPage = screen.getByText('Current Page');
    expect(currentPage.tagName).not.toBe('A');
  });

  it('has proper ARIA attributes', () => {
    const items = [
      { label: 'Home', path: '/' },
      { label: 'Products' },
    ];

    const { container } = renderWithRouter(<Breadcrumb items={items} />);
    const nav = container.querySelector('nav');

    expect(nav).toHaveAttribute('aria-label', 'Breadcrumb');
  });
});
```

**frontend/src/components/common/Breadcrumb/Breadcrumb.tsx:**
```typescript
import React from 'react';
import { Link } from 'react-router-dom';
import { LuChevronRight } from 'react-icons/lu';

interface BreadcrumbItem {
  label: string;
  path?: string;
}

interface BreadcrumbProps {
  items: BreadcrumbItem[];
  className?: string;
}

/**
 * Breadcrumb Component
 * Navigation breadcrumb trail
 */
export const Breadcrumb: React.FC<BreadcrumbProps> = ({
  items,
  className = '',
}) => {
  return (
    <nav aria-label="Breadcrumb" className={className}>
      <ol className="flex items-center space-x-2 text-sm">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;

          return (
            <li key={index} className="flex items-center">
              {!isLast && item.path ? (
                <>
                  <Link
                    to={item.path}
                    className="text-gray-500 hover:text-gray-700"
                  >
                    {item.label}
                  </Link>
                  <LuChevronRight className="w-4 h-4 mx-2 text-gray-400" />
                </>
              ) : (
                <span className="text-gray-900 font-medium">
                  {item.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};
```

**frontend/src/components/common/Breadcrumb/index.ts:**
```typescript
export { Breadcrumb } from './Breadcrumb';
```

**Update ProductDetail page:**

**frontend/src/pages/Products/ProductDetail.tsx:**

Add breadcrumb after back button:

```typescript
import { Breadcrumb } from '@/components/common/Breadcrumb';

// ... inside component render ...

return (
  <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    {/* Back Button */}
    <button
      onClick={handleBackToProducts}
      className="flex items-center gap-2 text-gray-600 hover:text-gray-900 mb-4"
    >
      <LuArrowLeft className="w-5 h-5" />
      Back to Products
    </button>

    {/* Breadcrumb */}
    <Breadcrumb
      items={[
        { label: 'Home', path: '/' },
        { label: product.name },
      ]}
      className="mb-6"
    />

    {/* ... rest of component ... */}
  </div>
);
```

**Acceptance Criteria:**
- [ ] Breadcrumb component created with tests
- [ ] Tests pass
- [ ] Breadcrumb added to Product Detail page
- [ ] Proper navigation links work
- [ ] ARIA labels present
- [ ] Visual separators between items

**Estimated Time:** 45 minutes

---

## Section 4: Testing & Validation

**Estimated Time:** 1-1.5 hours
**Dependencies:** All previous tasks

### Task 4.1: E2E Tests for Product Detail Flow

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Create end-to-end tests for the complete product detail flow.

**File to Create:**

**frontend/src/e2e/product-detail-flow.test.tsx:**
```typescript
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter } from 'react-router-dom';
import App from '@/App';
import { productService } from '@/api/services/product.service';

vi.mock('@/api/services/product.service');

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

describe('Product Detail E2E Flow', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('complete product detail browsing flow', async () => {
    const mockProducts = [
      {
        id: 'p123',
        name: 'Wireless Mouse',
        price: 49.99,
        stock: 25,
        image_url: 'http://example.com/mouse.jpg',
        published: true,
      },
    ];

    const mockProductDetail = {
      id: 'p123',
      name: 'Wireless Mouse',
      description: 'Ergonomic and precise wireless mouse',
      price: 49.99,
      stock: 25,
      image_url: 'http://example.com/mouse.jpg',
      published: true,
    };

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);
    vi.mocked(productService.getProduct).mockResolvedValue(mockProductDetail);

    const queryClient = createTestQueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    );

    // 1. Wait for home page to load
    await waitFor(() => {
      expect(screen.getByText('Featured Products')).toBeInTheDocument();
    });

    // 2. Product card should be visible
    await waitFor(() => {
      expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    });

    // 3. Click on product card to navigate to detail page
    fireEvent.click(screen.getByText('Wireless Mouse'));

    // 4. Wait for product detail page to load
    await waitFor(() => {
      expect(screen.getByText('SKU: p123')).toBeInTheDocument();
    });

    // 5. Verify product information is displayed
    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    expect(screen.getByText(/ergonomic and precise/i)).toBeInTheDocument();
    expect(screen.getByText('$49.99')).toBeInTheDocument();
    expect(screen.getByText(/25 available/i)).toBeInTheDocument();

    // 6. Verify quantity selector is present
    expect(screen.getByLabelText('Quantity')).toBeInTheDocument();

    // 7. Verify Add to Cart button is present and enabled
    const addToCartButton = screen.getByText('Add to Cart');
    expect(addToCartButton).toBeInTheDocument();
    expect(addToCartButton).not.toBeDisabled();

    // 8. Test quantity selector
    const incrementButton = screen.getByLabelText('Increase quantity');
    fireEvent.click(incrementButton);
    expect(screen.getByDisplayValue('2')).toBeInTheDocument();

    // 9. Verify back navigation
    const backButton = screen.getByText(/back to products/i);
    fireEvent.click(backButton);

    await waitFor(() => {
      expect(screen.getByText('Featured Products')).toBeInTheDocument();
    });
  });

  it('handles out of stock products correctly', async () => {
    const mockProducts = [
      {
        id: 'p456',
        name: 'Out of Stock Item',
        price: 99.99,
        stock: 0,
        image_url: 'http://example.com/item.jpg',
        published: true,
      },
    ];

    const mockProductDetail = {
      id: 'p456',
      name: 'Out of Stock Item',
      description: 'This item is currently unavailable',
      price: 99.99,
      stock: 0,
      image_url: 'http://example.com/item.jpg',
      published: true,
    };

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);
    vi.mocked(productService.getProduct).mockResolvedValue(mockProductDetail);

    const queryClient = createTestQueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    );

    // Navigate to detail page
    await waitFor(() => {
      expect(screen.getByText('Out of Stock Item')).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText('Out of Stock Item'));

    // Verify out of stock state
    await waitFor(() => {
      expect(screen.getByText('Out of Stock')).toBeInTheDocument();
    });

    // Quantity selector should not be present
    expect(screen.queryByLabelText('Quantity')).not.toBeInTheDocument();

    // Add to Cart button should be disabled
    const outOfStockButton = screen.getByText('Out of Stock');
    expect(outOfStockButton).toBeDisabled();
  });

  it('handles product fetch errors gracefully', async () => {
    const mockProducts = [
      {
        id: 'p789',
        name: 'Test Product',
        price: 29.99,
        stock: 10,
        image_url: 'http://example.com/test.jpg',
        published: true,
      },
    ];

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);
    vi.mocked(productService.getProduct).mockRejectedValue(
      new Error('Product not found')
    );

    const queryClient = createTestQueryClient();
    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </QueryClientProvider>
    );

    // Navigate to detail page
    await waitFor(() => {
      expect(screen.getByText('Test Product')).toBeInTheDocument();
    });
    fireEvent.click(screen.getByText('Test Product'));

    // Verify error state
    await waitFor(() => {
      expect(screen.getByText(/error loading product/i)).toBeInTheDocument();
      expect(screen.getByText(/product not found/i)).toBeInTheDocument();
    });

    // Back button should be present in error state
    expect(screen.getByText(/back to products/i)).toBeInTheDocument();
  });
});
```

**Run tests:**
```bash
npm run test -- product-detail-flow.test.tsx
```

**Acceptance Criteria:**
- [ ] E2E test file created
- [ ] Complete flow test passes
- [ ] Out of stock handling tested
- [ ] Error handling tested
- [ ] Navigation tested
- [ ] All edge cases covered
- [ ] Tests run consistently

**Estimated Time:** 1 hour 30 minutes

---

## Summary

### Total Tasks: 8
### Estimated Total Time: 8-10 hours (1-2 days)
### Priority: HIGH
### Prerequisites: Phase 3a must be completed and merged

### Deliverables Checklist:

**Navigation & Routing:**
- [ ] Home page navigation to Product Detail
- [ ] Product Detail route configured

**Product Detail Page Components:**
- [ ] ProductGallery component (TDD)
- [ ] ProductInfo component (TDD) - API aligned
- [ ] QuantitySelector component (TDD)
- [ ] ProductDetail page integration (TDD)

**Advanced Features:**
- [ ] Breadcrumb navigation

**Testing:**
- [ ] E2E tests for complete product detail flow

**API Alignment:**
- [ ] All components use correct API fields (`stock` not `available_quantity`)
- [ ] Type definitions match API specification
- [ ] StockBadge component updated

---

## Next Steps

After completing Phase 3b:
1. Review all tasks and mark complete
2. Run full test suite: `npm test`
3. Run type check: `npm run type-check`
4. Run build: `npm run build`
5. Test product detail flow manually
6. Test navigation from Home to Product Detail
7. Verify API field alignment
8. Create PR #2 with title: "feat(products): Add product detail page with navigation"
9. Wait for PR approval and merge
10. Proceed to Phase 4: Shopping Cart & Checkout

---

## Technical Notes

### API Field Usage
- Use `stock` field for inventory (NOT `available_quantity` which doesn't exist in API)
- Product Detail endpoint: `GET /api/products/{id}`
- All fields from API spec: `id`, `name`, `description`, `price`, `stock`, `image_url`, `published`

### Navigation Flow
- Home page → Click product card → Product Detail page (`/products/:id`)
- Product Detail → Click back button → Home page (`/`)
- Breadcrumb: Home → Product Name

### Component Structure
```
ProductDetail Page
├── Breadcrumb (Home / Product Name)
├── Back Button
└── Grid Layout
    ├── ProductGallery (image display)
    └── Product Info Section
        ├── ProductInfo (name, SKU, price, stock, description)
        ├── QuantitySelector (if in stock)
        └── Add to Cart Button
```

---

**Document Created:** October 2025
**Phase Status:** Not Started
**Target Completion:** 1-2 days from start
**PR Number:** #2
