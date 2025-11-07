/**
 * OrderItemCard Component
 * Displays a single order item with product details, quantity, and price
 * Based on Figma design: Order item display in checkout review
 */

import React from "react";
import { Card } from "@/components/ui/Card/Card";
import { CategoryBadge } from "@/components/features/product/CategoryBadge/CategoryBadge";
import type { OrderItem } from "@/types/order.types";

export interface OrderItemCardProps {
  item: OrderItem;
  showQuantityControls?: boolean;
  className?: string;
}

/**
 * OrderItemCard Component
 * Displays order item with product image, details, quantity, and price
 *
 * @param item - Order item with product, quantity, and price
 * @param showQuantityControls - Whether to show quantity adjustment controls (future feature)
 * @param className - Optional additional CSS classes
 */
export const OrderItemCard: React.FC<OrderItemCardProps> = ({
  item,
  showQuantityControls = false,
  className = "",
}) => {
  const { product, quantity, price } = item;

  // Get product description (available on ProductDetail type)
  const description = "description" in product ? product.description : "";

  return (
    <Card padding="md" className={className}>
      <div className="flex gap-4">
        {/* Product Image */}
        <div className="shrink-0">
          <img
            src={product.imageUrl}
            alt={product.name}
            className="w-20 h-20 sm:w-24 sm:h-24 object-cover rounded-lg"
            onError={(e) => {
              // Fallback for missing images
              e.currentTarget.src =
                "https://via.placeholder.com/96?text=No+Image";
            }}
          />
        </div>

        {/* Product Details */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-4">
            {/* Left: Product info */}
            <div className="flex-1 min-w-0">
              {/* Category Badge */}
              <CategoryBadge category="Accessories" />

              {/* Product Name */}
              <h3 className="font-semibold text-gray-900 mt-1 truncate">
                {product.name}
              </h3>

              {/* Product Description */}
              {description && (
                <p className="text-sm text-gray-600 mt-1 line-clamp-2">
                  {description}
                </p>
              )}

              {/* Quantity */}
              <div className="mt-2 flex items-center gap-2">
                <span className="text-sm text-gray-600">Quantity:</span>
                <span className="font-medium text-gray-900">{quantity}</span>

                {showQuantityControls && (
                  <div className="ml-2 text-xs text-gray-500">
                    (Quantity controls coming soon)
                  </div>
                )}
              </div>
            </div>

            {/* Right: Price */}
            <div className="shrink-0 text-right">
              <div className="font-semibold text-gray-900">
                ${price.toFixed(2)}
              </div>
              {quantity > 1 && (
                <div className="text-xs text-gray-500 mt-1">
                  ${(price / quantity).toFixed(2)} each
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
};
