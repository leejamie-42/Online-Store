/**
 * Order Utility Functions
 * Calculation and formatting utilities for orders and cart
 */

import React from "react";
import type { CartItem, CartSummary } from "@/types/cart.types";
import type { OrderStatus } from "@/types/order.types";

// Constants
const TAX_RATE = 0.08; // 8% tax rate
const FREE_SHIPPING_THRESHOLD = 100; // Free shipping for orders >= $100
const SHIPPING_COST = 10; // Standard shipping cost

/**
 * Calculate complete cart summary with pricing breakdown
 * @param items - Array of cart items
 * @returns Cart summary with subtotal, shipping, tax, and total
 */
export function calculateCartSummary(items: CartItem[]): CartSummary {
  // Calculate subtotal from all items
  const subtotal = items.reduce((sum, item) => {
    return sum + item.product.price * item.quantity;
  }, 0);

  // Apply free shipping for orders above threshold
  const shipping = subtotal >= FREE_SHIPPING_THRESHOLD ? 0 : SHIPPING_COST;

  // Calculate tax on subtotal only (not on shipping)
  const tax = subtotal * TAX_RATE;

  // Calculate final total
  const total = subtotal + shipping + tax;

  // Count total items
  const itemCount = items.reduce((sum, item) => sum + item.quantity, 0);

  return {
    subtotal,
    shipping,
    tax,
    total,
    itemCount,
  };
}

/**
 * Format number as currency (Australian Dollars)
 * @param amount - Numeric amount to format
 * @returns Formatted currency string (e.g., "$99.99")
 */
export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat("en-AU", {
    style: "currency",
    currency: "AUD",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount);
}

/**
 * Get shipping cost description
 * @param subtotal - Order subtotal amount
 * @returns Human-readable shipping cost description
 */
export function getShippingDescription(subtotal: number): string {
  if (subtotal >= FREE_SHIPPING_THRESHOLD) {
    return "FREE";
  }
  return formatCurrency(SHIPPING_COST);
}

/**
 * Calculate tax amount from subtotal
 * @param subtotal - Order subtotal
 * @returns Tax amount (8% of subtotal)
 */
export function calculateTax(subtotal: number): number {
  return subtotal * TAX_RATE;
}

/**
 * Check if order qualifies for free shipping
 * @param subtotal - Order subtotal amount
 * @returns True if order qualifies for free shipping
 */
export function qualifiesForFreeShipping(subtotal: number): boolean {
  return subtotal >= FREE_SHIPPING_THRESHOLD;
}

/**
 * Status badge styles mapping
 */
const STATUS_STYLES: Record<OrderStatus, string> = {
  delivered: "bg-green-100 text-green-800",
  delivering: "bg-blue-100 text-blue-800",
  processing: "bg-yellow-100 text-yellow-800",
  pending: "bg-gray-100 text-gray-800",
  picked_up: "bg-blue-100 text-blue-800",
  cancelled: "bg-red-100 text-red-800",
  refunded: "bg-green-100 text-green-800",
};

/**
 * Status badge label mapping
 */
const STATUS_LABELS: Record<OrderStatus, string> = {
  delivered: "Delivered",
  delivering: "In Transit",
  processing: "Processing",
  pending: "Pending",
  picked_up: "Picked Up",
  cancelled: "Cancelled",
  refunded: "Refunded",
};

/**
 * Get order status badge component
 * @param status - Order status
 * @returns React element with styled status badge
 */
export function getOrderStatusBadge(status: OrderStatus): React.ReactElement {
  const styles = STATUS_STYLES[status] || STATUS_STYLES.pending;
  const label = STATUS_LABELS[status] || status;

  return React.createElement(
    "span",
    {
      className: `inline-flex items-center px-3 py-1 rounded-md text-sm font-medium ${styles}`,
    },
    label
  );
}
