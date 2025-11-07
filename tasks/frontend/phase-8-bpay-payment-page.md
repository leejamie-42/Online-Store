# Phase 8: BPAY Payment Page Implementation

**Status**: ⬜ Not Started
**Estimated Time**: 5-7 hours
**Priority**: High
**Dependencies**: Phase 7 (Checkout Details Page)

## 📋 Phase Overview

Implement the third step of the multi-step checkout flow: the **BPAY Payment Page**. This page displays BPAY payment instructions after the order is created, allowing users to complete payment through their bank.

### Checkout Flow Architecture
```
Step 1: Review → Step 2: Details → Step 3: Payment (This Phase) → Step 4: Confirmation
```

### Key Features
- **Step Progress Indicator**: Visual guide showing Step 3 of 4 (Steps 1-2 completed with green checkmarks)
- **BPAY Payment Information Card**: Display biller code, reference number, and amount due
- **Copy-to-Clipboard Functionality**: Quick copy buttons for biller code and reference number
- **Payment Instructions**: Clear guidance on how to complete BPAY payment
- **Order Creation**: API call to create order with cart items and shipping info
- **Payment Confirmation**: Button to mark payment as completed and navigate to confirmation

### Design Reference
- **Figma**: [BPAY Payment Page](https://www.figma.com/design/BYlBng6s0iLmcrXxAaWvDt/Store-application?node-id=16-304)
- **Primary Color**: #155dfc (Blue for Step 3 active state)
- **Success Color**: #00c950 (Green for completed steps 1 & 2)
- **Typography**: Inter font family
- **Layout**: Centered card layout with payment details

---

## 📝 Task Breakdown

### Task 1: Type Definitions for Order API ⬜

**Estimated Time**: 30 minutes
**File to Update**: `frontend/src/types/order.types.ts`

**Description**: Add type definitions for BPAY payment details and order creation API responses.

**Implementation**:

```typescript
// Add to existing order.types.ts

/**
 * BPAY Payment Details
 * Returned from backend after order creation
 */
export interface BpayDetails {
  biller_code: string;
  reference_number: string;
  amount: number;
  expires_at: string; // ISO 8601 datetime string
}

/**
 * Order Creation Request
 * Sent to POST /api/orders
 */
export interface CreateOrderRequest {
  product_id: string;
  quantity: number;
  user_id: string;
  shipping_info: {
    receiver_name: string;
    receiver_email: string;
    receiver_phone: string;
    receiver_address: string;
    receiver_city: string;
    receiver_state: string;
    receiver_postcode: string;
  };
}

/**
 * Order Creation Response
 * Received from POST /api/orders
 */
export interface CreateOrderResponse {
  order_id: string;
  status: OrderStatus; // Will be 'pending'
  bpay_details: BpayDetails;
  total_amount: number;
  created_at: string;
}

/**
 * Payment Confirmation Request
 * Sent when user marks payment as completed
 */
export interface ConfirmPaymentRequest {
  order_id: string;
  payment_method: 'BPAY';
  confirmation_type: 'manual'; // User-initiated confirmation
}
```

**Acceptance Criteria**:
- ✅ Types match backend API specification from `docs/SYSTEM_INTERFACE_SPEC.md`
- ✅ BpayDetails interface includes all required fields
- ✅ CreateOrderRequest matches shipping schema from Phase 7
- ✅ All types exported and available for import

---

### Task 2: Order API Service ⬜

**Estimated Time**: 1 hour
**File to Create**: `frontend/src/api/services/order.service.ts`

**Description**: Create API service for order creation and management.

**Implementation**:

```typescript
import { apiClient } from '@/lib/axios';
import type {
  CreateOrderRequest,
  CreateOrderResponse,
  Order,
  ConfirmPaymentRequest,
} from '@/types/order.types';

/**
 * Order API Service
 * Handles all order-related API calls
 */
export const orderService = {
  /**
   * Create new order
   * POST /api/orders
   */
  createOrder: async (
    data: CreateOrderRequest
  ): Promise<CreateOrderResponse> => {
    const response = await apiClient.post<CreateOrderResponse>(
      '/orders',
      data
    );
    return response.data;
  },

  /**
   * Get order by ID
   * GET /api/orders/:id
   */
  getOrder: async (orderId: string): Promise<Order> => {
    const response = await apiClient.get<Order>(`/orders/${orderId}`);
    return response.data;
  },

  /**
   * Get user's orders
   * GET /api/orders
   */
  getUserOrders: async (): Promise<Order[]> => {
    const response = await apiClient.get<Order[]>('/orders');
    return response.data;
  },

  /**
   * Confirm payment (manual confirmation)
   * POST /api/orders/:id/confirm-payment
   */
  confirmPayment: async (
    orderId: string,
    data: ConfirmPaymentRequest
  ): Promise<void> => {
    await apiClient.post(`/orders/${orderId}/confirm-payment`, data);
  },
};
```

**Error Handling**:
```typescript
// Axios interceptor already handles:
// - 401 Unauthorized → Redirect to login
// - 500 Server Error → Display error toast
// - Network errors → Display connection error

// Service-specific errors:
try {
  const order = await orderService.createOrder(data);
  return order;
} catch (error) {
  if (error.response?.status === 400) {
    throw new Error('Invalid order data. Please check your details.');
  }
  if (error.response?.status === 409) {
    throw new Error('Cart items no longer available.');
  }
  throw error;
}
```

**Acceptance Criteria**:
- ✅ Uses apiClient from `@/lib/axios`
- ✅ All methods properly typed with TypeScript
- ✅ Error responses handled appropriately
- ✅ Follows existing API service patterns (e.g., auth.service.ts)

---

### Task 3: Copy-to-Clipboard Hook ⬜

**Estimated Time**: 30 minutes
**File to Create**: `frontend/src/hooks/useClipboard.ts`

**Description**: Create reusable hook for copy-to-clipboard functionality with user feedback.

**Implementation**:

```typescript
import { useState, useCallback } from 'react';

export interface UseClipboardReturn {
  copied: boolean;
  copy: (text: string) => Promise<void>;
  reset: () => void;
}

/**
 * useClipboard Hook
 * Provides copy-to-clipboard functionality with feedback
 *
 * @param timeout - Reset 'copied' state after this many ms (default: 2000)
 */
export function useClipboard(timeout = 2000): UseClipboardReturn {
  const [copied, setCopied] = useState(false);

  const copy = useCallback(async (text: string) => {
    try {
      if (!navigator.clipboard) {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
      } else {
        await navigator.clipboard.writeText(text);
      }

      setCopied(true);

      // Auto-reset after timeout
      setTimeout(() => {
        setCopied(false);
      }, timeout);
    } catch (error) {
      console.error('Failed to copy text:', error);
      setCopied(false);
    }
  }, [timeout]);

  const reset = useCallback(() => {
    setCopied(false);
  }, []);

  return { copied, copy, reset };
}
```

**Usage Example**:
```typescript
const { copied, copy } = useClipboard();

<button onClick={() => copy('93242')}>
  {copied ? 'Copied!' : 'Copy'}
</button>
```

**Acceptance Criteria**:
- ✅ Works in modern browsers with Clipboard API
- ✅ Fallback for older browsers (execCommand)
- ✅ Auto-resets copied state after timeout
- ✅ TypeScript types exported
- ✅ Error handling included

---

### Task 4: BpayPaymentInfo Component ⬜

**Estimated Time**: 1.5 hours
**File to Create**: `frontend/src/components/features/checkout/BpayPaymentInfo/BpayPaymentInfo.tsx`

**Description**: Display BPAY payment details with copy-to-clipboard functionality.

**Component Structure**:

```typescript
import React from 'react';
import { LuCopy, LuCheck } from 'react-icons/lu';
import { Card } from '@/components/ui/Card/Card';
import { Button } from '@/components/ui/Button/Button';
import { useClipboard } from '@/hooks/useClipboard';
import type { BpayDetails } from '@/types/order.types';

export interface BpayPaymentInfoProps {
  bpayDetails: BpayDetails;
}

/**
 * BpayPaymentInfo Component
 * Displays BPAY payment information with copy buttons
 *
 * Design: Gray background card with three sections:
 * 1. Biller Code with copy button
 * 2. Reference Number with copy button
 * 3. Amount Due (no copy button)
 * 4. Blue info box with payment instructions
 */
export const BpayPaymentInfo: React.FC<BpayPaymentInfoProps> = ({
  bpayDetails,
}) => {
  const billerCodeClipboard = useClipboard();
  const referenceClipboard = useClipboard();

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="text-center space-y-2">
        <h2 className="text-2xl font-semibold text-gray-900">
          BPAY Payment Information
        </h2>
        <p className="text-base text-gray-600">
          Use the following details to complete your payment
        </p>
      </div>

      {/* Payment Details Card */}
      <Card className="p-6 bg-gray-50 border-gray-200">
        <div className="space-y-4">
          {/* Biller Code */}
          <div className="flex items-center justify-between border-b border-gray-200 pb-4">
            <div className="space-y-1">
              <p className="text-sm text-gray-600">Biller Code</p>
              <p className="text-2xl font-normal text-gray-900">
                {bpayDetails.biller_code}
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() => billerCodeClipboard.copy(bpayDetails.biller_code)}
              className="flex items-center gap-2"
            >
              {billerCodeClipboard.copied ? (
                <>
                  <LuCheck className="w-4 h-4" />
                  Copied
                </>
              ) : (
                <>
                  <LuCopy className="w-4 h-4" />
                  Copy
                </>
              )}
            </Button>
          </div>

          {/* Reference Number */}
          <div className="flex items-center justify-between border-b border-gray-200 pb-4">
            <div className="space-y-1">
              <p className="text-sm text-gray-600">Reference Number</p>
              <p className="text-xl font-normal text-gray-900">
                {bpayDetails.reference_number}
              </p>
            </div>
            <Button
              variant="outline"
              size="sm"
              onClick={() =>
                referenceClipboard.copy(bpayDetails.reference_number)
              }
              className="flex items-center gap-2"
            >
              {referenceClipboard.copied ? (
                <>
                  <LuCheck className="w-4 h-4" />
                  Copied
                </>
              ) : (
                <>
                  <LuCopy className="w-4 h-4" />
                  Copy
                </>
              )}
            </Button>
          </div>

          {/* Amount Due */}
          <div className="space-y-1">
            <p className="text-sm text-gray-600">Amount Due</p>
            <p className="text-2xl font-normal text-primary-600">
              ${bpayDetails.amount.toFixed(2)} AUD
            </p>
          </div>
        </div>

        {/* Payment Instructions Note */}
        <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg space-y-2">
          <p className="text-sm text-gray-900">
            <strong className="font-bold">Note:</strong> Pay via your banking
            app/website within 24 hours.
          </p>
          <p className="text-sm text-gray-600">
            We'll email you when payment is confirmed.
          </p>
        </div>
      </Card>
    </div>
  );
};
```

**Layout Details** (from Figma):
- **Card Background**: `bg-gray-50`, `border-gray-200`
- **Biller Code**: Large text (text-2xl), 24px font size
- **Reference Number**: Medium text (text-xl), 20px font size
- **Amount**: Large text (text-2xl), primary blue color
- **Copy Buttons**: Outline variant, white background, bordered
- **Info Box**: Blue background (#eff6ff), blue border (#bedbff)
- **Borders**: Between sections using `border-b border-gray-200`

**Acceptance Criteria**:
- ✅ Displays all BPAY details correctly
- ✅ Copy buttons work and show "Copied!" feedback
- ✅ Amount formatted with 2 decimal places and AUD
- ✅ Blue info box matches Figma design
- ✅ Responsive layout (mobile-friendly)

---

### Task 5: useBpayPayment Hook ⬜

**Estimated Time**: 1 hour
**File to Create**: `frontend/src/hooks/useBpayPayment.ts`

**Description**: Custom hook to manage BPAY payment flow logic and order creation.

**Implementation**:

```typescript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCartStore } from '@/stores/cart.store';
import { useAuth } from '@/context/AuthContext';
import { orderService } from '@/api/services/order.service';
import { ROUTES } from '@/config/routes';
import type { CreateOrderResponse } from '@/types/order.types';

export interface UseBpayPaymentReturn {
  bpayDetails: CreateOrderResponse | null;
  isLoading: boolean;
  error: string | null;
  createOrder: () => Promise<void>;
  confirmPayment: () => Promise<void>;
}

/**
 * useBpayPayment Hook
 * Manages BPAY payment flow:
 * 1. Create order with cart items and shipping info
 * 2. Display BPAY payment details
 * 3. Handle payment confirmation
 */
export function useBpayPayment(): UseBpayPaymentReturn {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { items, shippingInfo, clearCart } = useCartStore();

  const [bpayDetails, setBpayDetails] = useState<CreateOrderResponse | null>(
    null
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Create order and get BPAY payment details
   */
  const createOrder = async () => {
    try {
      setIsLoading(true);
      setError(null);

      // Validation
      if (!user) {
        throw new Error('User not authenticated');
      }

      if (items.length === 0) {
        throw new Error('Cart is empty');
      }

      if (!shippingInfo) {
        throw new Error('Shipping information missing');
      }

      // Currently only support single item in cart
      const cartItem = items[0];

      // Create order request
      const orderData = {
        product_id: cartItem.product.id,
        quantity: cartItem.quantity,
        user_id: user.id,
        shipping_info: shippingInfo,
      };

      // Call API
      const response = await orderService.createOrder(orderData);
      setBpayDetails(response);
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Failed to create order';
      setError(message);
      console.error('Order creation error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Confirm payment and navigate to confirmation
   */
  const confirmPayment = async () => {
    try {
      setIsLoading(true);
      setError(null);

      if (!bpayDetails) {
        throw new Error('No order details available');
      }

      // Optional: Call confirm payment endpoint
      // await orderService.confirmPayment(bpayDetails.order_id, {
      //   order_id: bpayDetails.order_id,
      //   payment_method: 'BPAY',
      //   confirmation_type: 'manual',
      // });

      // Clear cart and navigate to confirmation
      clearCart();
      navigate(ROUTES.ORDER_CONFIRMATION(bpayDetails.order_id));
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Failed to confirm payment';
      setError(message);
      console.error('Payment confirmation error:', err);
    } finally {
      setIsLoading(false);
    }
  };

  return {
    bpayDetails,
    isLoading,
    error,
    createOrder,
    confirmPayment,
  };
}
```

**Hook Features**:
- **Order Creation**: Validates cart, shipping info, creates order via API
- **Error Handling**: User-friendly error messages for common failures
- **Loading States**: Manages loading state for UI feedback
- **Navigation**: Automatic redirect to confirmation after payment
- **Cart Clearing**: Clears cart after successful payment confirmation

**Acceptance Criteria**:
- ✅ Validates all required data before API call
- ✅ Handles errors gracefully with clear messages
- ✅ Returns loading state for UI components
- ✅ Clears cart and navigates after confirmation
- ✅ TypeScript types fully defined

---

### Task 6: BpayPaymentPage ⬜

**Estimated Time**: 2 hours
**File to Create**: `frontend/src/pages/checkout/BpayPaymentPage.tsx`

**Description**: Main BPAY payment page integrating all components with order creation flow.

**Page Structure**:

```typescript
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { LuCheckCircle, LuAlertCircle } from 'react-icons/lu';
import { Button } from '@/components/ui/Button/Button';
import { Alert, AlertDescription } from '@/components/ui/Alert/Alert';
import { StepIndicator } from '@/components/features/checkout/StepIndicator/StepIndicator';
import { BpayPaymentInfo } from '@/components/features/checkout/BpayPaymentInfo/BpayPaymentInfo';
import { useBpayPayment } from '@/hooks/useBpayPayment';
import { useCartStore } from '@/stores/cart.store';
import { CHECKOUT_STEPS } from '@/config/checkout.constants';
import { ROUTES } from '@/config/routes';

/**
 * BpayPaymentPage Component
 * Step 3 of 4: Display BPAY payment instructions and handle order creation
 */
export const BpayPaymentPage: React.FC = () => {
  const navigate = useNavigate();
  const { items, shippingInfo } = useCartStore();
  const { bpayDetails, isLoading, error, createOrder, confirmPayment } =
    useBpayPayment();

  // Create order on page mount
  useEffect(() => {
    if (!bpayDetails && !isLoading && !error) {
      createOrder();
    }
  }, [bpayDetails, isLoading, error, createOrder]);

  // Redirect if cart empty or no shipping info
  useEffect(() => {
    if (items.length === 0) {
      navigate(ROUTES.HOME);
    }
    if (!shippingInfo) {
      navigate(ROUTES.CHECKOUT_DETAILS);
    }
  }, [items.length, shippingInfo, navigate]);

  // Loading state
  if (isLoading && !bpayDetails) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          <StepIndicator currentStep={3} steps={CHECKOUT_STEPS} />
          <div className="mt-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4" />
            <p className="text-gray-600">Creating your order...</p>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error && !bpayDetails) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          <StepIndicator currentStep={3} steps={CHECKOUT_STEPS} />
          <div className="mt-8">
            <Alert variant="destructive" className="mb-4">
              <LuAlertCircle className="w-4 h-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
            <div className="flex gap-4 justify-center">
              <Button variant="outline" onClick={() => navigate(ROUTES.HOME)}>
                Return Home
              </Button>
              <Button onClick={createOrder}>Try Again</Button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Success: Display BPAY details
  if (!bpayDetails) {
    return null;
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-3xl">
        {/* Step Indicator */}
        <StepIndicator currentStep={3} steps={CHECKOUT_STEPS} />

        {/* BPAY Payment Information */}
        <div className="mt-8">
          <BpayPaymentInfo bpayDetails={bpayDetails.bpay_details} />
        </div>

        {/* Action Buttons */}
        <div className="mt-8 space-y-4">
          {/* Primary Action: Confirm Payment */}
          <div className="text-center">
            <Button
              size="lg"
              onClick={confirmPayment}
              disabled={isLoading}
              className="w-full sm:w-auto bg-black hover:bg-gray-800 text-white px-8"
            >
              <LuCheckCircle className="w-4 h-4 mr-2" />
              I've Completed the Transfer
            </Button>
            <p className="mt-3 text-sm text-gray-600">
              Click after you've made the payment through your bank
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
```

**Page Flow**:
1. **Mount**: Automatically call `createOrder()` to get BPAY details
2. **Loading**: Show spinner while order is being created
3. **Error**: Display error message with retry button
4. **Success**: Display BPAY payment information
5. **Confirmation**: User clicks "I've Completed the Transfer" → Navigate to confirmation page

**Error Scenarios**:
- Cart empty → Redirect to home
- No shipping info → Redirect to checkout details
- Order creation fails → Show error with retry
- Payment confirmation fails → Show error message

**Acceptance Criteria**:
- ✅ Automatically creates order on page mount
- ✅ Displays step indicator with Step 3 active
- ✅ Shows loading state during order creation
- ✅ Displays error state with retry button
- ✅ BPAY details displayed correctly
- ✅ Confirmation button navigates to confirmation page
- ✅ Responsive layout (mobile-friendly)

---

### Task 7: Update StepIndicator for Completed Steps ⬜

**Estimated Time**: 30 minutes
**File to Update**: `frontend/src/components/features/checkout/StepIndicator/StepIndicator.tsx`

**Description**: Enhance StepIndicator to show green checkmarks for completed steps.

**Updated Component**:

```typescript
// Update StepIndicator to accept completedSteps prop
export interface StepIndicatorProps {
  currentStep: 1 | 2 | 3 | 4;
  steps: Step[];
  completedSteps?: number[]; // NEW: Array of completed step numbers
}

export const StepIndicator: React.FC<StepIndicatorProps> = ({
  currentStep,
  steps,
  completedSteps = [], // Default: no completed steps
}) => {
  const getStepStyle = (stepNumber: number) => {
    if (completedSteps.includes(stepNumber)) {
      // Completed step: Green circle with checkmark
      return {
        circle: 'bg-green-500',
        text: 'text-gray-600',
        icon: <LuCheck className="w-4 h-4 text-white" />,
      };
    }
    if (stepNumber === currentStep) {
      // Active step: Blue circle with number
      return {
        circle: 'bg-primary-600',
        text: 'text-gray-900',
        icon: stepNumber,
      };
    }
    // Inactive step: Gray circle with number
    return {
      circle: 'bg-gray-200',
      text: 'text-gray-400',
      icon: stepNumber,
    };
  };

  return (
    <div className="flex items-center justify-center gap-4">
      {steps.map((step, index) => {
        const style = getStepStyle(step.number);
        const isLast = index === steps.length - 1;

        return (
          <React.Fragment key={step.number}>
            {/* Step Circle */}
            <div className="flex items-center gap-2">
              <div
                className={`
                  w-8 h-8 rounded-full flex items-center justify-center
                  text-sm font-medium text-white
                  ${style.circle}
                `}
                aria-current={
                  step.number === currentStep ? 'step' : undefined
                }
              >
                {style.icon}
              </div>
              <span className={`text-base font-normal ${style.text}`}>
                {step.label}
              </span>
            </div>

            {/* Connector Line */}
            {!isLast && (
              <div
                className={`
                  h-px w-12
                  ${
                    completedSteps.includes(step.number)
                      ? 'bg-green-500'
                      : 'bg-gray-300'
                  }
                `}
              />
            )}
          </React.Fragment>
        );
      })}
    </div>
  );
};
```

**Usage in BpayPaymentPage**:
```typescript
<StepIndicator
  currentStep={3}
  steps={CHECKOUT_STEPS}
  completedSteps={[1, 2]} // Steps 1 & 2 are completed
/>
```

**Visual Changes**:
- **Completed Steps**: Green circle (#00c950) with white checkmark icon
- **Active Step**: Blue circle (#155dfc) with white number
- **Pending Steps**: Gray circle with gray number
- **Connector Lines**: Green for completed, gray for pending

**Acceptance Criteria**:
- ✅ Displays green checkmark for completed steps
- ✅ Maintains blue styling for active step
- ✅ Gray styling for pending steps
- ✅ Connector lines match step completion status
- ✅ Backward compatible with existing usage

---

### Task 8: Checkout Constants Configuration ⬜

**Estimated Time**: 15 minutes
**File to Create**: `frontend/src/config/checkout.constants.ts`

**Description**: Centralize checkout configuration constants.

**Implementation**:

```typescript
/**
 * Checkout Configuration Constants
 */

export interface CheckoutStep {
  number: 1 | 2 | 3 | 4;
  label: string;
  description: string;
  path: string;
}

/**
 * Checkout Flow Steps
 * Used by StepIndicator component across all checkout pages
 */
export const CHECKOUT_STEPS: CheckoutStep[] = [
  {
    number: 1,
    label: 'Review',
    description: 'Review your order',
    path: '/checkout/review',
  },
  {
    number: 2,
    label: 'Details',
    description: 'Shipping information',
    path: '/checkout/details',
  },
  {
    number: 3,
    label: 'Payment',
    description: 'Payment via BPAY',
    path: '/checkout/payment',
  },
  {
    number: 4,
    label: 'Confirmation',
    description: 'Order confirmation',
    path: '/checkout/confirmation',
  },
];

/**
 * Tax Rate
 * 8% tax applied to all orders (Australian GST-equivalent)
 */
export const TAX_RATE = 0.08;

/**
 * Shipping Configuration
 */
export const SHIPPING = {
  FREE_THRESHOLD: 100, // Free shipping for orders >= $100
  STANDARD_COST: 10, // Flat rate shipping cost
  ESTIMATED_DAYS: '3-5', // Estimated delivery time
} as const;

/**
 * BPAY Configuration
 */
export const BPAY = {
  PAYMENT_TIMEOUT_HOURS: 24, // Payment must be completed within 24 hours
  DEFAULT_BILLER_NAME: 'ShopHub Online Store',
} as const;
```

**Acceptance Criteria**:
- ✅ All checkout constants centralized
- ✅ TypeScript types defined
- ✅ Used across all checkout pages
- ✅ Easy to update configuration in one place

---

### Task 9: Routing Update ⬜

**Estimated Time**: 15 minutes
**File to Update**: `frontend/src/App.tsx` (or router configuration)

**Description**: Add route for BPAY payment page.

**Router Configuration**:

```typescript
// In App.tsx or router config
import { BpayPaymentPage } from '@/pages/checkout/BpayPaymentPage';

// Inside Routes
<Route path="/checkout" element={<ProtectedRoute requireAuth />}>
  <Route index element={<Navigate to="/checkout/review" replace />} />
  <Route path="review" element={<CheckoutReview />} />
  <Route path="details" element={<CheckoutDetails />} />
  <Route path="payment" element={<BpayPaymentPage />} /> {/* NEW */}
  <Route path="confirmation" element={<div>Confirmation (Phase 9)</div>} />
</Route>
```

**Navigation Guards**:
- Requires authentication (already protected by parent route)
- Redirects to details page if no shipping info
- Redirects to home if cart is empty

**Acceptance Criteria**:
- ✅ `/checkout/payment` route accessible
- ✅ Protected by authentication
- ✅ Navigation from details page works
- ✅ Back button disabled (prevent re-creation of order)

---

### Task 10: Testing & Validation ⬜

**Estimated Time**: 1.5 hours
**Files to Create**:
- `frontend/src/hooks/useClipboard.test.ts`
- `frontend/src/hooks/useBpayPayment.test.ts`
- `frontend/src/components/features/checkout/BpayPaymentInfo/BpayPaymentInfo.test.tsx`
- `frontend/src/api/services/order.service.test.ts`

**Description**: Comprehensive testing of payment flow components and hooks.

#### Hook Tests

```typescript
// useClipboard.test.ts
import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useClipboard } from './useClipboard';

describe('useClipboard', () => {
  let mockClipboard: { writeText: ReturnType<typeof vi.fn> };

  beforeEach(() => {
    mockClipboard = {
      writeText: vi.fn(() => Promise.resolve()),
    };
    Object.assign(navigator, { clipboard: mockClipboard });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('copies text to clipboard', async () => {
    const { result } = renderHook(() => useClipboard());

    await act(async () => {
      await result.current.copy('test text');
    });

    expect(mockClipboard.writeText).toHaveBeenCalledWith('test text');
    expect(result.current.copied).toBe(true);
  });

  it('resets copied state after timeout', async () => {
    const { result } = renderHook(() => useClipboard(100)); // 100ms timeout

    await act(async () => {
      await result.current.copy('test');
    });

    expect(result.current.copied).toBe(true);

    await act(async () => {
      await new Promise((resolve) => setTimeout(resolve, 150));
    });

    expect(result.current.copied).toBe(false);
  });

  it('handles copy failure gracefully', async () => {
    mockClipboard.writeText = vi.fn(() => Promise.reject('Error'));

    const { result } = renderHook(() => useClipboard());

    await act(async () => {
      await result.current.copy('test');
    });

    expect(result.current.copied).toBe(false);
  });
});
```

```typescript
// order.service.test.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { orderService } from './order.service';
import { apiClient } from '@/lib/axios';

vi.mock('@/lib/axios');

describe('orderService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('createOrder', () => {
    it('creates order successfully', async () => {
      const mockResponse = {
        data: {
          order_id: 'ORD-123',
          status: 'pending',
          bpay_details: {
            biller_code: '93242',
            reference_number: '20251016-712390',
            amount: 96.38,
            expires_at: '2025-10-17T10:00:00Z',
          },
        },
      };

      vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

      const result = await orderService.createOrder({
        product_id: 'PROD-001',
        quantity: 1,
        user_id: 'USER-123',
        shipping_info: {
          receiver_name: 'John Smith',
          receiver_email: 'john@example.com',
          receiver_phone: '+61 412 345 678',
          receiver_address: '123 Main St',
          receiver_city: 'Sydney',
          receiver_state: 'NSW',
          receiver_postcode: '2000',
        },
      });

      expect(result).toEqual(mockResponse.data);
      expect(apiClient.post).toHaveBeenCalledWith('/orders', expect.any(Object));
    });

    it('throws error on API failure', async () => {
      vi.mocked(apiClient.post).mockRejectedValue(new Error('Network error'));

      await expect(
        orderService.createOrder({} as any)
      ).rejects.toThrow('Network error');
    });
  });
});
```

#### Component Tests

```typescript
// BpayPaymentInfo.test.tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BpayPaymentInfo } from './BpayPaymentInfo';

describe('BpayPaymentInfo', () => {
  const mockBpayDetails = {
    biller_code: '93242',
    reference_number: '20251016-712390',
    amount: 96.38,
    expires_at: '2025-10-17T10:00:00Z',
  };

  it('renders all BPAY details', () => {
    render(<BpayPaymentInfo bpayDetails={mockBpayDetails} />);

    expect(screen.getByText('93242')).toBeInTheDocument();
    expect(screen.getByText('20251016-712390')).toBeInTheDocument();
    expect(screen.getByText(/\$96\.38 AUD/)).toBeInTheDocument();
  });

  it('displays copy buttons', () => {
    render(<BpayPaymentInfo bpayDetails={mockBpayDetails} />);

    const copyButtons = screen.getAllByText('Copy');
    expect(copyButtons).toHaveLength(2); // Biller code + Reference number
  });

  it('shows copied state when copy button clicked', async () => {
    // Mock clipboard API
    Object.assign(navigator, {
      clipboard: {
        writeText: vi.fn(() => Promise.resolve()),
      },
    });

    render(<BpayPaymentInfo bpayDetails={mockBpayDetails} />);

    const copyButtons = screen.getAllByText('Copy');
    fireEvent.click(copyButtons[0]);

    // Wait for copied state
    await screen.findByText('Copied');
    expect(screen.getByText('Copied')).toBeInTheDocument();
  });

  it('displays payment instructions note', () => {
    render(<BpayPaymentInfo bpayDetails={mockBpayDetails} />);

    expect(
      screen.getByText(/Pay via your banking app\/website within 24 hours/)
    ).toBeInTheDocument();
  });
});
```

#### Manual Testing Checklist

- [ ] **Order Creation Flow**:
  - [ ] Navigate from details page to payment page
  - [ ] Order created automatically on page load
  - [ ] Loading spinner displays during creation
  - [ ] BPAY details display after successful creation

- [ ] **BPAY Information Display**:
  - [ ] Biller code displays correctly
  - [ ] Reference number displays correctly
  - [ ] Amount formatted with 2 decimals and AUD
  - [ ] Info note visible in blue box

- [ ] **Copy Functionality**:
  - [ ] Click "Copy" on biller code → Text copied to clipboard
  - [ ] Button shows "Copied!" feedback for 2 seconds
  - [ ] Click "Copy" on reference number → Text copied
  - [ ] Both copy buttons work independently

- [ ] **Payment Confirmation**:
  - [ ] Click "I've Completed the Transfer" button
  - [ ] Navigate to confirmation page
  - [ ] Cart cleared after confirmation
  - [ ] Order ID passed to confirmation page

- [ ] **Error Handling**:
  - [ ] Empty cart → Redirect to home
  - [ ] No shipping info → Redirect to details page
  - [ ] API failure → Error message displays
  - [ ] "Try Again" button retries order creation

- [ ] **Step Indicator**:
  - [ ] Steps 1 & 2 show green checkmarks
  - [ ] Step 3 highlighted in blue
  - [ ] Step 4 shows gray (pending)

- [ ] **Responsive Design**:
  - [ ] Mobile (<640px): Full-width layout
  - [ ] Tablet (768px): Card centered
  - [ ] Desktop (1024px+): Optimal card width

- [ ] **Accessibility**:
  - [ ] Keyboard navigation works
  - [ ] Copy buttons accessible via keyboard
  - [ ] ARIA labels present
  - [ ] Screen reader announces payment details

**Acceptance Criteria**:
- ✅ All unit tests pass (`npm run test`)
- ✅ Component tests cover happy path and error states
- ✅ Hook tests validate all functionality
- ✅ Manual testing checklist completed
- ✅ No console errors or warnings

---

## 🔗 API Integration Details

### POST /api/orders

**Endpoint**: `POST /api/orders`

**Request Body**:
```json
{
  "product_id": "PROD-001",
  "quantity": 2,
  "user_id": "USER-123",
  "shipping_info": {
    "receiver_name": "John Smith",
    "receiver_email": "john@example.com",
    "receiver_phone": "+61 412 345 678",
    "receiver_address": "123 Main Street, Unit 4",
    "receiver_city": "Sydney",
    "receiver_state": "NSW",
    "receiver_postcode": "2000"
  }
}
```

**Response** (201 Created):
```json
{
  "order_id": "ORD-20251016-001",
  "status": "pending",
  "bpay_details": {
    "biller_code": "93242",
    "reference_number": "20251016-712390",
    "amount": 96.38,
    "expires_at": "2025-10-17T10:00:00Z"
  },
  "total_amount": 96.38,
  "created_at": "2025-10-16T10:00:00Z"
}
```

**Error Responses**:
- `400 Bad Request`: Invalid request data
- `401 Unauthorized`: User not authenticated
- `404 Not Found`: Product not found
- `409 Conflict`: Product out of stock
- `500 Internal Server Error`: Server error

**Reference**: `docs/SYSTEM_INTERFACE_SPEC.md` lines 123-156

---

## 📦 Dependencies & Prerequisites

### Required (Already Completed)
- ✅ **Phase 7**: Checkout Details Page (shipping info in cart store)
- ✅ **Phase 6**: Checkout Review Page (StepIndicator component)
- ✅ **Authentication System**: User context with user ID
- ✅ **Cart Store**: Cart items and shipping info

### New Dependencies
All required libraries already installed:
- ✅ `react-icons` - LuCopy, LuCheck, LuCheckCircle icons
- ✅ `zustand` - Cart state management
- ✅ `axios` - API client
- ✅ `react-router-dom` - Navigation

---

## ✅ Success Criteria

### Functional Requirements
- ✅ Order created automatically on page load
- ✅ BPAY details displayed correctly (biller code, reference, amount)
- ✅ Copy-to-clipboard works with visual feedback
- ✅ Step indicator shows steps 1-2 completed, step 3 active
- ✅ Payment confirmation navigates to confirmation page
- ✅ Cart cleared after payment confirmation
- ✅ Error handling for all failure scenarios

### Non-Functional Requirements
- ✅ **Responsive Design**: Mobile/tablet/desktop layouts
- ✅ **Accessibility**: WCAG AA compliance, keyboard navigation
- ✅ **Performance**: Page loads in < 2 seconds
- ✅ **Type Safety**: No TypeScript errors
- ✅ **Testing**: 80%+ code coverage

### Code Quality
- ✅ Follows project conventions
- ✅ Uses existing UI components
- ✅ Proper error handling and loading states
- ✅ No console errors or warnings

---

## 🎨 Design System Reference

### Colors (from Figma)
- **Primary**: `#155dfc` (Blue for Step 3 active)
- **Success**: `#00c950` (Green for completed steps 1 & 2)
- **Text**: `#1a1a1a` (Body text)
- **Text Secondary**: `#4a5565` (Gray for labels)
- **Background Card**: `#f9fafb` (Gray 50)
- **Border**: `#e5e7eb` (Gray 200)
- **Info Box Background**: `#eff6ff` (Blue 50)
- **Info Box Border**: `#bedbff` (Blue 200)
- **Button Primary**: `#030213` (Black for confirmation button)

### Typography
- **Font Family**: Inter (fallback: system-ui, sans-serif)
- **Page Title**: 20px (text-2xl), font-weight 600
- **Subtitle**: 16px (text-base), font-weight 400
- **Biller Code**: 24px (text-2xl), font-weight 400
- **Reference Number**: 20px (text-xl), font-weight 400
- **Amount**: 24px (text-2xl), font-weight 400, primary color
- **Small Text**: 14px (text-sm), font-weight 400

### Spacing
- **Page Padding**: 32px (py-8)
- **Card Padding**: 24px (p-6)
- **Section Gap**: 24px (space-y-6)
- **Field Gap**: 16px (space-y-4)

### Components
- **Card**: `bg-gray-50`, `border-gray-200`, `rounded-lg`
- **Copy Button**: `variant="outline"`, white background, bordered
- **Info Box**: `bg-blue-50`, `border-blue-200`, `rounded-lg`
- **Confirmation Button**: Black background, white text, full-width on mobile

---

## 🚀 Next Steps (Phase 9)

After completing Phase 8, proceed to:

**Phase 9: Order Confirmation Page**
- Display order confirmation details
- Show order ID and status
- Display BPAY payment instructions reminder
- Email notification confirmation
- Order tracking information
- "View Orders" and "Continue Shopping" actions

**Required for Phase 9**:
- Order ID from Phase 8 (passed via route params)
- Order details API endpoint (`GET /api/orders/:id`)
- Email service integration (backend webhook)
- Order status tracking

---

## 📌 Notes & Considerations

### Design Decisions
- **Auto Order Creation**: Order created on page mount to avoid duplicate orders
- **Manual Confirmation**: User clicks button to confirm payment (actual payment verified by backend webhook)
- **Copy Functionality**: Essential for mobile users to paste into banking apps
- **24-Hour Timeout**: BPAY payment must be completed within 24 hours (enforced by backend)
- **Single Item Cart**: Currently only supports one item per order (Phase 7 constraint)

### Known Limitations
- Cart limited to single item (to be enhanced in future)
- No real-time payment status updates (requires webhook integration)
- Cannot edit order after creation (must cancel and re-create)
- No order cancellation from payment page (future feature)

### Security Considerations
- User must be authenticated to create order
- Order ID passed via route params (not sensitive data)
- BPAY details not stored in localStorage (only in order record)
- Backend validates all order data before creation

### Future Enhancements
- [ ] Real-time payment status updates via webhooks
- [ ] QR code for mobile banking app scanning
- [ ] Order editing before payment
- [ ] Payment timeout countdown timer
- [ ] Multiple payment method support
- [ ] Save payment methods for future orders

---

## 📖 Related Documentation

- **System Architecture**: `docs/SYSTEM_ARCHITECTURE.md`
- **API Specification**: `docs/SYSTEM_INTERFACE_SPEC.md` (Order endpoints)
- **Frontend Architecture**: `docs/frontend/ARCHITECTURE.md`
- **Development Standards**: `docs/frontend/DEVELOPMENT_STANDARDS.md`
- **Phase 6 Plan**: `tasks/frontend/phase-6-checkout-review-page.md`
- **Phase 7 Plan**: `tasks/frontend/phase-7-checkout-details-page.md`
- **Figma Design**: [BPAY Payment Page](https://www.figma.com/design/BYlBng6s0iLmcrXxAaWvDt/Store-application?node-id=16-304)

---

**Last Updated**: 2025-10-24
**Document Version**: 1.0
**Author**: Generated via Claude Code /sc:document command
