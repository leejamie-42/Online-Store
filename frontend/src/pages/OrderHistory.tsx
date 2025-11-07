/**
 * OrderHistory Page
 * Displays user's order history with search and filter capabilities
 */

import React, { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { orderService } from "@/api/services/order.service";
import { OrderCard } from "@/components/features/order/OrderCard";
import { OrderListEmpty } from "@/components/features/order/OrderListEmpty";
import { Input } from "@/components/ui/Input";
import { Spinner } from "@/components/ui/Spinner";
import { useOrderSearch } from "@/hooks/useOrderSearch";
import type { OrderStatus } from "@/types";

const STATUS_FILTER_OPTIONS = [
  { value: "all", label: "All Statuses" },
  { value: "pending", label: "Pending" },
  { value: "processing", label: "Processing" },
  { value: "picked_up", label: "Picked Up" },
  { value: "delivering", label: "Delivering" },
  { value: "delivered", label: "Delivered" },
  { value: "cancelled", label: "Cancelled" },
  { value: "refunded", label: "Refunded" },
];

const OrderHistory: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState<OrderStatus | "all">("all");

  // Fetch orders from API
  const {
    data: orders = [],
    isLoading,
    error,
  } = useQuery({
    queryKey: ["orders"],
    queryFn: orderService.getUserOrders,
  });

  // Apply search and filter
  const { filteredOrders, isSearching } = useOrderSearch({
    orders,
    searchTerm,
    statusFilter,
  });

  // Handle search input change
  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
  };

  // Handle search clear
  const handleSearchClear = () => {
    setSearchTerm("");
  };

  // Handle status filter change
  const handleStatusFilterChange = (
    e: React.ChangeEvent<HTMLSelectElement>
  ) => {
    setStatusFilter(e.target.value as OrderStatus | "all");
  };

  // Loading state
  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <Spinner size="lg" />
        </div>
      </div>
    );
  }

  // Error state
  if (error) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            Error Loading Orders
          </h3>
          <p className="text-red-600">
            Failed to load your order history. Please try again later.
          </p>
        </div>
      </div>
    );
  }

  // Check if filtered results are empty
  const hasOrders = orders.length > 0;
  const hasFilteredResults = filteredOrders.length > 0;
  const isFiltered = searchTerm.trim() !== "" || statusFilter !== "all";

  return (
    <div className="container mx-auto px-4 py-8 max-w-7xl">
      {/* Header */}
      <div className="mb-6">
        <h1 className="text-3xl font-semibold text-gray-900 mb-2">
          Order History
        </h1>
        <p className="text-base text-gray-600">
          View and track all your orders
        </p>
      </div>

      {/* Filters Section */}
      {hasOrders && (
        <div className="mb-6 flex flex-col sm:flex-row gap-4">
          {/* Search Input */}
          <div className="flex-1 relative">
            <Input
              type="text"
              placeholder="Search by order ID or product name..."
              value={searchTerm}
              onChange={handleSearchChange}
              className="w-full pr-10"
            />
            {searchTerm && (
              <button
                onClick={handleSearchClear}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 text-2xl leading-none"
                aria-label="Clear search"
              >
                ×
              </button>
            )}
          </div>

          {/* Status Filter */}
          <div className="w-full sm:w-48">
            <select
              value={statusFilter}
              onChange={handleStatusFilterChange}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              {STATUS_FILTER_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      )}

      {/* Order List */}
      <div className="space-y-4">
        {!hasOrders ? (
          // No orders at all
          <OrderListEmpty />
        ) : !hasFilteredResults ? (
          // No results from filter/search
          <OrderListEmpty isFiltered={true} />
        ) : (
          // Display filtered orders
          <>
            {isSearching && (
              <div className="text-center py-4">
                <Spinner size="sm" />
              </div>
            )}
            {filteredOrders.map((order) => (
              <OrderCard key={order.orderId} order={order} />
            ))}
          </>
        )}
      </div>
    </div>
  );
};

export default OrderHistory;
