/**
 * Cart Store (Zustand)
 * Manages shopping cart state with localStorage persistence
 */

import { create } from "zustand";
import { persist } from "zustand/middleware";
import type { CartState } from "@/types/cart.types";
import type { Product } from "@/types/product.types";

/**
 * Cart Store
 * Provides cart state management with persistence
 */
export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      items: [],
      shippingInfo: null,

      /**
       * Add item to cart or update quantity if already exists
       */
      addItem: (product: Product, quantity: number) => {
        set((state) => {
          const existingItemIndex = state.items.findIndex(
            (item) => item.product.id === product.id,
          );

          if (existingItemIndex !== -1) {
            // Update existing item quantity
            const updatedItems = [...state.items];
            updatedItems[existingItemIndex] = {
              ...updatedItems[existingItemIndex],
              quantity: updatedItems[existingItemIndex].quantity + quantity,
            };
            return { items: updatedItems };
          } else {
            // Currntly we only support 1 item in the cart
            // Replace old item and set new Item into cart
            return { items: [{ product, quantity }] };
          }
        });
      },

      /**
       * Remove item from cart
       */
      removeItem: (productId: string) => {
        set((state) => ({
          items: state.items.filter((item) => item.product.id !== productId),
        }));
      },

      /**
       * Update item quantity
       */
      updateQuantity: (productId: string, quantity: number) => {
        if (quantity <= 0) {
          get().removeItem(productId);
          return;
        }

        set((state) => ({
          items: state.items.map((item) =>
            item.product.id === productId ? { ...item, quantity } : item,
          ),
        }));
      },

      /**
       * Set shipping information for checkout
       */
      setShippingInfo: (info) => {
        set({ shippingInfo: info });
      },

      /**
       * Clear shipping information
       */
      clearShippingInfo: () => {
        set({ shippingInfo: null });
      },

      /**
       * Clear all items from cart
       */
      clearCart: () => {
        set({ items: [], shippingInfo: null });
      },

      /**
       * Get total number of items in cart
       */
      getItemCount: () => {
        return get().items.reduce((total, item) => total + item.quantity, 0);
      },

      /**
       * Get cart subtotal (sum of all item prices)
       */
      getSubtotal: () => {
        return get().items.reduce(
          (total, item) => total + item.product.price * item.quantity,
          0,
        );
      },
    }),
    {
      name: "cart-storage", // localStorage key
    },
  ),
);
