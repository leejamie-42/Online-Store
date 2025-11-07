# Phase 6: Checkout Review Page Implementation

**Status**: ⬜ Not Started
**Estimated Time**: 6-8 hours
**Priority**: High
**Dependencies**: Phase 5 (Product API Integration), Authentication System

## 📋 Phase Overview

Implement the first step of a multi-step checkout flow: the **Checkout Review Page**. This page allows users to review their order details, see the pricing breakdown, and proceed to the shipping details step.

### Checkout Flow Architecture
```
Step 1: Review (This Phase) → Step 2: Details → Step 3: Payment → Step 4: Confirmation
```

### Key Features
- **Step Progress Indicator**: Visual guide showing user's current position (Step 1 of 4)
- **Order Summary**: Display all cart items with product details, quantities, and prices
- **Pricing Breakdown**: Subtotal, shipping cost, tax (8%), and total amount
- **Navigation**: Proceed to shipping details step or return to shopping
- **Cart Integration**: Pull data from existing Zustand cart store
- **Protected Route**: Requires user authentication

### Design Reference
- **Figma**: [Checkout Review Page](https://www.figma.com/design/BYlBng6s0iLmcrXxAaWvDt/Store-application?node-id=16-3)
- **Primary Color**: #155dfc (Blue for active states, CTAs)
- **Typography**: Inter font family
- **Layout**: Two-column (Order Summary + Order Total sidebar)

---

## 📝 Task Breakdown

### Task 1: Type Definitions ⬜

**Estimated Time**: 30 minutes
**Files to Create**:
- `frontend/src/types/order.types.ts`
- `frontend/src/types/cart.types.ts`

**Description**: Define TypeScript interfaces for order and cart entities to ensure type safety across components.

**Implementation Details**:

#### `order.types.ts`
```typescript
import type { Product, ProductDetail } from './product.types';

export type OrderStatus =
  | 'pending'
  | 'processing'
  | 'picked_up'
  | 'delivering'
  | 'delivered'
  | 'cancelled';

export interface ShippingInfo {
  receiver_name: string;
  receiver_phone: string;
  receiver_address: string;
  receiver_city: string;
  receiver_state: string;
  receiver_postcode: string;
}

export interface OrderItem {
  product: Product | ProductDetail;
  quantity: number;
  price: number; // Price at time of order
}

export interface Order {
  id: string;
  user_id: string;
  items: OrderItem[];
  quantity: number; // Total quantity across all items
  status: OrderStatus;
  shipping_info?: ShippingInfo;
  subtotal: number;
  shipping_cost: number;
  tax: number;
  total: number;
  created_at: string;
  updated_at?: string;
}

export interface CreateOrderRequest {
  product_id: string;
  quantity: number;
  user_id: string;
  shipping_info: ShippingInfo;
}

export interface CreateOrderResponse {
  order_id: string;
  status: OrderStatus;
  bpay_details?: {
    biller_code: string;
    reference_number: string;
    amount: number;
    expires_at: string;
  };
}
```

#### `cart.types.ts`
```typescript
import type { Product } from './product.types';

export interface CartItem {
  product: Product;
  quantity: number;
}

export interface CartState {
  items: CartItem[];
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  getItemCount: () => number;
  getSubtotal: () => number;
}

export interface CartSummary {
  subtotal: number;
  shipping: number;
  tax: number;
  total: number;
  itemCount: number;
}
```

**Acceptance Criteria**:
- ✅ All interfaces match backend API spec (`docs/SYSTEM_INTERFACE_SPEC.md`)
- ✅ OrderStatus type includes all valid order states
- ✅ ShippingInfo matches POST /api/orders request structure
- ✅ No TypeScript errors when importing these types

---

### Task 2: StepIndicator Component ⬜

**Estimated Time**: 1 hour
**File**: `frontend/src/components/features/checkout/StepIndicator.tsx`

**Description**: Create a reusable component to display the 4-step checkout progress indicator.

**Component Structure**:
```typescript
interface Step {
  number: number;
  label: string;
  description: string;
}

interface StepIndicatorProps {
  currentStep: 1 | 2 | 3 | 4;
  steps: Step[];
}

export function StepIndicator({ currentStep, steps }: StepIndicatorProps) {
  // Implementation
}
```

**Visual Design** (from Figma):
- **Active Step**: Blue circle (#155dfc) with white number, bold label
- **Inactive Step**: Gray circle with gray number, gray label
- **Connector Line**: Horizontal line between steps (blue for completed, gray for upcoming)

**Default Steps**:
```typescript
const CHECKOUT_STEPS: Step[] = [
  { number: 1, label: 'Review', description: 'Review your order' },
  { number: 2, label: 'Details', description: 'Shipping information' },
  { number: 3, label: 'Payment', description: 'Payment via BPAY' },
  { number: 4, label: 'Confirmation', description: 'Order confirmation' }
];
```

**Accessibility Requirements**:
- ARIA labels: `aria-current="step"` for active step
- Screen reader text: "Step 1 of 4: Review"
- Keyboard navigation: Tab through steps (if clickable in future)

**Acceptance Criteria**:
- ✅ Displays 4 steps horizontally on desktop, vertically on mobile
- ✅ Highlights current step with primary color
- ✅ Responsive layout (stack vertically on mobile <640px)
- ✅ ARIA attributes for accessibility
- ✅ Accepts custom step labels via props

---

### Task 3: OrderItemCard Component ⬜

**Estimated Time**: 1 hour
**File**: `frontend/src/components/features/checkout/OrderItemCard.tsx`

**Description**: Display a single order item with product details, quantity, and price.

**Component Structure**:
```typescript
import type { OrderItem } from '@/types/order.types';

interface OrderItemCardProps {
  item: OrderItem;
  showQuantityControls?: boolean; // Future: allow quantity adjustment
}

export function OrderItemCard({ item, showQuantityControls = false }: OrderItemCardProps) {
  const { product, quantity, price } = item;

  return (
    <Card className="flex gap-4 p-4">
      {/* Product image */}
      <img
        src={product.image_url}
        alt={product.name}
        className="w-24 h-24 object-cover rounded"
      />

      {/* Product details */}
      <div className="flex-1">
        <div className="flex items-start justify-between">
          <div>
            <Badge variant="secondary">{product.category || 'Accessories'}</Badge>
            <h3 className="font-semibold mt-1">{product.name}</h3>
            <p className="text-sm text-gray-600 mt-1">{product.description}</p>
          </div>
          <p className="font-semibold">${price.toFixed(2)}</p>
        </div>

        {/* Quantity */}
        <div className="mt-2 flex items-center gap-2">
          <span className="text-sm text-gray-600">Quantity:</span>
          <span className="font-medium">{quantity}</span>
        </div>
      </div>
    </Card>
  );
}
```

**Layout** (from Figma):
- Horizontal card: Image (left) + Details (right)
- Category badge above product name
- Product name (semibold, larger text)
- Description (smaller, gray text)
- Price aligned to the right
- Quantity display at bottom left

**Acceptance Criteria**:
- ✅ Uses existing Card component from `@/components/ui/Card`
- ✅ Uses Badge component for category
- ✅ Displays product image with fallback for missing images
- ✅ Price formatted with 2 decimal places
- ✅ Responsive: Image shrinks on mobile (<640px)

---

### Task 4: OrderSummary Component ⬜

**Estimated Time**: 1.5 hours
**File**: `frontend/src/components/features/checkout/OrderSummary.tsx`

**Description**: Aggregate and display all order items from the cart.

**Component Structure**:
```typescript
import type { OrderItem } from '@/types/order.types';
import { OrderItemCard } from './OrderItemCard';

interface OrderSummaryProps {
  items: OrderItem[];
  isLoading?: boolean;
}

export function OrderSummary({ items, isLoading = false }: OrderSummaryProps) {
  if (isLoading) {
    return <OrderSummarySkeleton />;
  }

  if (items.length === 0) {
    return (
      <Card className="p-8 text-center">
        <p className="text-gray-600">Your cart is empty</p>
        <Button asChild className="mt-4">
          <Link to="/products">Continue Shopping</Link>
        </Button>
      </Card>
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-semibold">Order Summary</h2>
      <div className="space-y-3">
        {items.map((item) => (
          <OrderItemCard key={item.product.id} item={item} />
        ))}
      </div>
    </div>
  );
}
```

**Features**:
- Display all cart items using OrderItemCard
- Loading skeleton state while cart data loads
- Empty state with "Continue Shopping" button
- Item count display: "{count} item(s) in cart"

**Acceptance Criteria**:
- ✅ Renders OrderItemCard for each cart item
- ✅ Shows loading skeleton during data fetch
- ✅ Empty state redirects to product catalog
- ✅ Items display in order they were added to cart

---

### Task 5: OrderTotal Component ⬜

**Estimated Time**: 1 hour
**File**: `frontend/src/components/features/checkout/OrderTotal.tsx`

**Description**: Display pricing breakdown with subtotal, shipping, tax, and total.

**Component Structure**:
```typescript
import type { CartSummary } from '@/types/cart.types';

interface OrderTotalProps {
  summary: CartSummary;
}

export function OrderTotal({ summary }: OrderTotalProps) {
  return (
    <Card className="p-6 sticky top-4">
      <h3 className="text-xl font-semibold mb-4">Order Total</h3>

      <div className="space-y-3">
        <div className="flex justify-between">
          <span className="text-gray-600">Subtotal</span>
          <span className="font-medium">${summary.subtotal.toFixed(2)}</span>
        </div>

        <div className="flex justify-between">
          <span className="text-gray-600">Shipping</span>
          <span className="font-medium">
            {summary.shipping === 0 ? 'FREE' : `$${summary.shipping.toFixed(2)}`}
          </span>
        </div>

        <div className="flex justify-between">
          <span className="text-gray-600">Tax (8%)</span>
          <span className="font-medium">${summary.tax.toFixed(2)}</span>
        </div>

        <Separator />

        <div className="flex justify-between text-lg">
          <span className="font-semibold">Total</span>
          <span className="font-bold text-primary">${summary.total.toFixed(2)}</span>
        </div>
      </div>

      <Alert className="mt-4 bg-blue-50 border-blue-200">
        <AlertDescription>
          In the next step, you'll provide your shipping details and complete payment via BPAY.
        </AlertDescription>
      </Alert>
    </Card>
  );
}
```

**Calculation Utilities** (`frontend/src/utils/order.utils.ts`):
```typescript
import type { CartItem } from '@/types/cart.types';

const TAX_RATE = 0.08; // 8%
const FREE_SHIPPING_THRESHOLD = 100;
const SHIPPING_COST = 10;

export function calculateCartSummary(items: CartItem[]) {
  const subtotal = items.reduce((sum, item) => {
    return sum + (item.product.price * item.quantity);
  }, 0);

  const shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;
  const tax = subtotal * TAX_RATE;
  const total = subtotal + shipping + tax;

  return {
    subtotal,
    shipping,
    tax,
    total,
    itemCount: items.reduce((sum, item) => sum + item.quantity, 0)
  };
}

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat('en-AU', {
    style: 'currency',
    currency: 'AUD'
  }).format(amount);
}
```

**Acceptance Criteria**:
- ✅ Displays all pricing components (subtotal, shipping, tax, total)
- ✅ Tax calculated at 8% of subtotal
- ✅ Free shipping for orders ≥ $100
- ✅ Total amount highlighted in primary color
- ✅ Sticky positioning on desktop (stays visible when scrolling)
- ✅ Info alert about next steps

---

### Task 6: CheckoutReview Page ⬜

**Estimated Time**: 2 hours
**File**: `frontend/src/pages/checkout/CheckoutReview.tsx`

**Description**: Main checkout review page integrating all components with cart data.

**Page Structure**:
```typescript
import { useNavigate } from 'react-router-dom';
import { useCartStore } from '@/stores/cart.store';
import { StepIndicator } from '@/components/features/checkout/StepIndicator';
import { OrderSummary } from '@/components/features/checkout/OrderSummary';
import { OrderTotal } from '@/components/features/checkout/OrderTotal';
import { calculateCartSummary } from '@/utils/order.utils';
import { CHECKOUT_STEPS } from '@/config/checkout.constants';

export function CheckoutReview() {
  const navigate = useNavigate();
  const { items } = useCartStore();

  const orderItems = items.map(item => ({
    product: item.product,
    quantity: item.quantity,
    price: item.product.price
  }));

  const summary = calculateCartSummary(items);

  const handleContinue = () => {
    navigate('/checkout/details');
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-7xl">
      {/* Step Indicator */}
      <StepIndicator currentStep={1} steps={CHECKOUT_STEPS} />

      {/* Two-column layout */}
      <div className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left: Order Summary (2/3 width) */}
        <div className="lg:col-span-2">
          <OrderSummary items={orderItems} />

          {/* Action Buttons */}
          <div className="mt-6 flex gap-4">
            <Button
              variant="outline"
              onClick={() => navigate('/products')}
            >
              Continue Shopping
            </Button>
            <Button
              onClick={handleContinue}
              disabled={items.length === 0}
              className="flex-1"
            >
              Continue to Details →
            </Button>
          </div>
        </div>

        {/* Right: Order Total (1/3 width) */}
        <div className="lg:col-span-1">
          <OrderTotal summary={summary} />
        </div>
      </div>
    </div>
  );
}
```

**Layout** (from Figma):
- Full-width step indicator at top
- Two-column grid: Order Summary (left 2/3) + Order Total (right 1/3)
- Responsive: Stack vertically on mobile (<1024px)
- Action buttons below order summary

**Error Handling**:
- Redirect to `/products` if cart is empty
- Show error toast if cart data fails to load
- Disable "Continue" button if no items in cart

**Acceptance Criteria**:
- ✅ Pulls cart data from Zustand store (no API call needed)
- ✅ Displays step indicator showing Step 1 active
- ✅ Two-column layout on desktop, single column on mobile
- ✅ "Continue to Details" navigates to `/checkout/details`
- ✅ "Continue Shopping" navigates to `/products`
- ✅ Protected route (requires authentication)

---

### Task 7: Routing & Navigation ⬜

**Estimated Time**: 30 minutes
**Files to Update**:
- `frontend/src/config/routes.ts`
- `frontend/src/App.tsx` (or router configuration file)

**Description**: Configure nested checkout routes and route guards.

**Route Structure**:
```typescript
// frontend/src/config/routes.ts
export const ROUTES = {
  // ... existing routes
  CHECKOUT: '/checkout',
  CHECKOUT_REVIEW: '/checkout/review',
  CHECKOUT_DETAILS: '/checkout/details',
  CHECKOUT_PAYMENT: '/checkout/payment',
  CHECKOUT_CONFIRMATION: '/checkout/confirmation',
} as const;
```

**Router Configuration**:
```typescript
// In App.tsx or router config
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { CheckoutReview } from '@/pages/checkout/CheckoutReview';

// Inside Routes
<Route path="/checkout" element={<ProtectedRoute requireAuth />}>
  <Route index element={<Navigate to="/checkout/review" replace />} />
  <Route path="review" element={<CheckoutReview />} />
  {/* Future routes */}
  <Route path="details" element={<div>Details Step (Phase 7)</div>} />
  <Route path="payment" element={<div>Payment Step (Phase 8)</div>} />
  <Route path="confirmation" element={<div>Confirmation (Phase 9)</div>} />
</Route>
```

**Navigation Guards**:
- All `/checkout/*` routes require authentication
- If user not logged in → redirect to `/login?redirect=/checkout/review`
- Empty cart → redirect to `/products` with toast notification

**Acceptance Criteria**:
- ✅ `/checkout` redirects to `/checkout/review`
- ✅ Nested routes configured for 4-step flow
- ✅ Protected route wrapper requires authentication
- ✅ Unauthenticated access redirects to login
- ✅ Redirect back to checkout after successful login

---

### Task 8: Testing & Validation ⬜

**Estimated Time**: 1 hour
**Files to Create**:
- `frontend/src/utils/order.utils.test.ts`
- `frontend/src/components/features/checkout/OrderItemCard.test.tsx`
- `frontend/src/components/features/checkout/StepIndicator.test.tsx`

**Description**: Write unit and integration tests for checkout components.

#### Unit Tests: Order Utilities
```typescript
// order.utils.test.ts
import { describe, it, expect } from 'vitest';
import { calculateCartSummary, formatCurrency } from './order.utils';

describe('calculateCartSummary', () => {
  it('calculates subtotal correctly', () => {
    const items = [
      { product: { price: 50 }, quantity: 2 },
      { product: { price: 30 }, quantity: 1 }
    ];
    const result = calculateCartSummary(items);
    expect(result.subtotal).toBe(130);
  });

  it('applies free shipping for orders >= $100', () => {
    const items = [{ product: { price: 100 }, quantity: 1 }];
    const result = calculateCartSummary(items);
    expect(result.shipping).toBe(0);
  });

  it('applies $10 shipping for orders < $100', () => {
    const items = [{ product: { price: 50 }, quantity: 1 }];
    const result = calculateCartSummary(items);
    expect(result.shipping).toBe(10);
  });

  it('calculates 8% tax on subtotal', () => {
    const items = [{ product: { price: 100 }, quantity: 1 }];
    const result = calculateCartSummary(items);
    expect(result.tax).toBe(8); // 8% of 100
  });

  it('calculates total = subtotal + shipping + tax', () => {
    const items = [{ product: { price: 50 }, quantity: 1 }];
    const result = calculateCartSummary(items);
    // subtotal: 50, shipping: 10, tax: 4 (8% of 50) = 64
    expect(result.total).toBe(64);
  });
});

describe('formatCurrency', () => {
  it('formats currency with AUD symbol', () => {
    expect(formatCurrency(99.99)).toBe('$99.99');
  });
});
```

#### Component Tests
```typescript
// StepIndicator.test.tsx
import { render, screen } from '@testing-library/react';
import { StepIndicator } from './StepIndicator';

describe('StepIndicator', () => {
  const steps = [
    { number: 1, label: 'Review', description: 'Review order' },
    { number: 2, label: 'Details', description: 'Shipping info' }
  ];

  it('highlights current step', () => {
    render(<StepIndicator currentStep={1} steps={steps} />);
    expect(screen.getByText('Review')).toHaveClass('text-primary');
  });

  it('renders all steps', () => {
    render(<StepIndicator currentStep={1} steps={steps} />);
    expect(screen.getByText('Review')).toBeInTheDocument();
    expect(screen.getByText('Details')).toBeInTheDocument();
  });
});
```

#### Manual Testing Checklist
- [ ] **Empty Cart**: Navigate to checkout with empty cart → Should redirect to products
- [ ] **Single Item**: Add one product → Review shows correct subtotal, shipping, tax, total
- [ ] **Multiple Items**: Add 3+ products → All items display in order summary
- [ ] **Free Shipping**: Add items totaling ≥ $100 → Shipping shows "FREE"
- [ ] **Mobile Responsive**: Test on mobile (<640px) → Components stack vertically
- [ ] **Navigation**: Click "Continue to Details" → Navigate to `/checkout/details`
- [ ] **Authentication**: Log out → Access `/checkout/review` → Redirect to login
- [ ] **Keyboard Navigation**: Tab through step indicator → Focus visible

#### Accessibility Testing
- [ ] Run Lighthouse accessibility audit (score ≥ 90)
- [ ] Test with screen reader (VoiceOver/NVDA)
- [ ] Keyboard-only navigation works
- [ ] ARIA labels present on step indicator
- [ ] Color contrast meets WCAG AA (4.5:1 for text)

**Acceptance Criteria**:
- ✅ All unit tests pass (`npm run test`)
- ✅ Test coverage ≥ 80% for utility functions
- ✅ Component tests cover happy path and edge cases
- ✅ Manual testing checklist completed
- ✅ Accessibility audit passes

---

## 🔗 API Integration Notes

### Current Phase (Review)
- **No API calls needed** for checkout review step
- Cart data pulled from Zustand store (`useCartStore`)
- Data structure:
  ```typescript
  const cartStore = useCartStore();
  cartStore.items // CartItem[]
  ```

### Future Phases
- **POST /api/orders** (Phase 8: Payment step)
  - Request body: `{ product_id, quantity, user_id, shipping_info }`
  - Response: `{ order_id, status, bpay_details }`
  - Reference: `docs/SYSTEM_INTERFACE_SPEC.md` lines 123-156

- **GET /api/orders/:id** (Phase 9: Confirmation step)
  - Retrieve order details after payment
  - Display order confirmation with tracking info

---

## 📦 Dependencies & Prerequisites

### Required (Already Completed)
- ✅ **Phase 5**: Product API Integration (product types, API service)
- ✅ **Authentication System**: `useAuth` hook, protected routes
- ✅ **Zustand Cart Store**: Cart state management (assumed to exist)

### Optional (Nice to Have)
- ⬜ **Cart Page**: Add/remove items functionality (may be separate phase)
- ⬜ **Product Quantity Selector**: On product detail page (Phase 5 extension)

### External Libraries
- React Router DOM: Navigation and nested routes
- Zustand: Cart state management
- Tailwind CSS: Styling (already configured)
- Shadcn/ui: Card, Button, Badge, Alert components

---

## ✅ Success Criteria

### Functional Requirements
- ✅ User can view all cart items on checkout review page
- ✅ Pricing breakdown displays correctly (subtotal + shipping + tax = total)
- ✅ Step indicator shows "Step 1 of 4" active
- ✅ Navigation to next step (`/checkout/details`) works
- ✅ Protected route: Redirects to login if unauthenticated
- ✅ Empty cart: Redirects to product catalog

### Non-Functional Requirements
- ✅ **Responsive Design**: Works on mobile (320px), tablet (768px), desktop (1024px+)
- ✅ **Accessibility**: WCAG AA compliance, keyboard navigation
- ✅ **Performance**: Page loads in < 2 seconds
- ✅ **Type Safety**: No TypeScript errors, all components fully typed
- ✅ **Testing**: 80%+ code coverage, all tests passing

### Code Quality
- ✅ Follows project conventions (component structure, naming, imports)
- ✅ Uses existing UI components (Card, Button, Badge)
- ✅ Proper error handling and loading states
- ✅ No console errors or warnings in browser

---

## 🎨 Design System Reference

### Colors (from Figma)
- **Primary**: `#155dfc` (Blue for CTAs, active states)
- **Text**: `#1a1a1a` (Body text)
- **Text Secondary**: `#6b7280` (Gray 500 for labels)
- **Background**: `#ffffff` (White)
- **Border**: `#e5e7eb` (Gray 200)

### Typography
- **Font Family**: Inter (fallback: system-ui, sans-serif)
- **Heading 1**: 24px, font-weight 600
- **Heading 2**: 20px, font-weight 600
- **Body**: 16px, font-weight 400
- **Small**: 14px, font-weight 400

### Spacing
- **Section Gap**: 32px (8 in Tailwind)
- **Card Padding**: 24px (6 in Tailwind)
- **Component Gap**: 16px (4 in Tailwind)

### Breakpoints (Tailwind)
- **sm**: 640px (Mobile landscape)
- **md**: 768px (Tablet)
- **lg**: 1024px (Desktop)
- **xl**: 1280px (Large desktop)

---

## 📚 Code Examples & Patterns

### Component Import Pattern
```typescript
// External libraries
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

// Internal aliases (@/)
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import type { OrderItem } from '@/types/order.types';

// Relative imports
import { OrderItemCard } from './OrderItemCard';
```

### Error Boundary Pattern
```typescript
// Wrap checkout page in error boundary
<ErrorBoundary
  fallback={<CheckoutErrorFallback />}
  onError={(error) => logError('Checkout Error', error)}
>
  <CheckoutReview />
</ErrorBoundary>
```

### Loading State Pattern
```typescript
function OrderSummary({ items, isLoading }: OrderSummaryProps) {
  if (isLoading) {
    return (
      <div className="space-y-3">
        {[1, 2, 3].map(i => <Skeleton key={i} className="h-24" />)}
      </div>
    );
  }
  // ... rest of component
}
```

---

## 🚀 Next Steps (Future Phases)

After completing this phase, proceed to:

1. **Phase 7: Checkout Details Page**
   - Shipping information form
   - Form validation with Zod
   - Address autocomplete (optional)

2. **Phase 8: Payment Integration**
   - BPAY payment instructions
   - Order creation API call
   - Payment status polling

3. **Phase 9: Order Confirmation**
   - Order confirmation page
   - Email notification trigger
   - Order tracking link

---

## 📌 Notes & Considerations

### Design Decisions
- **Tax Calculation**: Fixed 8% rate (backend may recalculate based on location in future)
- **Shipping Cost**: Flat $10, free for orders ≥ $100 (backend controls final pricing)
- **Cart Persistence**: Uses Zustand with localStorage middleware (survives page refresh)
- **Step Navigation**: Linear flow (cannot skip steps), enforced by routing

### Known Limitations
- Cart currently supports single product variant (no size/color selection)
- No quantity adjustment on review page (must go back to cart)
- Shipping cost is flat rate (no location-based calculation yet)
- Tax rate is hardcoded (future: calculate based on shipping state)

### Technical Debt
- [ ] Extract CHECKOUT_STEPS to shared constants file
- [ ] Create shared `useCheckoutFlow` hook for step navigation logic
- [ ] Add unit tests for Zustand cart store
- [ ] Implement optimistic UI updates for quantity changes

---

## 📖 Related Documentation

- **System Architecture**: `docs/SYSTEM_ARCHITECTURE.md`
- **API Specification**: `docs/SYSTEM_INTERFACE_SPEC.md` (Order endpoints)
- **Frontend Architecture**: `docs/frontend/ARCHITECTURE.md`
- **Development Standards**: `docs/frontend/DEVELOPMENT_STANDARDS.md`
- **Figma Design**: [Checkout Review Page](https://www.figma.com/design/BYlBng6s0iLmcrXxAaWvDt/Store-application?node-id=16-3)

---

**Last Updated**: 2025-10-23
**Document Version**: 1.0
**Author**: Generated via Claude Code /sc:document command
