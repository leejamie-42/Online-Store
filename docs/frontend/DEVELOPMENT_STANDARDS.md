# Development Standards
## Online Store Application - Frontend

**Version:** 1.0
**Last Updated:** October 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Code Style](#code-style)
3. [Naming Conventions](#naming-conventions)
4. [File Organization](#file-organization)
5. [Component Guidelines](#component-guidelines)
6. [TypeScript Standards](#typescript-standards)
7. [Testing Standards](#testing-standards)
8. [Git Workflow](#git-workflow)
9. [Code Review Guidelines](#code-review-guidelines)
10. [Performance Guidelines](#performance-guidelines)

---

## Overview

This document establishes coding standards and best practices for the Online Store frontend application. Following these guidelines ensures code consistency, maintainability, and quality across the team.

### Core Principles

1. **Consistency** - Code should look like it was written by one person
2. **Readability** - Code should be easy to read and understand
3. **Maintainability** - Code should be easy to modify and extend
4. **Type Safety** - Leverage TypeScript for type safety
5. **Testing** - Write tests for critical functionality

---

## Code Style

### ESLint Configuration

```javascript
// eslint.config.js
import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules'] },
  {
    extends: [js.configs.recommended, ...tseslint.configs.recommended],
    files: ['**/*.{ts,tsx}'],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    plugins: {
      'react-hooks': reactHooks,
      'react-refresh': reactRefresh,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'react-refresh/only-export-components': [
        'warn',
        { allowConstantExport: true },
      ],
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_' },
      ],
      '@typescript-eslint/explicit-function-return-type': 'off',
      '@typescript-eslint/explicit-module-boundary-types': 'off',
      'no-console': ['warn', { allow: ['warn', 'error'] }],
    },
  },
);
```

### Prettier Configuration

```json
// .prettierrc
{
  "semi": true,
  "trailingComma": "es5",
  "singleQuote": true,
  "printWidth": 100,
  "tabWidth": 2,
  "useTabs": false,
  "arrowParens": "always",
  "endOfLine": "lf"
}
```

### Code Formatting

- **Indentation:** 2 spaces (no tabs)
- **Line Length:** Maximum 100 characters
- **Quotes:** Single quotes for strings, double quotes for JSX attributes
- **Semicolons:** Always use semicolons
- **Trailing Commas:** Use trailing commas in multi-line objects/arrays

**Example:**

```typescript
// Good
const user = {
  name: 'John Doe',
  email: 'john@example.com',
};

// Bad
const user = {
  name: "John Doe",
  email: "john@example.com"
}
```

---

## Naming Conventions

### Files and Folders

```typescript
// Components - PascalCase
Button.tsx
ProductCard.tsx
UserProfile.tsx

// Hooks - camelCase with 'use' prefix
useAuth.ts
useProducts.ts
useDebounce.ts

// Utils - camelCase
formatters.ts
validators.ts
helpers.ts

// Types - camelCase with '.types' suffix
user.types.ts
product.types.ts
api.types.ts

// Constants - camelCase or UPPER_CASE
constants.ts
API_ENDPOINTS.ts

// Tests - same name as file with '.test' suffix
Button.test.tsx
useAuth.test.ts
```

### Variables and Functions

```typescript
// Variables - camelCase
const userName = 'John Doe';
const isLoggedIn = true;
const cartItems = [];

// Constants - UPPER_SNAKE_CASE
const MAX_CART_ITEMS = 100;
const API_BASE_URL = 'http://localhost:8080';
const DEFAULT_PAGE_SIZE = 20;

// Functions - camelCase, verb prefix
function getUserData() {}
function handleSubmit() {}
function validateEmail() {}
function calculateTotal() {}

// Boolean variables - is/has/can prefix
const isLoading = true;
const hasError = false;
const canEdit = true;
const shouldUpdate = false;

// Event handlers - handle prefix
const handleClick = () => {};
const handleChange = () => {};
const handleSubmit = () => {};
```

### React Components

```typescript
// Component - PascalCase
const Button: React.FC<ButtonProps> = ({ children, onClick }) => {
  return <button onClick={onClick}>{children}</button>;
};

// Props interface - ComponentName + Props
interface ButtonProps {
  children: React.ReactNode;
  onClick: () => void;
  variant?: 'primary' | 'secondary';
}

// Event handlers in components
const ProductCard = ({ product }: ProductCardProps) => {
  const handleAddToCart = () => {
    // Handle add to cart
  };

  const handleViewDetails = () => {
    // Handle view details
  };

  return (
    <div>
      <button onClick={handleAddToCart}>Add to Cart</button>
      <button onClick={handleViewDetails}>View Details</button>
    </div>
  );
};
```

### Types and Interfaces

```typescript
// Interfaces - PascalCase
interface User {
  id: string;
  name: string;
  email: string;
}

interface ProductCardProps {
  product: Product;
  onAddToCart: (productId: string) => void;
}

// Types - PascalCase
type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED';

type ApiResponse<T> = {
  data: T;
  message: string;
};

// Enums - PascalCase
enum UserRole {
  Admin = 'ADMIN',
  Customer = 'CUSTOMER',
  Guest = 'GUEST',
}
```

---

## File Organization

### Component File Structure

Each component should have its own directory with the following structure:

```
ComponentName/
├── ComponentName.tsx       # Component implementation
├── ComponentName.test.tsx  # Component tests
├── ComponentName.stories.tsx  # Storybook stories (optional)
├── index.ts               # Export file
└── types.ts               # Component-specific types (if needed)
```

**Example:**

```typescript
// Button/Button.tsx
import React from 'react';
import { ButtonProps } from './types';

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  onClick,
  disabled = false,
  isLoading = false,
}) => {
  return (
    <button
      className={`btn btn-${variant} btn-${size}`}
      onClick={onClick}
      disabled={disabled || isLoading}
    >
      {isLoading ? 'Loading...' : children}
    </button>
  );
};

// Button/types.ts
export interface ButtonProps {
  children: React.ReactNode;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  onClick?: () => void;
  disabled?: boolean;
  isLoading?: boolean;
}

// Button/index.ts
export { Button } from './Button';
export type { ButtonProps } from './types';
```

### Import Order

Organize imports in the following order:

1. External libraries (React, third-party)
2. Internal aliases (@/)
3. Relative imports
4. Styles

```typescript
// 1. External libraries
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

// 2. Internal aliases
import { Button } from '@/components/ui/Button';
import { useAuth } from '@/hooks/useAuth';
import { formatCurrency } from '@/utils/formatters';
import type { Product } from '@/types/product.types';

// 3. Relative imports
import { ProductCard } from './ProductCard';
import { ProductFilters } from './ProductFilters';

// 4. Styles
import './ProductList.css';
```

---

## Component Guidelines

### Component Structure

Organize component code in the following order:

1. Imports
2. Type definitions
3. Component definition
4. Hooks
5. Event handlers
6. Effects
7. Render helpers
8. Return statement

```typescript
// 1. Imports
import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';

// 2. Type definitions
interface ProductListProps {
  category?: string;
}

// 3. Component definition
export const ProductList: React.FC<ProductListProps> = ({ category }) => {
  // 4. Hooks
  const [products, setProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const navigate = useNavigate();

  // 5. Event handlers
  const handleProductClick = (productId: string) => {
    navigate(`/products/${productId}`);
  };

  const handleAddToCart = (productId: string) => {
    // Add to cart logic
  };

  // 6. Effects
  useEffect(() => {
    const fetchProducts = async () => {
      setIsLoading(true);
      try {
        const data = await productService.getProducts({ category });
        setProducts(data.content);
      } catch (error) {
        console.error('Failed to fetch products:', error);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProducts();
  }, [category]);

  // 7. Render helpers
  const renderProduct = (product: Product) => (
    <ProductCard
      key={product.id}
      product={product}
      onClick={() => handleProductClick(product.id)}
      onAddToCart={() => handleAddToCart(product.id)}
    />
  );

  // 8. Return statement
  if (isLoading) return <LoadingSpinner />;
  if (!products.length) return <EmptyState />;

  return (
    <div className="product-list">
      {products.map(renderProduct)}
    </div>
  );
};
```

### Component Best Practices

#### 1. Keep Components Small and Focused

```typescript
// Good - Single responsibility
const UserAvatar: React.FC<UserAvatarProps> = ({ user }) => {
  return (
    <img
      src={user.avatar}
      alt={user.name}
      className="rounded-full w-10 h-10"
    />
  );
};

// Bad - Too many responsibilities
const UserProfile: React.FC = () => {
  // Fetches user data
  // Displays avatar
  // Shows user info
  // Handles editing
  // Manages settings
  // etc...
};
```

#### 2. Use Composition Over Props Drilling

```typescript
// Good - Composition
<Card>
  <CardHeader>
    <CardTitle>Product Name</CardTitle>
  </CardHeader>
  <CardContent>
    <ProductImage />
    <ProductPrice />
  </CardContent>
  <CardFooter>
    <Button>Add to Cart</Button>
  </CardFooter>
</Card>

// Bad - Props drilling
<Card
  title="Product Name"
  image={productImage}
  price={productPrice}
  buttonText="Add to Cart"
  onButtonClick={handleAddToCart}
/>
```

#### 3. Extract Complex Logic to Custom Hooks

```typescript
// Good - Custom hook
const useProductFilters = () => {
  const [filters, setFilters] = useState<ProductFilters>({});
  const [products, setProducts] = useState<Product[]>([]);

  const applyFilters = useCallback((newFilters: ProductFilters) => {
    setFilters(newFilters);
    // Fetch products with filters
  }, []);

  return { filters, products, applyFilters };
};

// Usage in component
const ProductList = () => {
  const { filters, products, applyFilters } = useProductFilters();

  return (
    <div>
      <ProductFilters onFilterChange={applyFilters} />
      <ProductGrid products={products} />
    </div>
  );
};
```

#### 4. Avoid Inline Functions in JSX

```typescript
// Good - Defined function
const ProductCard = ({ product }: ProductCardProps) => {
  const handleClick = () => {
    console.log('Product clicked:', product.id);
  };

  return <div onClick={handleClick}>{product.name}</div>;
};

// Bad - Inline function (creates new function on every render)
const ProductCard = ({ product }: ProductCardProps) => {
  return (
    <div onClick={() => console.log('Product clicked:', product.id)}>
      {product.name}
    </div>
  );
};
```

---

## TypeScript Standards

### Type Definitions

#### Always Define Types

```typescript
// Good
interface User {
  id: string;
  name: string;
  email: string;
}

const getUser = (userId: string): Promise<User> => {
  return apiClient.get(`/users/${userId}`);
};

// Bad
const getUser = (userId) => {
  return apiClient.get(`/users/${userId}`);
};
```

#### Use Interfaces for Objects, Types for Unions/Intersections

```typescript
// Interfaces for objects
interface Product {
  id: string;
  name: string;
  price: number;
}

// Types for unions
type OrderStatus = 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED';

// Types for intersections
type ProductWithReviews = Product & {
  reviews: Review[];
};
```

#### Avoid `any`, Use `unknown` When Needed

```typescript
// Good
const handleError = (error: unknown) => {
  if (error instanceof Error) {
    console.error(error.message);
  }
};

// Bad
const handleError = (error: any) => {
  console.error(error.message); // No type safety
};
```

#### Use Utility Types

```typescript
// Partial - Make all properties optional
type PartialProduct = Partial<Product>;

// Pick - Select specific properties
type ProductSummary = Pick<Product, 'id' | 'name' | 'price'>;

// Omit - Exclude specific properties
type ProductWithoutId = Omit<Product, 'id'>;

// Record - Key-value mapping
type ProductMap = Record<string, Product>;

// Required - Make all properties required
type RequiredProduct = Required<PartialProduct>;
```

---

## Testing Standards

### Unit Tests

```typescript
// Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders with children', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByText('Click me')).toBeInTheDocument();
  });

  it('calls onClick when clicked', () => {
    const handleClick = jest.fn();
    render(<Button onClick={handleClick}>Click me</Button>);

    fireEvent.click(screen.getByText('Click me'));
    expect(handleClick).toHaveBeenCalledTimes(1);
  });

  it('shows loading state', () => {
    render(<Button isLoading>Submit</Button>);

    expect(screen.getByText('Loading...')).toBeInTheDocument();
    expect(screen.getByRole('button')).toBeDisabled();
  });

  it('applies correct variant class', () => {
    render(<Button variant="secondary">Click me</Button>);

    expect(screen.getByRole('button')).toHaveClass('btn-secondary');
  });
});
```

### Integration Tests

```typescript
// ProductList.test.tsx
import { render, screen, waitFor } from '@testing-library/react';
import { ProductList } from './ProductList';
import { productService } from '@/api/services/product.service';

jest.mock('@/api/services/product.service');

describe('ProductList', () => {
  it('displays products after loading', async () => {
    const mockProducts = [
      { id: '1', name: 'Product 1', price: 10 },
      { id: '2', name: 'Product 2', price: 20 },
    ];

    (productService.getProducts as jest.Mock).mockResolvedValue({
      content: mockProducts,
    });

    render(<ProductList />);

    // Check loading state
    expect(screen.getByText('Loading...')).toBeInTheDocument();

    // Wait for products to load
    await waitFor(() => {
      expect(screen.getByText('Product 1')).toBeInTheDocument();
      expect(screen.getByText('Product 2')).toBeInTheDocument();
    });
  });

  it('displays error message on fetch failure', async () => {
    (productService.getProducts as jest.Mock).mockRejectedValue(
      new Error('Failed to fetch')
    );

    render(<ProductList />);

    await waitFor(() => {
      expect(screen.getByText('Error loading products')).toBeInTheDocument();
    });
  });
});
```

### Test Coverage Goals

- **Unit Tests:** 80%+ coverage
- **Integration Tests:** Critical user flows
- **E2E Tests:** Main user journeys

---

## Git Workflow

### Branch Naming

```bash
# Feature branches
feature/user-authentication
feature/product-search
feature/checkout-flow

# Bug fix branches
fix/cart-quantity-bug
fix/login-error

# Hotfix branches
hotfix/security-patch
hotfix/critical-crash

# Refactor branches
refactor/payment-service
refactor/component-structure
```

### Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
# Format
<type>(<scope>): <subject>

# Types
feat: Add new feature
fix: Fix a bug
docs: Documentation changes
style: Code style changes (formatting, etc.)
refactor: Code refactoring
test: Add or update tests
chore: Maintenance tasks

# Examples
feat(auth): add login functionality
fix(cart): resolve quantity update bug
docs(api): update API documentation
refactor(components): reorganize component structure
test(auth): add login form tests
```

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] No console errors

## Screenshots (if applicable)
Add screenshots here

## Related Issues
Closes #123
```

---

## Code Review Guidelines

### What to Look For

#### Code Quality
- [ ] Code is readable and well-structured
- [ ] Follows naming conventions
- [ ] No unnecessary complexity
- [ ] Proper error handling
- [ ] No console.log statements

#### TypeScript
- [ ] Proper type definitions
- [ ] No `any` types
- [ ] Types exported correctly

#### React
- [ ] Components are small and focused
- [ ] Proper use of hooks
- [ ] No unnecessary re-renders
- [ ] Props properly typed

#### Performance
- [ ] No unnecessary computations
- [ ] Proper use of useMemo/useCallback
- [ ] Images optimized
- [ ] Code splitting where appropriate

#### Testing
- [ ] Tests added for new functionality
- [ ] Tests are meaningful
- [ ] Edge cases covered

#### Accessibility
- [ ] Semantic HTML used
- [ ] ARIA labels where needed
- [ ] Keyboard navigation works
- [ ] Color contrast sufficient

---

## Performance Guidelines

### Optimization Techniques

#### 1. Code Splitting

```typescript
// Lazy load routes
import { lazy, Suspense } from 'react';

const ProductDetail = lazy(() => import('./pages/ProductDetail'));
const OrderHistory = lazy(() => import('./pages/OrderHistory'));

function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <Routes>
        <Route path="/products/:id" element={<ProductDetail />} />
        <Route path="/orders" element={<OrderHistory />} />
      </Routes>
    </Suspense>
  );
}
```

#### 2. Memoization

```typescript
// useMemo for expensive computations
const sortedProducts = useMemo(() => {
  return products.sort((a, b) => a.price - b.price);
}, [products]);

// useCallback for functions passed as props
const handleAddToCart = useCallback((productId: string) => {
  addToCart(productId);
}, [addToCart]);

// React.memo for components
export const ProductCard = memo<ProductCardProps>(({ product }) => {
  return <div>{product.name}</div>;
});
```

#### 3. Image Optimization

```typescript
// Lazy loading
<img src={product.image} alt={product.name} loading="lazy" />

// Responsive images
<img
  srcSet="
    image-320w.jpg 320w,
    image-640w.jpg 640w,
    image-1280w.jpg 1280w
  "
  sizes="(max-width: 640px) 100vw, 640px"
  src="image-640w.jpg"
  alt="Product"
/>
```

#### 4. Virtualization for Long Lists

```typescript
import { useVirtualizer } from '@tanstack/react-virtual';

const ProductList = ({ products }: ProductListProps) => {
  const parentRef = useRef<HTMLDivElement>(null);

  const virtualizer = useVirtualizer({
    count: products.length,
    getScrollElement: () => parentRef.current,
    estimateSize: () => 100,
  });

  return (
    <div ref={parentRef} style={{ height: '600px', overflow: 'auto' }}>
      <div
        style={{
          height: `${virtualizer.getTotalSize()}px`,
          position: 'relative',
        }}
      >
        {virtualizer.getVirtualItems().map((virtualItem) => (
          <div
            key={virtualItem.key}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '100%',
              height: `${virtualItem.size}px`,
              transform: `translateY(${virtualItem.start}px)`,
            }}
          >
            <ProductCard product={products[virtualItem.index]} />
          </div>
        ))}
      </div>
    </div>
  );
};
```

---

## Documentation

### Component Documentation

```typescript
/**
 * Button component for user interactions
 *
 * @example
 * ```tsx
 * <Button variant="primary" onClick={handleClick}>
 *   Click me
 * </Button>
 * ```
 */
export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  children,
  onClick,
}) => {
  // Implementation
};
```

### Complex Function Documentation

```typescript
/**
 * Calculates the total price including tax and shipping
 *
 * @param items - Array of cart items
 * @param taxRate - Tax rate as decimal (e.g., 0.1 for 10%)
 * @param shippingCost - Flat shipping cost
 * @returns Total price with tax and shipping
 *
 * @example
 * ```typescript
 * const total = calculateTotal(cartItems, 0.1, 15.00);
 * // Returns: subtotal + (subtotal * 0.1) + 15.00
 * ```
 */
export const calculateTotal = (
  items: CartItem[],
  taxRate: number,
  shippingCost: number
): number => {
  const subtotal = items.reduce((sum, item) => sum + item.price * item.quantity, 0);
  const tax = subtotal * taxRate;
  return subtotal + tax + shippingCost;
};
```

---

## Tools and Scripts

### Package Scripts

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "lint:fix": "eslint . --fix",
    "format": "prettier --write \"src/**/*.{ts,tsx,css,md}\"",
    "format:check": "prettier --check \"src/**/*.{ts,tsx,css,md}\"",
    "type-check": "tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage"
  }
}
```

### Pre-commit Hooks

```bash
# .husky/pre-commit
#!/bin/sh
. "$(dirname "$0")/_/husky.sh"

npm run lint
npm run type-check
npm run test
```

---

## Checklist for New Features

Before submitting a pull request:

- [ ] Code follows style guidelines
- [ ] TypeScript types properly defined
- [ ] Component is properly structured
- [ ] Tests added and passing
- [ ] Documentation updated
- [ ] No console errors
- [ ] Accessibility checked
- [ ] Performance optimized
- [ ] Code reviewed by peer
- [ ] Pull request description complete

---

**Document Maintained By:** Frontend Team Lead
**Last Reviewed:** October 2025
