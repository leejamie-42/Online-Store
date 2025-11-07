/**
 * Cart Type Definitions
 * Shopping cart state management types for Zustand store
 */

import type { Product } from "./product.types";
import type { ShippingInfo } from "./order.types";

/**
 * Individual item in the shopping cart
 */
export interface CartItem {
  product: Product;
  quantity: number;
}

/**
 * Cart state interface for Zustand store
 * Manages shopping cart operations and state
 */
export interface CartState {
  items: CartItem[];
  shippingInfo: ShippingInfo | null;
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  setShippingInfo: (info: ShippingInfo) => void;
  clearShippingInfo: () => void;
  clearCart: () => void;
  getItemCount: () => number;
  getSubtotal: () => number;
}

/**
 * Cart summary with pricing breakdown
 * Used for checkout review and order total display
 */
export interface CartSummary {
  subtotal: number;
  shipping: number;
  tax: number;
  total: number;
  itemCount: number;
}
