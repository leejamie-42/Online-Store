# Frontend Architecture
## Online Store Application

**Version:** 1.0
**Last Updated:** October 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Project Structure](#project-structure)
3. [Component Architecture](#component-architecture)
4. [State Management](#state-management)
5. [Routing Architecture](#routing-architecture)
6. [Data Flow](#data-flow)
7. [Key Patterns](#key-patterns)

---

## Overview

This document describes the architecture and organization of the frontend application. The application follows a component-based architecture using React with TypeScript, emphasizing modularity, reusability, and maintainability.

### Architectural Principles

1. **Separation of Concerns** - Clear separation between UI, business logic, and data
2. **Component Composition** - Building complex UIs from simple, reusable components
3. **Unidirectional Data Flow** - Predictable state management
4. **Type Safety** - Comprehensive TypeScript usage
5. **Code Reusability** - DRY principle applied throughout

---

## Project Structure

```
frontend/
├── public/                      # Static assets
│   ├── images/
│   ├── fonts/
│   └── favicon.ico
│
├── src/
│   ├── api/                     # API integration layer
│   │   ├── client.ts           # Axios instance with interceptors
│   │   ├── endpoints.ts        # API endpoint constants
│   │   └── services/           # Service modules
│   │       ├── auth.service.ts
│   │       ├── product.service.ts
│   │       ├── order.service.ts
│   │       └── user.service.ts
│   │
│   ├── assets/                  # Images, icons, etc.
│   │   ├── images/
│   │   ├── icons/
│   │   └── logos/
│   │
│   ├── components/              # Reusable components
│   │   ├── ui/                 # Base UI components
│   │   │   ├── Button/
│   │   │   │   ├── Button.tsx
│   │   │   │   ├── Button.test.tsx
│   │   │   │   └── index.ts
│   │   │   ├── Input/
│   │   │   ├── Card/
│   │   │   ├── Modal/
│   │   │   ├── Badge/
│   │   │   ├── Spinner/
│   │   │   └── ...
│   │   │
│   │   ├── layout/             # Layout components
│   │   │   ├── Header/
│   │   │   ├── Footer/
│   │   │   ├── Sidebar/
│   │   │   ├── MainLayout/
│   │   │   └── AuthLayout/
│   │   │
│   │   ├── features/           # Feature-specific components
│   │   │   ├── product/
│   │   │   │   ├── ProductCard/
│   │   │   │   ├── ProductGrid/
│   │   │   │   ├── ProductFilters/
│   │   │   │   ├── ProductGallery/
│   │   │   │   └── ProductReviews/
│   │   │   │
│   │   │   ├── cart/
│   │   │   │   ├── CartItem/
│   │   │   │   ├── CartSummary/
│   │   │   │   ├── CartDrawer/
│   │   │   │   └── EmptyCart/
│   │   │   │
│   │   │   ├── order/
│   │   │   │   ├── OrderCard/
│   │   │   │   ├── OrderTimeline/
│   │   │   │   ├── OrderSummary/
│   │   │   │   └── CancelOrderModal/
│   │   │   │
│   │   │   └── auth/
│   │   │       ├── LoginForm/
│   │   │       ├── RegisterForm/
│   │   │       └── PasswordResetForm/
│   │   │
│   │   └── common/             # Common shared components
│   │       ├── SearchBar/
│   │       ├── Pagination/
│   │       ├── ErrorBoundary/
│   │       ├── LoadingState/
│   │       └── EmptyState/
│   │
│   ├── pages/                   # Page components (route containers)
│   │   ├── Home/
│   │   │   ├── Home.tsx
│   │   │   ├── Home.test.tsx
│   │   │   └── index.ts
│   │   ├── Auth/
│   │   │   ├── Login.tsx
│   │   │   ├── Register.tsx
│   │   │   └── ForgotPassword.tsx
│   │   ├── Products/
│   │   │   ├── ProductList.tsx
│   │   │   └── ProductDetail.tsx
│   │   ├── Cart/
│   │   │   └── Cart.tsx
│   │   ├── Checkout/
│   │   │   ├── Checkout.tsx
│   │   │   └── OrderConfirmation.tsx
│   │   ├── Orders/
│   │   │   ├── OrderHistory.tsx
│   │   │   └── OrderDetail.tsx
│   │   ├── Profile/
│   │   │   ├── Profile.tsx
│   │   │   ├── EditProfile.tsx
│   │   │   └── AddressManagement.tsx
│   │   └── NotFound/
│   │       └── NotFound.tsx
│   │
│   ├── hooks/                   # Custom React hooks
│   │   ├── useAuth.ts
│   │   ├── useCart.ts
│   │   ├── useProducts.ts
│   │   ├── useOrders.ts
│   │   ├── useDebounce.ts
│   │   ├── useLocalStorage.ts
│   │   ├── useMediaQuery.ts
│   │   └── usePagination.ts
│   │
│   ├── context/                 # React Context providers
│   │   ├── AuthContext.tsx
│   │   ├── CartContext.tsx
│   │   ├── ThemeContext.tsx
│   │   └── NotificationContext.tsx
│   │
│   ├── store/                   # State management (if using Zustand/Redux)
│   │   ├── slices/
│   │   │   ├── authSlice.ts
│   │   │   ├── cartSlice.ts
│   │   │   └── productSlice.ts
│   │   └── index.ts
│   │
│   ├── types/                   # TypeScript type definitions
│   │   ├── api.types.ts
│   │   ├── product.types.ts
│   │   ├── order.types.ts
│   │   ├── user.types.ts
│   │   ├── cart.types.ts
│   │   └── common.types.ts
│   │
│   ├── utils/                   # Utility functions
│   │   ├── formatters.ts       # Date, currency, etc.
│   │   ├── validators.ts       # Validation helpers
│   │   ├── storage.ts          # LocalStorage helpers
│   │   ├── constants.ts        # App constants
│   │   └── helpers.ts          # General helpers
│   │
│   ├── styles/                  # Global styles
│   │   ├── globals.css
│   │   ├── tailwind.css
│   │   └── variables.css
│   │
│   ├── config/                  # Configuration files
│   │   ├── routes.ts           # Route configuration
│   │   ├── api.config.ts       # API configuration
│   │   └── app.config.ts       # App configuration
│   │
│   ├── lib/                     # Third-party library configurations
│   │   ├── axios.ts
│   │   └── queryClient.ts      # React Query config
│   │
│   ├── App.tsx                  # Root component
│   ├── main.tsx                 # Entry point
│   ├── router.tsx               # Route definitions
│   └── vite-env.d.ts
│
├── .env                         # Environment variables
├── .env.example
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── vite.config.ts
├── tailwind.config.js
└── postcss.config.js
```

---

## Component Architecture

### Component Hierarchy

```
App
├── Router
│   ├── MainLayout
│   │   ├── Header
│   │   │   ├── Logo
│   │   │   ├── Navigation
│   │   │   ├── SearchBar
│   │   │   ├── CartIcon
│   │   │   └── UserMenu
│   │   ├── Main (Route Content)
│   │   └── Footer
│   │
│   ├── AuthLayout
│   │   └── AuthForm (Login/Register)
│   │
│   └── ErrorBoundary
```

### Component Types

#### 1. Presentational Components (UI Components)

Pure components that focus on how things look. They receive data and callbacks via props.

**Example: Button Component**

```typescript
// components/ui/Button/Button.tsx
interface ButtonProps {
  variant?: 'primary' | 'secondary' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  disabled?: boolean;
  children: React.ReactNode;
  onClick?: () => void;
  type?: 'button' | 'submit' | 'reset';
  className?: string;
}

export const Button: React.FC<ButtonProps> = ({
  variant = 'primary',
  size = 'md',
  isLoading = false,
  disabled = false,
  children,
  onClick,
  type = 'button',
  className,
}) => {
  // Component implementation
};
```

#### 2. Container Components (Smart Components)

Components that handle business logic, state management, and data fetching.

**Example: ProductListContainer**

```typescript
// pages/Products/ProductList.tsx
export const ProductList: React.FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState<FilterState>({});

  useEffect(() => {
    fetchProducts(filters);
  }, [filters]);

  return (
    <ProductListView
      products={products}
      loading={loading}
      filters={filters}
      onFilterChange={setFilters}
    />
  );
};
```

#### 3. Layout Components

Components that define the page structure and layout.

**Example: MainLayout**

```typescript
// components/layout/MainLayout/MainLayout.tsx
interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="min-h-screen flex flex-col">
      <Header />
      <main className="flex-1">
        {children}
      </main>
      <Footer />
    </div>
  );
};
```

#### 4. Feature Components

Domain-specific components that combine multiple UI components.

**Example: ProductCard**

```typescript
// components/features/product/ProductCard/ProductCard.tsx
interface ProductCardProps {
  product: Product;
  onAddToCart: (productId: string) => void;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  onAddToCart,
}) => {
  return (
    <Card>
      <ProductImage src={product.image} alt={product.name} />
      <CardContent>
        <h3>{product.name}</h3>
        <PriceDisplay price={product.price} />
        <StockBadge available={product.inStock} />
        <Button onClick={() => onAddToCart(product.id)}>
          Add to Cart
        </Button>
      </CardContent>
    </Card>
  );
};
```

---

## State Management

### Approach: Layered State Management

We use different state management strategies based on the scope and nature of the state:

#### 1. Local Component State (useState)

For UI state that doesn't need to be shared.

```typescript
// Example: Form state
const [email, setEmail] = useState('');
const [password, setPassword] = useState('');
```

#### 2. Context API

For application-wide state that needs to be accessed by many components.

**Auth Context Example:**

```typescript
// context/AuthContext.tsx
interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  register: (userData: RegisterData) => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  // Auth logic implementation

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout, register }}>
      {children}
    </AuthContext.Provider>
  );
};

// Custom hook for easy access
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};
```

#### 3. Zustand (Alternative to Context for Complex State)

For more complex state management with better performance.

```typescript
// store/slices/cartSlice.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface CartItem {
  productId: string;
  quantity: number;
  price: number;
}

interface CartState {
  items: CartItem[];
  addItem: (item: CartItem) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  getTotalPrice: () => number;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],

      addItem: (item) => set((state) => ({
        items: [...state.items, item]
      })),

      removeItem: (productId) => set((state) => ({
        items: state.items.filter((item) => item.productId !== productId)
      })),

      updateQuantity: (productId, quantity) => set((state) => ({
        items: state.items.map((item) =>
          item.productId === productId ? { ...item, quantity } : item
        )
      })),

      clearCart: () => set({ items: [] }),

      getTotalPrice: () => {
        const { items } = get();
        return items.reduce((total, item) => total + (item.price * item.quantity), 0);
      }
    }),
    {
      name: 'cart-storage',
    }
  )
);
```

#### 4. React Query / TanStack Query

For server state management (data fetching, caching, synchronization).

```typescript
// hooks/useProducts.ts
import { useQuery } from '@tanstack/react-query';
import { productService } from '@/api/services/product.service';

export const useProducts = (filters?: ProductFilters) => {
  return useQuery({
    queryKey: ['products', filters],
    queryFn: () => productService.getProducts(filters),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useProduct = (productId: string) => {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: () => productService.getProduct(productId),
    enabled: !!productId,
  });
};
```

### State Organization

| State Type | Tool | Use Case |
|------------|------|----------|
| UI State | `useState` | Form inputs, modals, toggles |
| Shared UI State | Context API | Theme, notifications |
| Client State | Zustand | Cart, user preferences |
| Server State | React Query | API data, caching |
| Form State | React Hook Form | Complex forms with validation |

---

## Routing Architecture

### Route Configuration

```typescript
// config/routes.ts
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  REGISTER: '/register',
  PRODUCTS: '/products',
  PRODUCT_DETAIL: '/products/:id',
  CART: '/cart',
  CHECKOUT: '/checkout',
  ORDER_CONFIRMATION: '/order/confirmation/:orderId',
  ORDER_HISTORY: '/orders',
  ORDER_DETAIL: '/orders/:orderId',
  PROFILE: '/profile',
  SETTINGS: '/settings',
  NOT_FOUND: '*',
} as const;
```

### Router Setup

```typescript
// router.tsx
import { createBrowserRouter } from 'react-router-dom';
import { MainLayout } from './components/layout/MainLayout';
import { ProtectedRoute } from './components/common/ProtectedRoute';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <MainLayout />,
    errorElement: <ErrorPage />,
    children: [
      {
        index: true,
        element: <Home />,
      },
      {
        path: 'products',
        children: [
          {
            index: true,
            element: <ProductList />,
          },
          {
            path: ':id',
            element: <ProductDetail />,
          },
        ],
      },
      {
        path: 'cart',
        element: <Cart />,
      },
      {
        path: 'checkout',
        element: (
          <ProtectedRoute>
            <Checkout />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders',
        element: (
          <ProtectedRoute>
            <OrderHistory />
          </ProtectedRoute>
        ),
      },
      {
        path: 'orders/:orderId',
        element: (
          <ProtectedRoute>
            <OrderDetail />
          </ProtectedRoute>
        ),
      },
      {
        path: 'profile',
        element: (
          <ProtectedRoute>
            <Profile />
          </ProtectedRoute>
        ),
      },
    ],
  },
  {
    path: '/login',
    element: <Login />,
  },
  {
    path: '/register',
    element: <Register />,
  },
  {
    path: '*',
    element: <NotFound />,
  },
]);
```

### Protected Routes

```typescript
// components/common/ProtectedRoute/ProtectedRoute.tsx
interface ProtectedRouteProps {
  children: React.ReactNode;
  requireAuth?: boolean;
}

export const ProtectedRoute: React.FC<ProtectedRouteProps> = ({
  children,
  requireAuth = true,
}) => {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (requireAuth && !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return <>{children}</>;
};
```

---

## Data Flow

### Request Flow

```
User Action
    ↓
Component Event Handler
    ↓
API Service Call
    ↓
Axios Interceptor (Add Auth Token)
    ↓
Backend API
    ↓
Response Interceptor (Handle Errors)
    ↓
State Update (Context/Store)
    ↓
Component Re-render
    ↓
UI Update
```

### Authentication Flow

```
1. User submits login form
2. LoginForm calls auth.login(email, password)
3. authService.login() sends POST /api/auth/login
4. Backend validates credentials
5. Backend returns JWT token
6. Frontend stores token in localStorage
7. AuthContext updates user state
8. User redirected to dashboard
9. All subsequent API calls include JWT token in Authorization header
```

### Cart Flow

```
1. User clicks "Add to Cart" on ProductCard
2. onAddToCart handler called
3. cartStore.addItem() updates cart state
4. Cart state persisted to localStorage
5. CartIcon badge updates with new count
6. Toast notification shows success message
```

---

## Key Patterns

### 1. Composition Pattern

Build complex components from simpler ones.

```typescript
// Good: Composition
<Card>
  <CardHeader>
    <CardTitle>{product.name}</CardTitle>
  </CardHeader>
  <CardContent>
    <ProductImage src={product.image} />
    <PriceDisplay price={product.price} />
  </CardContent>
  <CardFooter>
    <Button>Add to Cart</Button>
  </CardFooter>
</Card>
```

### 2. Render Props Pattern

Share logic between components.

```typescript
interface LoadingStateProps<T> {
  isLoading: boolean;
  data: T | null;
  error: Error | null;
  children: (data: T) => React.ReactNode;
}

function LoadingState<T>({ isLoading, data, error, children }: LoadingStateProps<T>) {
  if (isLoading) return <Spinner />;
  if (error) return <ErrorMessage error={error} />;
  if (!data) return <EmptyState />;
  return <>{children(data)}</>;
}

// Usage
<LoadingState isLoading={loading} data={products} error={error}>
  {(products) => <ProductGrid products={products} />}
</LoadingState>
```

### 3. Custom Hooks Pattern

Extract and reuse stateful logic.

```typescript
// hooks/useDebounce.ts
export function useDebounce<T>(value: T, delay: number): T {
  const [debouncedValue, setDebouncedValue] = useState<T>(value);

  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);

  return debouncedValue;
}

// Usage in component
const [searchTerm, setSearchTerm] = useState('');
const debouncedSearchTerm = useDebounce(searchTerm, 500);

useEffect(() => {
  if (debouncedSearchTerm) {
    searchProducts(debouncedSearchTerm);
  }
}, [debouncedSearchTerm]);
```

### 4. Error Boundary Pattern

Catch and handle errors gracefully.

```typescript
// components/common/ErrorBoundary/ErrorBoundary.tsx
interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends React.Component<
  { children: ReactNode },
  ErrorBoundaryState
> {
  constructor(props: { children: ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return <ErrorFallback error={this.state.error} />;
    }

    return this.props.children;
  }
}
```

### 5. Provider Pattern

Provide dependencies to component tree.

```typescript
// App.tsx
function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <CartProvider>
            <ThemeProvider>
              <NotificationProvider>
                <RouterProvider router={router} />
              </NotificationProvider>
            </ThemeProvider>
          </CartProvider>
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
```

---

## Performance Considerations

### Code Splitting

```typescript
// Lazy load routes
const ProductDetail = lazy(() => import('./pages/Products/ProductDetail'));
const OrderHistory = lazy(() => import('./pages/Orders/OrderHistory'));

// Wrap in Suspense
<Suspense fallback={<LoadingSpinner />}>
  <ProductDetail />
</Suspense>
```

### Memoization

```typescript
// Memoize expensive computations
const totalPrice = useMemo(() => {
  return cartItems.reduce((sum, item) => sum + item.price * item.quantity, 0);
}, [cartItems]);

// Memoize callbacks
const handleAddToCart = useCallback((productId: string) => {
  addToCart(productId);
}, [addToCart]);

// Memoize components
const ProductCard = memo<ProductCardProps>(({ product, onAddToCart }) => {
  // Component implementation
});
```

---

## Testing Strategy

### Component Testing

```typescript
// Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react';
import { Button } from './Button';

describe('Button', () => {
  it('renders children correctly', () => {
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
    expect(screen.getByRole('button')).toBeDisabled();
  });
});
```

---

## Deployment Architecture

```
Development: localhost:3000 → Spring Boot (localhost:8080)
Staging: staging-app.example.com → staging-api.example.com
Production: app.example.com → api.example.com
```

---

**Document Maintained By:** Frontend Team
**Last Reviewed:** October 2025
