# Phase 7: Checkout Details Page Implementation

**Status**: ⬜ Not Started
**Estimated Time**: 5-7 hours
**Priority**: High
**Dependencies**: Phase 6 (Checkout Review Page)

## 📋 Phase Overview

Implement the second step of the multi-step checkout flow: the **Checkout Details Page**. This page collects shipping information from the user through a validated form and prepares the order for payment processing.

### Checkout Flow Architecture
```
Step 1: Review → Step 2: Details (This Phase) → Step 3: Payment → Step 4: Confirmation
```

### Key Features
- **Step Progress Indicator**: Visual guide showing Step 2 of 4 (Step 1 completed)
- **Contact Information Form**: Name, email, phone number collection
- **Shipping Address Form**: Complete delivery address with validation
- **Payment Method Selection**: BPAY payment method (pre-selected)
- **Form Validation**: React Hook Form + Zod schema validation
- **Order Summary Sidebar**: Sticky summary showing cart total
- **Navigation**: Back to review, proceed to payment

### Design Reference
- **Figma**: CustomerDetailsPage component from [Online Store Application](https://www.figma.com/make/CrfvGRppEEV6IztkurDwpi/Online-Store-Application)
- **Primary Color**: #155dfc (Blue for CTAs, active states)
- **Success Color**: Green for completed steps
- **Typography**: Inter font family
- **Layout**: Two-column (Form 2/3 + Order Summary Sidebar 1/3)

---

## 📝 Task Breakdown

### Task 1: Shipping Information Schema ⬜

**Estimated Time**: 30 minutes
**File to Create**: `frontend/src/schemas/shipping.schema.ts`

**Description**: Define Zod validation schema for shipping information form, matching the backend API `ShippingInfo` interface.

**Implementation**:

```typescript
import { z } from "zod";

/**
 * Shipping Information Validation Schema
 * Matches backend ShippingInfo interface from order.types.ts
 */
export const shippingSchema = z.object({
  receiver_name: z
    .string()
    .min(1, "Full name is required")
    .min(2, "Name must be at least 2 characters")
    .max(100, "Name must be less than 100 characters"),

  receiver_email: z
    .string()
    .min(1, "Email is required")
    .email("Invalid email format"),

  receiver_phone: z
    .string()
    .min(1, "Phone number is required")
    .regex(/^\+?[\d\s\-()]+$/, "Invalid phone number format")
    .refine(
      (val) => val.replace(/\D/g, "").length >= 10,
      "Phone number must have at least 10 digits"
    ),

  receiver_address: z
    .string()
    .min(1, "Street address is required")
    .min(5, "Address must be at least 5 characters"),

  receiver_city: z
    .string()
    .min(1, "City is required")
    .min(2, "City must be at least 2 characters"),

  receiver_state: z
    .string()
    .min(1, "State is required")
    .regex(/^[A-Z]{2,3}$/, "State must be 2-3 uppercase letters (e.g., NSW, VIC)"),

  receiver_postcode: z
    .string()
    .min(1, "Postcode is required")
    .regex(/^\d{4}$/, "Postcode must be exactly 4 digits"),
});

/**
 * TypeScript type inferred from schema
 */
export type ShippingFormData = z.infer<typeof shippingSchema>;

/**
 * Transform form data to match backend ShippingInfo interface
 */
export function transformToShippingInfo(data: ShippingFormData): ShippingFormData {
  return {
    receiver_name: data.receiver_name.trim(),
    receiver_email: data.receiver_email.trim().toLowerCase(),
    receiver_phone: data.receiver_phone.trim(),
    receiver_address: data.receiver_address.trim(),
    receiver_city: data.receiver_city.trim(),
    receiver_state: data.receiver_state.trim().toUpperCase(),
    receiver_postcode: data.receiver_postcode.trim(),
  };
}
```

**Validation Rules**:
- **Name**: Required, 2-100 characters
- **Email**: Valid email format
- **Phone**: Australian format, at least 10 digits (e.g., +61 4XX XXX XXX)
- **Address**: Required, minimum 5 characters
- **City**: Required, minimum 2 characters
- **State**: 2-3 uppercase letters (NSW, VIC, QLD, etc.)
- **Postcode**: Exactly 4 digits (Australian postcode format)

**Acceptance Criteria**:
- ✅ Schema matches `ShippingInfo` interface from `order.types.ts`
- ✅ All validation rules work correctly
- ✅ Error messages are clear and user-friendly
- ✅ TypeScript type exports work with React Hook Form

---

### Task 2: Contact Information Component ⬜

**Estimated Time**: 1 hour
**File**: `frontend/src/components/features/checkout/ContactInformation/ContactInformation.tsx`

**Description**: Form section for collecting user contact details (name, email, phone).

**Component Structure**:

```typescript
import React from 'react';
import { UseFormRegister, FieldErrors } from 'react-hook-form';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { Input } from '@/components/ui/Input/Input';
import { Label } from '@/components/ui/Label/Label';
import type { ShippingFormData } from '@/schemas/shipping.schema';

export interface ContactInformationProps {
  register: UseFormRegister<ShippingFormData>;
  errors: FieldErrors<ShippingFormData>;
}

/**
 * ContactInformation Component
 * Collects user's name, email, and phone number for order delivery
 */
export const ContactInformation: React.FC<ContactInformationProps> = ({
  register,
  errors,
}) => {
  return (
    <Card>
      <CardHeader>
        <h3 className="text-xl font-semibold text-gray-900">
          Contact Information
        </h3>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Full Name */}
        <div className="space-y-2">
          <Label htmlFor="receiver_name">
            Full Name <span className="text-red-500">*</span>
          </Label>
          <Input
            id="receiver_name"
            type="text"
            placeholder="John Smith"
            {...register('receiver_name')}
            className={errors.receiver_name ? 'border-red-500' : ''}
            aria-invalid={errors.receiver_name ? 'true' : 'false'}
            aria-describedby={errors.receiver_name ? 'receiver_name-error' : undefined}
          />
          {errors.receiver_name && (
            <p id="receiver_name-error" className="text-sm text-red-600">
              {errors.receiver_name.message}
            </p>
          )}
        </div>

        {/* Email and Phone - Grid Layout */}
        <div className="grid sm:grid-cols-2 gap-4">
          {/* Email */}
          <div className="space-y-2">
            <Label htmlFor="receiver_email">
              Email Address <span className="text-red-500">*</span>
            </Label>
            <Input
              id="receiver_email"
              type="email"
              placeholder="john@example.com"
              {...register('receiver_email')}
              className={errors.receiver_email ? 'border-red-500' : ''}
              aria-invalid={errors.receiver_email ? 'true' : 'false'}
              aria-describedby={errors.receiver_email ? 'receiver_email-error' : undefined}
            />
            {errors.receiver_email && (
              <p id="receiver_email-error" className="text-sm text-red-600">
                {errors.receiver_email.message}
              </p>
            )}
          </div>

          {/* Phone Number */}
          <div className="space-y-2">
            <Label htmlFor="receiver_phone">
              Phone Number <span className="text-red-500">*</span>
            </Label>
            <Input
              id="receiver_phone"
              type="tel"
              placeholder="+61 4XX XXX XXX"
              {...register('receiver_phone')}
              className={errors.receiver_phone ? 'border-red-500' : ''}
              aria-invalid={errors.receiver_phone ? 'true' : 'false'}
              aria-describedby={errors.receiver_phone ? 'receiver_phone-error' : undefined}
            />
            {errors.receiver_phone && (
              <p id="receiver_phone-error" className="text-sm text-red-600">
                {errors.receiver_phone.message}
              </p>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
};
```

**Layout** (from Figma):
- Card wrapper with header
- Full-width name field
- 2-column grid for email + phone (stacks on mobile)
- Red border on invalid fields
- Error messages below each field

**Acceptance Criteria**:
- ✅ Uses existing Card, Input, Label components
- ✅ Integrates with React Hook Form `register` and `errors`
- ✅ Displays validation errors with proper styling
- ✅ Responsive layout (2-column on desktop, stacked on mobile)
- ✅ ARIA attributes for accessibility

---

### Task 3: Shipping Address Component ⬜

**Estimated Time**: 1 hour
**File**: `frontend/src/components/features/checkout/ShippingAddress/ShippingAddress.tsx`

**Description**: Form section for collecting complete delivery address.

**Component Structure**:

```typescript
import React from 'react';
import { UseFormRegister, FieldErrors } from 'react-hook-form';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { Input } from '@/components/ui/Input/Input';
import { Label } from '@/components/ui/Label/Label';
import type { ShippingFormData } from '@/schemas/shipping.schema';

export interface ShippingAddressProps {
  register: UseFormRegister<ShippingFormData>;
  errors: FieldErrors<ShippingFormData>;
}

/**
 * ShippingAddress Component
 * Collects delivery address details with validation
 */
export const ShippingAddress: React.FC<ShippingAddressProps> = ({
  register,
  errors,
}) => {
  return (
    <Card>
      <CardHeader>
        <h3 className="text-xl font-semibold text-gray-900">
          Shipping Address
        </h3>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* Street Address */}
        <div className="space-y-2">
          <Label htmlFor="receiver_address">
            Street Address <span className="text-red-500">*</span>
          </Label>
          <textarea
            id="receiver_address"
            placeholder="123 Main Street, Unit 4"
            rows={2}
            {...register('receiver_address')}
            className={`
              w-full px-3 py-2 border rounded-lg
              focus:outline-none focus:ring-2 focus:ring-primary-500
              ${errors.receiver_address ? 'border-red-500' : 'border-gray-300'}
            `}
            aria-invalid={errors.receiver_address ? 'true' : 'false'}
            aria-describedby={errors.receiver_address ? 'receiver_address-error' : undefined}
          />
          {errors.receiver_address && (
            <p id="receiver_address-error" className="text-sm text-red-600">
              {errors.receiver_address.message}
            </p>
          )}
        </div>

        {/* City, State, Postcode - Grid Layout */}
        <div className="grid sm:grid-cols-3 gap-4">
          {/* City */}
          <div className="space-y-2">
            <Label htmlFor="receiver_city">
              City <span className="text-red-500">*</span>
            </Label>
            <Input
              id="receiver_city"
              type="text"
              placeholder="Sydney"
              {...register('receiver_city')}
              className={errors.receiver_city ? 'border-red-500' : ''}
              aria-invalid={errors.receiver_city ? 'true' : 'false'}
              aria-describedby={errors.receiver_city ? 'receiver_city-error' : undefined}
            />
            {errors.receiver_city && (
              <p id="receiver_city-error" className="text-sm text-red-600">
                {errors.receiver_city.message}
              </p>
            )}
          </div>

          {/* State */}
          <div className="space-y-2">
            <Label htmlFor="receiver_state">
              State <span className="text-red-500">*</span>
            </Label>
            <Input
              id="receiver_state"
              type="text"
              placeholder="NSW"
              maxLength={3}
              {...register('receiver_state')}
              className={errors.receiver_state ? 'border-red-500' : ''}
              aria-invalid={errors.receiver_state ? 'true' : 'false'}
              aria-describedby={errors.receiver_state ? 'receiver_state-error' : undefined}
            />
            {errors.receiver_state && (
              <p id="receiver_state-error" className="text-sm text-red-600">
                {errors.receiver_state.message}
              </p>
            )}
          </div>

          {/* Postcode */}
          <div className="space-y-2">
            <Label htmlFor="receiver_postcode">
              Postcode <span className="text-red-500">*</span>
            </Label>
            <Input
              id="receiver_postcode"
              type="text"
              placeholder="2000"
              maxLength={4}
              {...register('receiver_postcode')}
              className={errors.receiver_postcode ? 'border-red-500' : ''}
              aria-invalid={errors.receiver_postcode ? 'true' : 'false'}
              aria-describedby={errors.receiver_postcode ? 'receiver_postcode-error' : undefined}
            />
            {errors.receiver_postcode && (
              <p id="receiver_postcode-error" className="text-sm text-red-600">
                {errors.receiver_postcode.message}
              </p>
            )}
          </div>
        </div>

        {/* Delivery Estimate */}
        <p className="text-sm text-gray-600">
          📦 Estimated delivery: 3-5 business days
        </p>
      </CardContent>
    </Card>
  );
};
```

**Layout** (from Figma):
- Card wrapper with header
- Full-width textarea for street address (2 rows)
- 3-column grid for city/state/postcode
- Delivery estimate text at bottom
- maxLength attributes for state (3) and postcode (4)

**Acceptance Criteria**:
- ✅ Textarea for multi-line address input
- ✅ 3-column responsive grid (stacks on mobile)
- ✅ Input length restrictions (state: 3 chars, postcode: 4 chars)
- ✅ Error validation and display
- ✅ Delivery estimate message included

---

### Task 4: Payment Method Selector Component ⬜

**Estimated Time**: 45 minutes
**File**: `frontend/src/components/features/checkout/PaymentMethodSelector/PaymentMethodSelector.tsx`

**Description**: Display BPAY as the payment method with visual indicators (future: add more payment options).

**Component Structure**:

```typescript
import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import { LuCreditCard, LuLock } from 'react-icons/lu';

/**
 * PaymentMethodSelector Component
 * Displays BPAY payment method (pre-selected)
 * Future: Add RadioGroup for multiple payment methods
 */
export const PaymentMethodSelector: React.FC = () => {
  return (
    <Card>
      <CardHeader>
        <h3 className="text-xl font-semibold text-gray-900">
          Payment Method
        </h3>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* BPAY Option */}
        <div className="border-2 border-blue-200 bg-blue-50 rounded-lg p-4">
          <div className="flex items-start space-x-3">
            <div className="flex-shrink-0 mt-1">
              <div className="w-5 h-5 rounded-full border-2 border-blue-600 bg-blue-600 flex items-center justify-center">
                <div className="w-2 h-2 bg-white rounded-full" />
              </div>
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-2">
                <LuCreditCard className="w-5 h-5 text-blue-600" />
                <span className="font-medium text-gray-900">BPAY</span>
                <span className="px-2 py-1 bg-blue-600 text-white text-xs font-medium rounded">
                  Recommended
                </span>
              </div>
              <p className="text-sm text-gray-600">
                Secure bank transfer. You'll receive BPAY payment details in the next step.
              </p>
            </div>
          </div>
        </div>

        {/* Security Message */}
        <div className="flex items-center gap-2 text-sm text-gray-600">
          <LuLock className="w-4 h-4" />
          <span>Your payment information is encrypted and secure</span>
        </div>
      </CardContent>
    </Card>
  );
};
```

**Design** (from Figma):
- Blue-bordered card (border-blue-200, bg-blue-50)
- Radio button visual (pre-selected, blue filled)
- CreditCard icon + "BPAY" label + "Recommended" badge
- Description text
- Lock icon + security message

**Future Enhancement**:
- Add RadioGroup when supporting multiple payment methods
- Options: BPAY, Credit Card, PayPal, etc.

**Acceptance Criteria**:
- ✅ Displays BPAY as pre-selected payment method
- ✅ Uses LucideReact icons (CreditCard, Lock)
- ✅ Blue theme matches primary color
- ✅ "Recommended" badge displayed
- ✅ Security message included

---

### Task 5: Checkout Order Summary Sidebar ⬜

**Estimated Time**: 45 minutes
**File**: `frontend/src/components/features/checkout/CheckoutOrderSummary/CheckoutOrderSummary.tsx`

**Description**: Sticky sidebar showing cart summary (used on details and payment pages).

**Component Structure**:

```typescript
import React from 'react';
import { Card, CardHeader, CardContent } from '@/components/ui/Card/Card';
import type { CartSummary } from '@/types/cart.types';

export interface CheckoutOrderSummaryProps {
  summary: CartSummary;
  showFreeShipping?: boolean;
}

/**
 * CheckoutOrderSummary Component
 * Sticky sidebar showing order total during checkout flow
 */
export const CheckoutOrderSummary: React.FC<CheckoutOrderSummaryProps> = ({
  summary,
  showFreeShipping = true,
}) => {
  return (
    <Card className="sticky top-24">
      <CardHeader>
        <h3 className="text-xl font-semibold text-gray-900">
          Order Summary
        </h3>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-3">
          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Items ({summary.itemCount})</span>
            <span className="font-medium">${summary.subtotal.toFixed(2)}</span>
          </div>

          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Shipping</span>
            <span className="font-medium">
              {summary.shipping === 0 ? (
                <span className="text-green-600">FREE</span>
              ) : (
                `$${summary.shipping.toFixed(2)}`
              )}
            </span>
          </div>

          <div className="flex justify-between text-sm">
            <span className="text-gray-600">Tax (8%)</span>
            <span className="font-medium">${summary.tax.toFixed(2)}</span>
          </div>

          <div className="border-t border-gray-200 pt-3">
            <div className="flex justify-between">
              <span className="font-semibold text-gray-900">Total</span>
              <span className="font-bold text-primary-600">
                ${summary.total.toFixed(2)}
              </span>
            </div>
          </div>
        </div>

        {/* Free Shipping Badge */}
        {showFreeShipping && summary.shipping === 0 && (
          <div className="bg-green-50 text-green-700 p-3 rounded-md text-sm flex items-center gap-2">
            <span>✓</span>
            <span>Free shipping included</span>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
```

**Features**:
- Sticky positioning (stays visible on scroll)
- Item count display
- Pricing breakdown (subtotal, shipping, tax, total)
- Free shipping badge when applicable
- Reusable for details and payment pages

**Acceptance Criteria**:
- ✅ Sticky positioning works on desktop
- ✅ Displays cart summary from CartSummary type
- ✅ Free shipping message conditional
- ✅ Total highlighted in primary color

---

### Task 6: CheckoutDetails Page ⬜

**Estimated Time**: 2 hours
**File**: `frontend/src/pages/checkout/CheckoutDetails.tsx`

**Description**: Main checkout details page integrating all form components with validation and navigation.

**Page Structure**:

```typescript
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { useNavigate } from 'react-router-dom';
import { LuArrowLeft, LuCreditCard } from 'react-icons/lu';
import { Button } from '@/components/ui/Button/Button';
import { StepIndicator } from '@/components/features/checkout/StepIndicator/StepIndicator';
import { ContactInformation } from '@/components/features/checkout/ContactInformation/ContactInformation';
import { ShippingAddress } from '@/components/features/checkout/ShippingAddress/ShippingAddress';
import { PaymentMethodSelector } from '@/components/features/checkout/PaymentMethodSelector/PaymentMethodSelector';
import { CheckoutOrderSummary } from '@/components/features/checkout/CheckoutOrderSummary/CheckoutOrderSummary';
import { useCartStore } from '@/stores/cart.store';
import { calculateCartSummary } from '@/utils/order.utils';
import { CHECKOUT_STEPS } from '@/config/checkout.constants';
import { shippingSchema, transformToShippingInfo, type ShippingFormData } from '@/schemas/shipping.schema';
import { ROUTES } from '@/config/routes';

/**
 * CheckoutDetails Page Component
 * Step 2 of 4: Collect shipping information
 */
export const CheckoutDetails: React.FC = () => {
  const navigate = useNavigate();
  const { items, setShippingInfo } = useCartStore();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ShippingFormData>({
    resolver: zodResolver(shippingSchema),
    mode: 'onBlur', // Validate on blur for better UX
  });

  const summary = calculateCartSummary(items);

  // Handle form submission
  const onSubmit = async (data: ShippingFormData) => {
    try {
      // Transform and store shipping info
      const shippingInfo = transformToShippingInfo(data);
      setShippingInfo(shippingInfo);

      // Navigate to payment step
      navigate(ROUTES.CHECKOUT_PAYMENT);
    } catch (error) {
      console.error('Error saving shipping info:', error);
      // Show error toast (if toast system implemented)
    }
  };

  // Redirect if cart is empty
  React.useEffect(() => {
    if (items.length === 0) {
      navigate(ROUTES.PRODUCTS);
    }
  }, [items.length, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-7xl">
        {/* Back Button */}
        <Button
          variant="ghost"
          onClick={() => navigate(ROUTES.CHECKOUT_REVIEW)}
          className="mb-6"
        >
          <LuArrowLeft className="w-4 h-4 mr-2" />
          Back to Review
        </Button>

        {/* Step Indicator */}
        <StepIndicator currentStep={2} steps={CHECKOUT_STEPS} />

        {/* Main Content: Two-column layout */}
        <div className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Left Column: Form (2/3 width) */}
          <div className="lg:col-span-2">
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
              {/* Contact Information */}
              <ContactInformation register={register} errors={errors} />

              {/* Shipping Address */}
              <ShippingAddress register={register} errors={errors} />

              {/* Payment Method */}
              <PaymentMethodSelector />

              {/* Submit Button */}
              <Button
                type="submit"
                variant="primary"
                size="lg"
                className="w-full"
                disabled={isSubmitting || items.length === 0}
                isLoading={isSubmitting}
              >
                <LuCreditCard className="w-4 h-4 mr-2" />
                Proceed to Payment
              </Button>
            </form>
          </div>

          {/* Right Column: Order Summary (1/3 width) */}
          <div className="lg:col-span-1">
            <CheckoutOrderSummary summary={summary} />
          </div>
        </div>
      </div>
    </div>
  );
};
```

**Layout** (from Figma):
- Back button to review page
- Step indicator showing Step 2 active, Step 1 complete (green checkmark)
- Two-column layout: Form (left 2/3) + Sidebar (right 1/3)
- Form sections stacked vertically with spacing
- Full-width submit button at bottom

**Form Handling**:
- React Hook Form with Zod validation
- Validate on blur for better UX
- Transform data before storing
- Store in cart store (add `setShippingInfo` method)
- Navigate to payment page on success

**Error Handling**:
- Display validation errors inline
- Disable submit while loading
- Redirect if cart empty
- Show error toast on submission failure (optional)

**Acceptance Criteria**:
- ✅ Form validates all fields correctly
- ✅ Error messages display properly
- ✅ Data stored in cart store on submit
- ✅ Navigation to payment works
- ✅ Step indicator shows step 2 active
- ✅ Responsive layout (stacks on mobile)
- ✅ Back button returns to review page

---

### Task 7: Update Cart Store with Shipping Info ⬜

**Estimated Time**: 30 minutes
**File**: `frontend/src/stores/cart.store.ts`

**Description**: Add shipping information state management to cart store.

**Implementation**:

```typescript
// Add to cart.store.ts interface
export interface CartState {
  items: CartItem[];
  shippingInfo: ShippingFormData | null;  // NEW

  // Existing methods...
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  getItemCount: () => number;
  getSubtotal: () => number;

  // NEW methods
  setShippingInfo: (info: ShippingFormData) => void;
  clearShippingInfo: () => void;
}

// Add to store implementation
export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      shippingInfo: null,  // NEW

      // ... existing methods ...

      // NEW: Set shipping information
      setShippingInfo: (info: ShippingFormData) => {
        set({ shippingInfo: info });
      },

      // NEW: Clear shipping information
      clearShippingInfo: () => {
        set({ shippingInfo: null });
      },
    }),
    {
      name: 'cart-storage',
    }
  )
);
```

**Acceptance Criteria**:
- ✅ Shipping info persists in localStorage
- ✅ TypeScript types updated
- ✅ Methods work correctly (set, clear)
- ✅ No breaking changes to existing cart functionality

---

### Task 8: Testing & Validation ⬜

**Estimated Time**: 1 hour
**Files to Create**:
- `frontend/src/schemas/__tests__/shipping.schema.test.ts`
- `frontend/src/components/features/checkout/ContactInformation/ContactInformation.test.tsx`
- `frontend/src/components/features/checkout/ShippingAddress/ShippingAddress.test.tsx`

**Description**: Comprehensive testing of form validation and components.

#### Schema Validation Tests

```typescript
// shipping.schema.test.ts
import { describe, it, expect } from 'vitest';
import { shippingSchema } from '../shipping.schema';

describe('shippingSchema', () => {
  it('validates correct data', () => {
    const validData = {
      receiver_name: 'John Smith',
      receiver_email: 'john@example.com',
      receiver_phone: '+61 412 345 678',
      receiver_address: '123 Main Street, Unit 4',
      receiver_city: 'Sydney',
      receiver_state: 'NSW',
      receiver_postcode: '2000',
    };

    const result = shippingSchema.safeParse(validData);
    expect(result.success).toBe(true);
  });

  it('rejects missing required fields', () => {
    const invalidData = { receiver_name: '' };
    const result = shippingSchema.safeParse(invalidData);
    expect(result.success).toBe(false);
  });

  it('validates email format', () => {
    const invalidEmail = {
      receiver_name: 'John',
      receiver_email: 'invalid-email',
      // ... other fields
    };
    const result = shippingSchema.safeParse(invalidEmail);
    expect(result.success).toBe(false);
  });

  it('validates phone number format', () => {
    const invalidPhone = {
      receiver_name: 'John',
      receiver_email: 'john@example.com',
      receiver_phone: 'abc123',
      // ... other fields
    };
    const result = shippingSchema.safeParse(invalidPhone);
    expect(result.success).toBe(false);
  });

  it('validates postcode is 4 digits', () => {
    const invalidPostcode = {
      receiver_name: 'John',
      receiver_email: 'john@example.com',
      receiver_phone: '+61 412 345 678',
      receiver_address: '123 Main St',
      receiver_city: 'Sydney',
      receiver_state: 'NSW',
      receiver_postcode: '20000', // 5 digits
    };
    const result = shippingSchema.safeParse(invalidPostcode);
    expect(result.success).toBe(false);
  });

  it('validates state format (2-3 uppercase letters)', () => {
    const validStates = ['NSW', 'VIC', 'QLD', 'ACT'];
    validStates.forEach(state => {
      const data = {
        receiver_name: 'John',
        receiver_email: 'john@example.com',
        receiver_phone: '+61 412 345 678',
        receiver_address: '123 Main St',
        receiver_city: 'Sydney',
        receiver_state: state,
        receiver_postcode: '2000',
      };
      const result = shippingSchema.safeParse(data);
      expect(result.success).toBe(true);
    });
  });
});
```

#### Component Tests

```typescript
// ContactInformation.test.tsx
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { useForm } from 'react-hook-form';
import { ContactInformation } from './ContactInformation';

describe('ContactInformation', () => {
  it('renders all form fields', () => {
    const Wrapper = () => {
      const { register, formState: { errors } } = useForm();
      return <ContactInformation register={register} errors={errors} />;
    };

    render(<Wrapper />);

    expect(screen.getByLabelText(/full name/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/email address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/phone number/i)).toBeInTheDocument();
  });

  it('displays error messages', () => {
    const Wrapper = () => {
      const { register, formState: { errors } } = useForm();
      const mockErrors = {
        receiver_name: { message: 'Name is required' }
      };
      return <ContactInformation register={register} errors={mockErrors as any} />;
    };

    render(<Wrapper />);
    expect(screen.getByText('Name is required')).toBeInTheDocument();
  });
});
```

#### Manual Testing Checklist

- [ ] **Form Validation**:
  - [ ] All required fields show error when empty
  - [ ] Email validation works (valid/invalid formats)
  - [ ] Phone validation accepts Australian format
  - [ ] Postcode only accepts 4 digits
  - [ ] State accepts 2-3 uppercase letters

- [ ] **Form Submission**:
  - [ ] Valid data submits successfully
  - [ ] Invalid data prevents submission
  - [ ] Data stored in cart store
  - [ ] Navigation to payment works

- [ ] **UI/UX**:
  - [ ] Error messages clear and helpful
  - [ ] Red borders on invalid fields
  - [ ] Loading state on submit button
  - [ ] Back button works

- [ ] **Responsive Design**:
  - [ ] Mobile (<640px): All fields stack vertically
  - [ ] Tablet (768px): 2-column layouts work
  - [ ] Desktop (1024px+): Full layout displays

- [ ] **Accessibility**:
  - [ ] Keyboard navigation works (Tab through fields)
  - [ ] ARIA labels present
  - [ ] Error messages announced to screen readers
  - [ ] Focus management correct

**Acceptance Criteria**:
- ✅ All schema validation tests pass
- ✅ Component tests pass
- ✅ Manual testing checklist complete
- ✅ No console errors or warnings

---

## 🔗 Integration Notes

### Cart Store Integration

The shipping information needs to be stored temporarily during checkout:

**Option 1: Cart Store** (Recommended)
```typescript
// In CheckoutDetails page
const { setShippingInfo } = useCartStore();
const onSubmit = (data) => {
  setShippingInfo(transformToShippingInfo(data));
  navigate('/checkout/payment');
};

// In Payment page
const { shippingInfo } = useCartStore();
// Use shippingInfo for order creation
```

**Option 2: Navigation State**
```typescript
// Alternative: Pass via navigation state
navigate('/checkout/payment', {
  state: { shippingInfo: transformToShippingInfo(data) }
});
```

### API Integration (Future Phase 8)

Shipping info will be submitted as part of order creation:

```typescript
// POST /api/orders
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

---

## 📦 Dependencies & Prerequisites

### Required (Already Available)
- ✅ **Phase 6**: Checkout Review Page, StepIndicator, cart store
- ✅ **React Hook Form**: `useForm`, `register`, form validation
- ✅ **Zod**: Schema validation library
- ✅ **@hookform/resolvers**: Zod resolver for React Hook Form
- ✅ **Existing UI Components**: Card, Input, Button, Label
- ✅ **LucideReact Icons**: ArrowLeft, CreditCard, Lock

### To Create
- ⬜ Textarea component (or use native textarea with styling)
- ⬜ RadioGroup component (may already exist from Radix UI)
- ⬜ Label component (if not already exists)

### External Libraries
All required libraries already installed in `package.json`:
- `react-hook-form@^7.65.0`
- `@hookform/resolvers@^5.2.2`
- `zod@^4.1.12`
- `react-icons@^5.5.0`

---

## ✅ Success Criteria

### Functional Requirements
- ✅ Form validates all shipping information correctly
- ✅ Error messages display for invalid inputs
- ✅ Form submission stores data in cart store
- ✅ Navigation to payment step works
- ✅ Back navigation to review step works
- ✅ Step indicator shows Step 2 active, Step 1 complete
- ✅ Order summary sidebar displays cart total
- ✅ Empty cart redirects to products page

### Non-Functional Requirements
- ✅ **Form UX**: Validation on blur, clear error messages
- ✅ **Responsive Design**: Mobile/tablet/desktop layouts
- ✅ **Accessibility**: WCAG AA compliance, ARIA labels, keyboard navigation
- ✅ **Performance**: Form renders quickly, validation is instant
- ✅ **Type Safety**: No TypeScript errors, all components typed
- ✅ **Testing**: Schema tests and component tests passing

### Code Quality
- ✅ Follows project conventions (component structure, naming)
- ✅ Uses React Hook Form best practices
- ✅ Zod schema matches backend API interface
- ✅ Proper error handling and user feedback
- ✅ No console errors or warnings

---

## 🎨 Design System Reference

### Colors (from Figma)
- **Primary**: `#155dfc` (Blue for CTAs, active steps)
- **Success**: `#10b981` (Green for completed step checkmark)
- **Error**: `#ef4444` (Red for validation errors)
- **Text**: `#1f2937` (Gray 800 for headings)
- **Text Secondary**: `#6b7280` (Gray 500 for labels)
- **Background**: `#f9fafb` (Gray 50 for page background)
- **Border**: `#e5e7eb` (Gray 200 for cards)

### Typography
- **Font Family**: Inter (fallback: system-ui, sans-serif)
- **Heading**: 20px (text-xl), font-weight 600
- **Body**: 16px (text-base), font-weight 400
- **Small**: 14px (text-sm), font-weight 400
- **Label**: 14px (text-sm), font-weight 500

### Spacing
- **Section Gap**: 24px (gap-6)
- **Card Padding**: 16-24px (p-4 to p-6)
- **Input Height**: 40px (h-10)
- **Form Field Gap**: 8px (space-y-2)

### Breakpoints (Tailwind)
- **sm**: 640px (Mobile landscape)
- **md**: 768px (Tablet)
- **lg**: 1024px (Desktop)

---

## 📚 Code Examples & Patterns

### React Hook Form Integration Pattern

```typescript
// From Login.tsx - Proven pattern
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

const { register, handleSubmit, formState: { errors } } = useForm({
  resolver: zodResolver(shippingSchema),
  mode: 'onBlur', // Validate on blur for better UX
});

// In JSX
<Input
  id="receiver_name"
  {...register('receiver_name')}
  className={errors.receiver_name ? 'border-red-500' : ''}
  aria-invalid={errors.receiver_name ? 'true' : 'false'}
/>
{errors.receiver_name && (
  <p className="text-sm text-red-600">{errors.receiver_name.message}</p>
)}
```

### Component Import Pattern

```typescript
// External libraries
import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';

// Internal aliases (@/)
import { Button } from '@/components/ui/Button/Button';
import { Card } from '@/components/ui/Card/Card';
import type { ShippingFormData } from '@/schemas/shipping.schema';

// Relative imports
import { ContactInformation } from './ContactInformation';
```

---

## 🚀 Next Steps (Phase 8)

After completing Phase 7, proceed to:

**Phase 8: Payment Page (BPAY Instructions)**
- Display BPAY payment details (biller code, reference number)
- Order creation API call (`POST /api/orders`)
- Display order confirmation with BPAY instructions
- Payment status handling

**Required for Phase 8**:
- Shipping info from Phase 7 (stored in cart)
- Cart items from Phase 6
- Order creation API integration
- BPAY details display component

---

## 📌 Notes & Considerations

### Design Decisions
- **Form Validation**: Validate on blur (not on change) for better UX
- **Data Storage**: Use cart store for temporary shipping info storage
- **Payment Method**: BPAY pre-selected (only option currently)
- **State Format**: Uppercase 2-3 letters (NSW, VIC, QLD, ACT, etc.)
- **Postcode**: Australian format (4 digits)

### Known Limitations
- Only BPAY payment method (future: add more options)
- No address autocomplete (future: integrate Google Places API)
- No address validation (future: validate with Australia Post API)
- State input is text (future: dropdown with all Australian states)

### Future Enhancements
- [ ] Address autocomplete with Google Places API
- [ ] Address validation with Australia Post API
- [ ] State dropdown (NSW, VIC, QLD, SA, WA, TAS, NT, ACT)
- [ ] Multiple payment method support
- [ ] Save address to user profile
- [ ] "Use saved address" option for returning customers

---

## 📖 Related Documentation

- **System Architecture**: `docs/SYSTEM_ARCHITECTURE.md`
- **API Specification**: `docs/SYSTEM_INTERFACE_SPEC.md` (Order creation endpoint)
- **Frontend Architecture**: `docs/frontend/ARCHITECTURE.md`
- **Development Standards**: `docs/frontend/DEVELOPMENT_STANDARDS.md`
- **Phase 6 Plan**: `tasks/frontend/phase-6-checkout-review-page.md`
- **Figma Design**: CustomerDetailsPage component

---

**Last Updated**: 2025-10-24
**Document Version**: 1.0
**Author**: Generated via Claude Code /sc:document command
