/**
 * OrderCard Component
 * Displays order summary in the Order History list
 */

import React from "react";
import { useNavigate } from "react-router-dom";
import type { OrderHistoryResponse } from "@/types";
import { Button } from "@/components/ui/Button";
import { formatCurrency, formatDate } from "@/utils/formatters";
import { getOrderStatusBadge } from "@/utils/order.utils";

interface OrderCardProps {
  order: OrderHistoryResponse;
  className?: string;
}

export const OrderCard: React.FC<OrderCardProps> = ({
  order,
  className = "",
}) => {
  const navigate = useNavigate();

  const handleViewDetails = () => {
    navigate(`/orders/${order.orderId}`);
  };

  // Get first product for preview
  const firstProduct = order.products[0];
  const productCount = order.products.length;

  return (
    <div
      className={`bg-white rounded-lg border border-gray-200 p-5 hover:shadow-md transition-shadow ${className}`}
    >
      <div className="flex items-start justify-between gap-4">
        {/* Left side - Product preview and info */}
        <div className="flex items-start gap-4 flex-1 min-w-0">
          {/* Product Image */}
          {firstProduct && (
            <div className="flex-shrink-0">
              <img
                src={firstProduct.imageUrl}
                alt={firstProduct.name}
                className="w-20 h-20 object-cover rounded-lg"
                onError={(e) => {
                  // Fallback to placeholder if image fails to load
                  e.currentTarget.src =
                    "https://via.placeholder.com/80?text=Product";
                }}
              />
            </div>
          )}

          {/* Order Info */}
          <div className="flex-1 min-w-0">
            {/* Order ID */}
            <h3 className="text-base font-medium text-gray-900 mb-1">
              Order #{order.orderId}
            </h3>

            {/* Order Date */}
            <p className="text-sm text-gray-600 mb-2">
              Placed on {formatDate(order.createdAt)}
            </p>

            {/* Product Name and Quantity */}
            {firstProduct && (
              <p className="text-sm text-gray-900 mb-1 truncate">
                {firstProduct.name}
              </p>
            )}
            <p className="text-sm text-gray-600">
              Quantity: {firstProduct?.quantity || 0}
              {productCount > 1 && ` (+${productCount - 1} more)`}
            </p>

            {/* Total Price */}
            <p className="text-base font-semibold text-gray-900 mt-2">
              {formatCurrency(order.totalAmount)}
            </p>
          </div>
        </div>

        {/* Right side - Status and Actions */}
        <div className="flex flex-col items-end gap-3 flex-shrink-0">
          {/* Status Badge */}
          {getOrderStatusBadge(order.status)}

          {/* View Details Button */}
          <Button
            variant="outline"
            size="sm"
            onClick={handleViewDetails}
            className="whitespace-nowrap"
          >
            View Details
          </Button>
        </div>
      </div>
    </div>
  );
};
