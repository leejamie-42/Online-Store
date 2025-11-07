/**
 * OrderStatusMessage Component
 * Displays status messages for cancelled and refunded orders
 * Replaces the stepper for orders that are cancelled or refunded
 */

import React from "react";
import type { OrderStatus } from "@/types";

export interface OrderStatusMessageProps {
  status: OrderStatus;
  className?: string;
}

const STATUS_CONFIG: Record<
  "cancelled" | "refunded",
  {
    badge: string;
    badgeClassName: string;
    message: string;
    messageClassName: string;
  }
> = {
  cancelled: {
    badge: "Cancelled",
    badgeClassName: "bg-red-600 text-white",
    message: "This order has been cancelled. Refund is being processed.",
    messageClassName: "bg-red-50 text-red-800 border-red-200",
  },
  refunded: {
    badge: "Refunded",
    badgeClassName: "bg-green-600 text-white",
    message:
      "Refund has been successfully processed to your original payment method.",
    messageClassName: "bg-green-50 text-green-800 border-green-200",
  },
};

export const OrderStatusMessage: React.FC<OrderStatusMessageProps> = ({
  status,
  className = "",
}) => {
  // Only show for cancelled or refunded orders
  if (status !== "cancelled" && status !== "refunded") {
    return null;
  }

  const config = STATUS_CONFIG[status];

  return (
    <div className={`bg-white ${className}`}>
      {/* Header with title and status badge */}
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-xl font-semibold text-gray-900">Order Status</h2>
        <span
          className={`inline-flex items-center px-3 py-1 rounded-md text-sm font-medium ${config.badgeClassName}`}
        >
          {config.badge}
        </span>
      </div>

      {/* Status Message Banner */}
      <div
        className={`p-4 rounded-lg border ${config.messageClassName}`}
        role="alert"
      >
        <p className="text-sm font-medium">{config.message}</p>
      </div>
    </div>
  );
};
