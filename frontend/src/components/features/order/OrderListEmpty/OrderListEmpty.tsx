/**
 * OrderListEmpty Component
 * Displays empty state when user has no orders or no search results
 */

import React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/Button";

interface OrderListEmptyProps {
  isFiltered?: boolean; // true if showing empty results from search/filter
  className?: string;
}

export const OrderListEmpty: React.FC<OrderListEmptyProps> = ({
  isFiltered = false,
  className = "",
}) => {
  const navigate = useNavigate();

  const handleStartShopping = () => {
    navigate("/products");
  };

  return (
    <div
      className={`flex flex-col items-center justify-center py-12 px-4 ${className}`}
    >
      {/* Icon */}
      <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
        <span className="text-4xl">📦</span>
      </div>

      {/* Message */}
      <h3 className="text-xl font-semibold text-gray-900 mb-2">
        {isFiltered ? "No orders found" : "No orders yet"}
      </h3>

      <p className="text-sm text-gray-600 text-center max-w-md mb-6">
        {isFiltered
          ? "Try adjusting your search or filter criteria"
          : "You haven't placed any orders yet. Start shopping to see your order history here."}
      </p>

      {/* Action Button */}
      {!isFiltered && (
        <Button onClick={handleStartShopping} variant="primary">
          Start Shopping
        </Button>
      )}
    </div>
  );
};
