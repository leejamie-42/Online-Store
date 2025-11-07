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
    label: "Review",
    description: "Review your order",
    path: "/checkout/review",
  },
  {
    number: 2,
    label: "Details",
    description: "Shipping information",
    path: "/checkout/details",
  },
  {
    number: 3,
    label: "Payment",
    description: "Payment via BPAY",
    path: "/checkout/payment",
  },
  {
    number: 4,
    label: "Confirmation",
    description: "Order confirmation",
    path: "/checkout/confirmation",
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
  ESTIMATED_DAYS: "3-5", // Estimated delivery time
} as const;

/**
 * BPAY Configuration
 */
export const BPAY = {
  PAYMENT_TIMEOUT_HOURS: 24, // Payment must be completed within 24 hours
  DEFAULT_BILLER_NAME: "ShopHub Online Store",
} as const;
