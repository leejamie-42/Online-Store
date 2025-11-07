# Phase 9: Order Detail Page Implementation

## Overview

**Phase Goal:** Implement a comprehensive Order Detail page that displays order information with a visual status tracker, product details, shipping information, and customer support options.

**Based On:** Figma Design - Order Detail Page UI

**Estimated Duration:** 8-12 hours (1-2 days)

**Priority:** HIGH

**Status:** ⬜ Not Started

**Prerequisites:**
- Phase 0-8 completed (foundation, UI components, API integration)
- Order API service exists (`orderService.getOrder()`)
- Order types defined (`Order`, `OrderStatus`, `ShippingInfo`)
- UI components available (Card, Button, Badge, Spinner)

---

## Deliverables

By the end of this phase, you will have:

✅ **OrderStatusStepper Component** - Visual 5-stage progress tracker
✅ **OrderProductCard Component** - Product information display
✅ **OrderInfoPanel Component** - Order details sidebar
✅ **OrderDetail Page** - Complete page with API integration
✅ **Route Configuration** - `/orders/:orderId` route setup
✅ **Figma Design Alignment** - Pixel-perfect implementation
✅ **Responsive Layout** - Mobile, tablet, desktop support
✅ **Unit Tests** - Component and page tests

---

## Section 1: UI Components

### Task 9.1: Create OrderStatusStepper Component

**Status:** ⬜ Not Started
**Depends On:** None
**Estimated Time:** 1.5-2 hours

**Description:**

Create a visual order status stepper component that shows 5 stages of order fulfillment with progress indication.

**Figma Design Requirements:**

- **5 Stages:** Order Placed → Processing → Picked Up → In Transit → Delivered
- **Visual Style:** Blue circular icons connected by horizontal lines
- **Active State:** Blue filled circle with white icon
- **Completed State:** Blue filled circle with checkmark
- **Pending State:** Gray outlined circle
- **Current Status Badge:** Green "Delivered" badge at top right

**Files to Create:**

```
frontend/src/components/features/order/OrderStatusStepper/
├── OrderStatusStepper.tsx
├── OrderStatusStepper.test.tsx
└── index.ts
```

**Implementation Guide:**

```typescript
// frontend/src/components/features/order/OrderStatusStepper/OrderStatusStepper.tsx

import React from 'react';
import type { OrderStatus } from '@/types';

interface OrderStatusStepperProps {
  currentStatus: OrderStatus;
  className?: string;
}

type OrderStep = {
  id: OrderStatus;
  label: string;
  icon: string;
};

const ORDER_STEPS: OrderStep[] = [
  { id: 'pending', label: 'Order Placed', icon: '📦' },
  { id: 'processing', label: 'Processing', icon: '⚙️' },
  { id: 'picked_up', label: 'Picked Up', icon: '📦' },
  { id: 'delivering', label: 'In Transit', icon: '🚚' },
  { id: 'delivered', label: 'Delivered', icon: '✓' },
];

export const OrderStatusStepper: React.FC<OrderStatusStepperProps> = ({
  currentStatus,
  className = '',
}) => {
  // Get current step index
  const currentStepIndex = ORDER_STEPS.findIndex(
    (step) => step.id === currentStatus
  );

  // Check if step is completed
  const isStepCompleted = (stepIndex: number): boolean => {
    return stepIndex <= currentStepIndex;
  };

  // Check if step is current
  const isStepCurrent = (stepIndex: number): boolean => {
    return stepIndex === currentStepIndex;
  };

  // Get status badge for current status
  const getStatusBadge = () => {
    if (currentStatus === 'delivered') {
      return (
        <span className="inline-flex items-center px-3 py-1 rounded-md text-sm font-medium bg-green-100 text-green-800">
          Delivered
        </span>
      );
    }
    if (currentStatus === 'cancelled') {
      return (
        <span className="inline-flex items-center px-3 py-1 rounded-md text-sm font-medium bg-red-100 text-red-800">
          Cancelled
        </span>
      );
    }
    return null;
  };

  return (
    <div className={`bg-white ${className}`}>
      {/* Header with title and status badge */}
      <div className="flex items-center justify-between mb-8">
        <h2 className="text-xl font-semibold text-gray-900">Order Status</h2>
        {getStatusBadge()}
      </div>

      {/* Stepper */}
      <div className="relative">
        {/* Steps container */}
        <div className="flex items-center justify-between">
          {ORDER_STEPS.map((step, index) => {
            const isCompleted = isStepCompleted(index);
            const isCurrent = isStepCurrent(index);
            const isLast = index === ORDER_STEPS.length - 1;

            return (
              <div key={step.id} className="flex-1 relative">
                <div className="flex flex-col items-center">
                  {/* Step circle */}
                  <div
                    className={`
                      relative z-10 flex items-center justify-center w-12 h-12 rounded-full border-2
                      ${
                        isCompleted
                          ? 'bg-blue-600 border-blue-600'
                          : 'bg-white border-gray-300'
                      }
                    `}
                  >
                    <span
                      className={`text-lg ${
                        isCompleted ? 'text-white' : 'text-gray-400'
                      }`}
                    >
                      {isCompleted && step.id === 'delivered' ? '✓' : step.icon}
                    </span>
                  </div>

                  {/* Step label */}
                  <div className="mt-3 text-center">
                    <p
                      className={`text-sm font-medium ${
                        isCompleted ? 'text-gray-900' : 'text-gray-500'
                      }`}
                    >
                      {step.label}
                    </p>
                  </div>
                </div>

                {/* Connecting line */}
                {!isLast && (
                  <div
                    className="absolute top-6 left-1/2 w-full h-0.5 -z-0"
                    style={{ transform: 'translateY(-50%)' }}
                  >
                    <div
                      className={`h-full ${
                        isStepCompleted(index + 1)
                          ? 'bg-blue-600'
                          : 'bg-gray-300'
                      }`}
                    />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Email notification banner */}
      <div className="mt-6 flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
        <span className="text-blue-600 text-xl">✉️</span>
        <p className="text-sm text-gray-700">
          Email notifications are being sent for each status update
        </p>
      </div>
    </div>
  );
};
```

```typescript
// frontend/src/components/features/order/OrderStatusStepper/index.ts

export { OrderStatusStepper } from './OrderStatusStepper';
```

**Acceptance Criteria:**

- [ ] Component renders 5 order stages
- [ ] Current status is visually highlighted with blue circle
- [ ] Completed stages show blue filled circles
- [ ] Pending stages show gray outlined circles
- [ ] Connecting lines show progress between stages
- [ ] Status badge displays at top right (e.g., "Delivered")
- [ ] Email notification banner shows below stepper
- [ ] Responsive design works on mobile/tablet/desktop
- [ ] TypeScript types are properly defined
- [ ] Component is exported via index.ts

---

### Task 9.2: Create OrderProductCard Component

**Status:** ⬜ Not Started
**Depends On:** None
**Estimated Time:** 1-1.5 hours

**Description:**

Create a component to display product information within the order detail page, including product image, name, quantity, and price.

**Figma Design Requirements:**

- **Layout:** Horizontal card with image on left, details on right
- **Image:** Product thumbnail (80x80px)
- **Product Name:** Medium font weight
- **Quantity:** "Quantity: X" in gray text
- **Price:** Bold, displayed below quantity

**Files to Create:**

```
frontend/src/components/features/order/OrderProductCard/
├── OrderProductCard.tsx
├── OrderProductCard.test.tsx
└── index.ts
```

**Implementation Guide:**

```typescript
// frontend/src/components/features/order/OrderProductCard/OrderProductCard.tsx

import React from 'react';

interface OrderProduct {
  id: string;
  name: string;
  price: number;
  quantity: number;
  imageUrl: string;
}

interface OrderProductCardProps {
  product: OrderProduct;
  className?: string;
}

export const OrderProductCard: React.FC<OrderProductCardProps> = ({
  product,
  className = '',
}) => {
  const formatPrice = (price: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD',
    }).format(price);
  };

  return (
    <div className={`flex items-center gap-4 ${className}`}>
      {/* Product Image */}
      <div className="flex-shrink-0">
        <img
          src={product.imageUrl}
          alt={product.name}
          className="w-20 h-20 object-cover rounded-lg"
        />
      </div>

      {/* Product Details */}
      <div className="flex-1">
        <h3 className="text-base font-medium text-gray-900">
          {product.name}
        </h3>
        <p className="text-sm text-gray-600 mt-1">
          Quantity: {product.quantity}
        </p>
        <p className="text-base font-semibold text-gray-900 mt-1">
          {formatPrice(product.price)}
        </p>
      </div>
    </div>
  );
};
```

```typescript
// frontend/src/components/features/order/OrderProductCard/index.ts

export { OrderProductCard } from './OrderProductCard';
```

**Acceptance Criteria:**

- [ ] Component displays product image (80x80px, rounded)
- [ ] Product name is displayed with medium font weight
- [ ] Quantity is shown in gray text
- [ ] Price is formatted as currency with bold font
- [ ] Layout is horizontal (image left, details right)
- [ ] Component accepts className prop for custom styling
- [ ] Price formatting uses Intl.NumberFormat
- [ ] TypeScript types are properly defined
- [ ] Component is exported via index.ts

---

### Task 9.3: Create OrderInfoPanel Component

**Status:** ⬜ Not Started
**Depends On:** None
**Estimated Time:** 1.5-2 hours

**Description:**

Create a sidebar component that displays order metadata including Order ID, Order Date, Last Updated timestamp, and Shipping Address.

**Figma Design Requirements:**

- **Section Title:** "Order Details"
- **Fields:**
  - Order ID (e.g., "ORD-176060492528")
  - Order Date (formatted date and time)
  - Last Updated (formatted date and time)
  - Shipping Address (multi-line)
- **Styling:** Clean, structured layout with labels and values

**Files to Create:**

```
frontend/src/components/features/order/OrderInfoPanel/
├── OrderInfoPanel.tsx
├── OrderInfoPanel.test.tsx
└── index.ts
```

**Implementation Guide:**

```typescript
// frontend/src/components/features/order/OrderInfoPanel/OrderInfoPanel.tsx

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import type { ShippingInfo } from '@/types';

interface OrderInfoPanelProps {
  orderId: string;
  orderDate: string;
  lastUpdated: string;
  shippingInfo: ShippingInfo;
  className?: string;
}

export const OrderInfoPanel: React.FC<OrderInfoPanelProps> = ({
  orderId,
  orderDate,
  lastUpdated,
  shippingInfo,
  className = '',
}) => {
  // Format date and time
  const formatDateTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
    });
  };

  // Format shipping address
  const formatAddress = (info: ShippingInfo): string => {
    return `${info.addressLine1}, ${info.city}, ${info.state} ${info.postcode}`;
  };

  return (
    <Card className={className}>
      <CardHeader>
        <h2 className="text-lg font-semibold text-gray-900">Order Details</h2>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {/* Order ID */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Order ID</p>
            <p className="text-base font-medium text-gray-900">{orderId}</p>
          </div>

          {/* Order Date */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Order Date</p>
            <p className="text-base text-gray-900">
              {formatDateTime(orderDate)}
            </p>
          </div>

          {/* Last Updated */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Last Updated</p>
            <p className="text-base text-gray-900">
              {formatDateTime(lastUpdated)}
            </p>
          </div>

          {/* Shipping Address */}
          <div>
            <p className="text-sm text-gray-600 mb-1">Shipping Address</p>
            <p className="text-base text-gray-900">
              {formatAddress(shippingInfo)}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
```

```typescript
// frontend/src/components/features/order/OrderInfoPanel/index.ts

export { OrderInfoPanel } from './OrderInfoPanel';
```

**Acceptance Criteria:**

- [ ] Component displays Order ID prominently
- [ ] Order Date is formatted correctly (e.g., "October 16, 2025 at 07:55 PM")
- [ ] Last Updated timestamp is formatted consistently
- [ ] Shipping Address is displayed with proper formatting
- [ ] Uses Card component for consistent styling
- [ ] Field labels are gray and values are darker for hierarchy
- [ ] Spacing between fields is consistent
- [ ] TypeScript types are properly defined
- [ ] Component is exported via index.ts

---

### Task 9.4: Create NeedHelpSection Component

**Status:** ⬜ Not Started
**Depends On:** None
**Estimated Time:** 45 minutes - 1 hour

**Description:**

Create a "Need Help?" section with a Contact Support button and availability message.

**Figma Design Requirements:**

- **Section Title:** "Need Help?"
- **Button:** "Contact Support" (white background, border)
- **Availability Text:** "Available 24/7 to assist you" (gray, small text)

**Files to Create:**

```
frontend/src/components/features/order/NeedHelpSection/
├── NeedHelpSection.tsx
├── NeedHelpSection.test.tsx
└── index.ts
```

**Implementation Guide:**

```typescript
// frontend/src/components/features/order/NeedHelpSection/NeedHelpSection.tsx

import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { Button } from '@/components/ui/Button/Button';

interface NeedHelpSectionProps {
  onContactSupport?: () => void;
  className?: string;
}

export const NeedHelpSection: React.FC<NeedHelpSectionProps> = ({
  onContactSupport,
  className = '',
}) => {
  const handleContactSupport = () => {
    if (onContactSupport) {
      onContactSupport();
    } else {
      // Default behavior: open support email or chat
      window.location.href = 'mailto:support@shophub.com';
    }
  };

  return (
    <Card className={className}>
      <CardHeader>
        <h3 className="text-lg font-semibold text-gray-900">Need Help?</h3>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          <Button
            variant="outline"
            className="w-full"
            onClick={handleContactSupport}
          >
            Contact Support
          </Button>
          <p className="text-sm text-gray-600 text-center">
            Available 24/7 to assist you
          </p>
        </div>
      </CardContent>
    </Card>
  );
};
```

```typescript
// frontend/src/components/features/order/NeedHelpSection/index.ts

export { NeedHelpSection } from './NeedHelpSection';
```

**Acceptance Criteria:**

- [ ] Component displays "Need Help?" title
- [ ] "Contact Support" button is full-width with outline variant
- [ ] Availability text is centered and gray
- [ ] Button click triggers support action (email or callback)
- [ ] Uses Card component for consistent styling
- [ ] TypeScript types are properly defined
- [ ] Component is exported via index.ts

---

## Section 2: Page Implementation

### Task 9.5: Create OrderDetail Page

**Status:** ⬜ Not Started
**Depends On:** Tasks 9.1, 9.2, 9.3, 9.4
**Estimated Time:** 2-3 hours

**Description:**

Create the main Order Detail page that integrates all components and fetches order data from the API.

**Files to Create:**

```
frontend/src/pages/OrderDetail.tsx
frontend/src/pages/OrderDetail.test.tsx
```

**Implementation Guide:**

```typescript
// frontend/src/pages/OrderDetail.tsx

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { orderService } from '@/api/services/order.service';
import type { Order } from '@/types';
import { OrderStatusStepper } from '@/components/features/order/OrderStatusStepper';
import { OrderProductCard } from '@/components/features/order/OrderProductCard';
import { OrderInfoPanel } from '@/components/features/order/OrderInfoPanel';
import { NeedHelpSection } from '@/components/features/order/NeedHelpSection';
import { Card, CardContent } from '@/components/ui/Card/Card';
import { Spinner } from '@/components/ui/Spinner';
import { Button } from '@/components/ui/Button/Button';

/**
 * OrderDetail Page
 * Displays comprehensive order information with status tracking
 */
const OrderDetail: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();

  const [order, setOrder] = useState<Order | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch order details
  useEffect(() => {
    const fetchOrder = async () => {
      if (!orderId) {
        setError('Invalid order ID');
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);
        const orderData = await orderService.getOrder(orderId);
        setOrder(orderData);
      } catch (err) {
        console.error('Error fetching order:', err);
        setError('Failed to load order details. Please try again.');
      } finally {
        setIsLoading(false);
      }
    };

    fetchOrder();
  }, [orderId]);

  // Loading state
  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <Spinner size="lg" />
        </div>
      </div>
    );
  }

  // Error state
  if (error || !order) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            Error Loading Order
          </h3>
          <p className="text-red-600 mb-4">{error || 'Order not found'}</p>
          <Button onClick={() => navigate('/orders')} variant="primary">
            Back to Orders
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-7xl">
      {/* Back navigation */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/orders')}
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
        >
          <span>←</span>
          <span>Back to Orders</span>
        </button>
      </div>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left column - Order status and product info */}
        <div className="lg:col-span-2 space-y-6">
          {/* Order Status Stepper */}
          <Card>
            <CardContent className="p-6">
              <OrderStatusStepper currentStatus={order.status} />
            </CardContent>
          </Card>

          {/* Product Information */}
          <Card>
            <CardContent className="p-6">
              {order.products.map((product) => (
                <OrderProductCard key={product.id} product={product} />
              ))}
            </CardContent>
          </Card>
        </div>

        {/* Right column - Order details and support */}
        <div className="space-y-6">
          {/* Order Info Panel */}
          <OrderInfoPanel
            orderId={order.id}
            orderDate={order.createdAt}
            lastUpdated={order.updatedAt}
            shippingInfo={order.shippingInfo}
          />

          {/* Need Help Section */}
          <NeedHelpSection />
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;
```

**Acceptance Criteria:**

- [ ] Page fetches order data using `orderService.getOrder()`
- [ ] Loading state shows centered spinner
- [ ] Error state shows error message with "Back to Orders" button
- [ ] "Back to Orders" navigation works correctly
- [ ] OrderStatusStepper displays current order status
- [ ] All products in order are displayed with OrderProductCard
- [ ] OrderInfoPanel shows order metadata
- [ ] NeedHelpSection renders in sidebar
- [ ] Layout is responsive (2-column on desktop, stacked on mobile)
- [ ] TypeScript has no errors
- [ ] Component handles missing orderId gracefully

---

### Task 9.6: Configure Route for OrderDetail Page

**Status:** ⬜ Not Started
**Depends On:** Task 9.5
**Estimated Time:** 15-20 minutes

**Description:**

Add the OrderDetail page route to the application router configuration.

**Files to Modify:**

```
frontend/src/App.tsx (or routes configuration file)
```

**Implementation Guide:**

```typescript
// In your routes configuration (App.tsx or routes file)

import OrderDetail from '@/pages/OrderDetail';

// Add this route to your router
{
  path: '/orders/:orderId',
  element: (
    <ProtectedRoute requireAuth>
      <OrderDetail />
    </ProtectedRoute>
  ),
}
```

**Acceptance Criteria:**

- [ ] Route `/orders/:orderId` is configured
- [ ] Route requires authentication (wrapped in ProtectedRoute)
- [ ] Route parameter `orderId` is accessible in component
- [ ] Navigating to `/orders/123` loads the OrderDetail page
- [ ] 404 page shows for invalid routes

---

### Task 9.7: Add Navigation from Order History to Order Detail

**Status:** ⬜ Not Started
**Depends On:** Task 9.6
**Estimated Time:** 30 minutes

**Description:**

Update the Order History page to include links/buttons that navigate to the Order Detail page.

**Files to Modify:**

```
frontend/src/pages/OrderHistory.tsx (if exists)
frontend/src/components/features/order/OrderListItem.tsx (if exists)
```

**Implementation Guide:**

```typescript
// Example: In OrderListItem or OrderHistory component

import { useNavigate } from 'react-router-dom';

const OrderListItem: React.FC<{ order: Order }> = ({ order }) => {
  const navigate = useNavigate();

  const handleViewDetails = () => {
    navigate(`/orders/${order.id}`);
  };

  return (
    <div className="border rounded-lg p-4">
      {/* Order summary */}
      <div className="flex justify-between items-center">
        <div>
          <h3 className="font-medium">Order {order.id}</h3>
          <p className="text-sm text-gray-600">{order.createdAt}</p>
        </div>
        <Button onClick={handleViewDetails} variant="outline" size="sm">
          View Details
        </Button>
      </div>
    </div>
  );
};
```

**Acceptance Criteria:**

- [ ] "View Details" button added to order list items
- [ ] Button click navigates to `/orders/:orderId`
- [ ] Navigation preserves order ID correctly
- [ ] User can navigate back to order history from detail page
- [ ] Navigation is accessible (keyboard and screen readers)

---

## Section 3: Styling & Polish

### Task 9.8: Align Design with Figma Specifications

**Status:** ⬜ Not Started
**Depends On:** Tasks 9.1-9.7
**Estimated Time:** 1-2 hours

**Description:**

Fine-tune component styling to match the Figma design pixel-perfect, including colors, spacing, typography, and layout.

**Figma Design Checklist:**

**Colors:**
- [ ] Primary blue: `#3B82F6` (or matching Tailwind `blue-600`)
- [ ] Green badge: `#10B981` background, `#065F46` text
- [ ] Gray text: `#6B7280` for labels, `#111827` for values
- [ ] White background cards: `#FFFFFF`

**Typography:**
- [ ] Page title: 24px, font-semibold
- [ ] Section headers: 18px, font-semibold
- [ ] Body text: 14px, regular
- [ ] Labels: 12px, gray-600
- [ ] Values: 14px, gray-900

**Spacing:**
- [ ] Card padding: 24px (p-6)
- [ ] Section gaps: 24px (gap-6)
- [ ] Element spacing: 16px (space-y-4)
- [ ] Container max-width: 1280px

**Layout:**
- [ ] Desktop: 2-column grid (2/3 left, 1/3 right)
- [ ] Tablet: Stacked layout
- [ ] Mobile: Full-width stacked

**Components:**
- [ ] Status stepper circles: 48px diameter
- [ ] Product image: 80x80px, rounded-lg
- [ ] Buttons: Full-width in sidebar, outline variant
- [ ] Badges: Rounded-md, small padding

**Files to Modify:**

All component files from Tasks 9.1-9.4

**Acceptance Criteria:**

- [ ] All colors match Figma design
- [ ] Typography sizes and weights are correct
- [ ] Spacing and padding match design
- [ ] Component proportions are accurate
- [ ] Design is pixel-perfect on desktop
- [ ] No visual regressions from Figma

---

### Task 9.9: Implement Responsive Design

**Status:** ⬜ Not Started
**Depends On:** Task 9.8
**Estimated Time:** 1-1.5 hours

**Description:**

Ensure the Order Detail page is fully responsive across mobile, tablet, and desktop devices.

**Responsive Breakpoints:**

- **Mobile:** < 640px (sm)
- **Tablet:** 640px - 1024px (md, lg)
- **Desktop:** > 1024px (xl)

**Responsive Requirements:**

**Mobile (< 640px):**
- [ ] Stacked layout (single column)
- [ ] Status stepper shows vertically or with smaller circles
- [ ] Product images scale appropriately
- [ ] Buttons are full-width
- [ ] Text is readable at 16px minimum

**Tablet (640px - 1024px):**
- [ ] Single column or 2-column with breakpoint
- [ ] Status stepper shows all steps
- [ ] Comfortable touch targets (44px minimum)

**Desktop (> 1024px):**
- [ ] 2-column grid layout (2/3 and 1/3)
- [ ] Maximum container width: 1280px
- [ ] Optimal line lengths for readability

**Testing Checklist:**

- [ ] Test on Chrome DevTools device emulation
- [ ] Test on actual mobile device (iOS/Android)
- [ ] Test on tablet (iPad or similar)
- [ ] Test on desktop (1920px, 1440px, 1280px)
- [ ] Test landscape and portrait orientations
- [ ] No horizontal scrolling on any device
- [ ] Touch targets are accessible on mobile

**Acceptance Criteria:**

- [ ] Page is fully responsive on all breakpoints
- [ ] No layout breaking on small screens
- [ ] Touch targets meet accessibility standards
- [ ] Images scale without distortion
- [ ] Text is readable on all devices
- [ ] Navigation is accessible on mobile

---

## Section 4: Testing & Validation

### Task 9.10: Write Component Unit Tests

**Status:** ⬜ Not Started
**Depends On:** Tasks 9.1-9.4
**Estimated Time:** 1.5-2 hours

**Description:**

Write comprehensive unit tests for all Order Detail components.

**Files to Create:**

- `OrderStatusStepper.test.tsx`
- `OrderProductCard.test.tsx`
- `OrderInfoPanel.test.tsx`
- `NeedHelpSection.test.tsx`

**Test Cases:**

**OrderStatusStepper:**
```typescript
// frontend/src/components/features/order/OrderStatusStepper/OrderStatusStepper.test.tsx

import { render, screen } from '@testing-library/react';
import { OrderStatusStepper } from './OrderStatusStepper';

describe('OrderStatusStepper', () => {
  it('renders all 5 order stages', () => {
    render(<OrderStatusStepper currentStatus="processing" />);

    expect(screen.getByText('Order Placed')).toBeInTheDocument();
    expect(screen.getByText('Processing')).toBeInTheDocument();
    expect(screen.getByText('Picked Up')).toBeInTheDocument();
    expect(screen.getByText('In Transit')).toBeInTheDocument();
    expect(screen.getByText('Delivered')).toBeInTheDocument();
  });

  it('highlights completed stages', () => {
    const { container } = render(
      <OrderStatusStepper currentStatus="picked_up" />
    );

    // First 3 stages should be completed (blue)
    const completedCircles = container.querySelectorAll('.bg-blue-600');
    expect(completedCircles).toHaveLength(3);
  });

  it('shows delivered badge when status is delivered', () => {
    render(<OrderStatusStepper currentStatus="delivered" />);

    expect(screen.getByText('Delivered')).toHaveClass('bg-green-100');
  });

  it('shows email notification banner', () => {
    render(<OrderStatusStepper currentStatus="processing" />);

    expect(
      screen.getByText(/Email notifications are being sent/i)
    ).toBeInTheDocument();
  });
});
```

**OrderProductCard:**
```typescript
// frontend/src/components/features/order/OrderProductCard/OrderProductCard.test.tsx

import { render, screen } from '@testing-library/react';
import { OrderProductCard } from './OrderProductCard';

describe('OrderProductCard', () => {
  const mockProduct = {
    id: 'PRD-001',
    name: 'Wireless Mouse',
    price: 63.98,
    quantity: 1,
    imageUrl: '/images/mouse.jpg',
  };

  it('renders product information', () => {
    render(<OrderProductCard product={mockProduct} />);

    expect(screen.getByText('Wireless Mouse')).toBeInTheDocument();
    expect(screen.getByText('Quantity: 1')).toBeInTheDocument();
    expect(screen.getByText('$63.98')).toBeInTheDocument();
  });

  it('displays product image', () => {
    render(<OrderProductCard product={mockProduct} />);

    const image = screen.getByAltText('Wireless Mouse');
    expect(image).toHaveAttribute('src', '/images/mouse.jpg');
  });

  it('formats price correctly', () => {
    render(<OrderProductCard product={mockProduct} />);

    expect(screen.getByText('$63.98')).toBeInTheDocument();
  });
});
```

**Coverage Goals:**

- [ ] All components have > 80% code coverage
- [ ] All props are tested
- [ ] Edge cases are covered (null, undefined, empty data)
- [ ] Accessibility is tested (ARIA labels, roles)

**Acceptance Criteria:**

- [ ] All component tests pass
- [ ] Test coverage is > 80%
- [ ] Tests are readable and maintainable
- [ ] Tests document component behavior
- [ ] No console errors or warnings in tests

---

### Task 9.11: Integration Testing & End-to-End Validation

**Status:** ⬜ Not Started
**Depends On:** Task 9.10
**Estimated Time:** 1-1.5 hours

**Description:**

Perform integration testing and end-to-end validation of the complete Order Detail page flow.

**Manual Testing Checklist:**

**Happy Path:**
- [ ] Navigate to Order History page
- [ ] Click "View Details" on an order
- [ ] Order Detail page loads successfully
- [ ] Order status stepper shows correct stage
- [ ] Product information displays correctly
- [ ] Order details panel shows all metadata
- [ ] Shipping address is formatted properly
- [ ] "Contact Support" button works
- [ ] "Back to Orders" navigation works

**Error Handling:**
- [ ] Invalid order ID shows error message
- [ ] Network error shows error state
- [ ] Empty/null order data is handled gracefully
- [ ] User can recover from errors

**Edge Cases:**
- [ ] Order with multiple products displays all
- [ ] Cancelled order shows correct status
- [ ] Very long product names don't break layout
- [ ] Very long addresses don't break layout

**Cross-Browser Testing:**
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)

**Accessibility Testing:**
- [ ] Keyboard navigation works
- [ ] Screen reader announces content correctly
- [ ] Focus indicators are visible
- [ ] Color contrast meets WCAG AA standards

**Performance Testing:**
- [ ] Page loads in < 2 seconds
- [ ] No unnecessary re-renders
- [ ] Images load efficiently
- [ ] API calls are optimized

**Acceptance Criteria:**

- [ ] All manual tests pass
- [ ] No console errors or warnings
- [ ] Accessibility audit passes (Lighthouse > 90)
- [ ] Performance audit passes (Lighthouse > 90)
- [ ] All browsers render correctly
- [ ] User flow is intuitive and smooth

---

## Phase 9 Completion Checklist

Before marking this phase as complete, verify:

### Components
- [ ] OrderStatusStepper component created and tested
- [ ] OrderProductCard component created and tested
- [ ] OrderInfoPanel component created and tested
- [ ] NeedHelpSection component created and tested

### Page Implementation
- [ ] OrderDetail page created and functional
- [ ] Route `/orders/:orderId` configured
- [ ] Navigation from Order History works

### Styling & Design
- [ ] Design matches Figma specifications
- [ ] Responsive design works on all devices
- [ ] Colors, typography, spacing are pixel-perfect

### Testing & Quality
- [ ] Unit tests written and passing (> 80% coverage)
- [ ] Integration testing completed
- [ ] Manual testing checklist completed
- [ ] Accessibility standards met
- [ ] Performance benchmarks met

### Documentation
- [ ] Components are documented with JSDoc
- [ ] README updated with new components
- [ ] Code is clean and follows standards

---

## Common Issues & Solutions

### Issue 1: Order Status Not Updating Visually

**Problem:** Status stepper doesn't reflect current order status

**Solution:**
- Verify `currentStatus` prop is passed correctly
- Check ORDER_STEPS array includes all status values
- Ensure status mapping is case-sensitive

### Issue 2: API Data Not Loading

**Problem:** Order data fails to load from API

**Solution:**
- Check API endpoint configuration
- Verify authentication token is valid
- Check network tab for CORS errors
- Add error logging to orderService

### Issue 3: Layout Breaking on Mobile

**Problem:** Components overflow or don't stack properly on mobile

**Solution:**
- Use Tailwind responsive classes (`sm:`, `md:`, `lg:`)
- Test with Chrome DevTools device emulation
- Ensure max-width is set on container
- Check for hardcoded pixel widths

### Issue 4: Date Formatting Issues

**Problem:** Dates show incorrectly or in wrong timezone

**Solution:**
- Use `Intl.DateTimeFormat` or `toLocaleString()`
- Ensure backend sends ISO 8601 format
- Consider user's timezone preferences
- Test with different date formats

---

## Next Steps

After completing Phase 9:

1. **Phase 10:** Order History List Page (if not yet implemented)
2. **Phase 11:** Order Cancellation Feature
3. **Phase 12:** Real-time Order Status Updates (WebSockets)
4. **Phase 13:** Email Notification Preferences

---

## Resources

**Documentation:**
- [React Router - useParams](https://reactrouter.com/en/main/hooks/use-params)
- [Tailwind CSS - Responsive Design](https://tailwindcss.com/docs/responsive-design)
- [TypeScript - Type Definitions](https://www.typescriptlang.org/docs/handbook/2/types-from-types.html)

**Internal Docs:**
- `docs/frontend/ARCHITECTURE.md` - Component patterns
- `docs/frontend/UI_DESIGN_SYSTEM.md` - Design tokens
- `docs/frontend/API_INTEGRATION.md` - API specs
- `docs/SYSTEM_INTERFACE_SPEC.md` - Backend contracts

**Figma Design:**
- Order Detail Page UI (provided screenshot)

---

**Phase Status:** ⬜ Not Started
**Created:** November 1, 2025
**Last Updated:** November 1, 2025
**Estimated Completion:** 1-2 days

---

**Good luck with the implementation! 🚀**
