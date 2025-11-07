/**
 * OrderSummary Component
 * Displays all order items in the cart with loading and empty states
 * Based on Figma design: Order summary section in checkout
 */

import React from "react";
import { Link } from "react-router-dom";
import { Card } from "@/components/ui/Card/Card";
import { Button } from "@/components/ui/Button/Button";
import { Spinner } from "@/components/ui/Spinner/Spinner";
import { OrderItemCard } from "../OrderItemCard/OrderItemCard";
import { ROUTES } from "@/config/routes";
import type { OrderItem } from "@/types/order.types";

export interface OrderSummaryProps {
  items: OrderItem[];
  isLoading?: boolean;
  className?: string;
}

/**
 * OrderSummary Component
 * Aggregates and displays all cart items with appropriate states
 *
 * @param items - Array of order items to display
 * @param isLoading - Whether cart data is still loading
 * @param className - Optional additional CSS classes
 */
export const OrderSummary: React.FC<OrderSummaryProps> = ({
  items,
  isLoading = false,
  className = "",
}) => {
  // Loading state
  if (isLoading) {
    return (
      <div className={className}>
        <h2 className="text-2xl font-semibold text-gray-900 mb-4">
          Order Summary
        </h2>
        <Card
          padding="lg"
          className="flex items-center justify-center min-h-[200px]"
        >
          <div className="text-center">
            <Spinner size="lg" />
            <p className="text-gray-600 mt-4">Loading your cart...</p>
          </div>
        </Card>
      </div>
    );
  }

  // Empty state
  if (items.length === 0) {
    return (
      <div className={className}>
        <h2 className="text-2xl font-semibold text-gray-900 mb-4">
          Order Summary
        </h2>
        <Card padding="lg" className="text-center">
          <div className="py-8">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
              />
            </svg>
            <h3 className="mt-2 text-lg font-medium text-gray-900">
              Your cart is empty
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              Add some products to get started with your order.
            </p>
            <div className="mt-6">
              <Link to={ROUTES.HOME}>
                <Button variant="primary">Continue Shopping</Button>
              </Link>
            </div>
          </div>
        </Card>
      </div>
    );
  }

  // Items display
  return (
    <div className={className}>
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-semibold text-gray-900">Order Summary</h2>
        <span className="text-sm text-gray-600">
          {items.length} {items.length === 1 ? "item" : "items"}
        </span>
      </div>

      <div className="space-y-3">
        {items.map((item, index) => (
          <OrderItemCard key={`${item.product.id}-${index}`} item={item} />
        ))}
      </div>
    </div>
  );
};
