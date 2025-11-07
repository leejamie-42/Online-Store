/**
 * useOrderSearch Hook
 * Handles search and filter logic for order history
 */

import { useMemo } from "react";
import type { OrderHistoryResponse, OrderStatus } from "@/types";
import { useDebounce } from "./useDebounce";

interface UseOrderSearchOptions {
  orders: OrderHistoryResponse[];
  searchTerm: string;
  statusFilter: OrderStatus | "all";
}

export const useOrderSearch = ({
  orders,
  searchTerm,
  statusFilter,
}: UseOrderSearchOptions) => {
  // Debounce search term to avoid excessive filtering
  const debouncedSearchTerm = useDebounce(searchTerm, 300);

  const filteredOrders = useMemo(() => {
    let results = [...orders];

    // Apply status filter
    if (statusFilter !== "all") {
      results = results.filter((order) => order.status === statusFilter);
    }

    // Apply search filter
    if (debouncedSearchTerm.trim()) {
      const lowerSearch = debouncedSearchTerm.toLowerCase().trim();
      results = results.filter((order) => {
        // Search in order ID
        const orderIdMatch = order.orderId
          .toString()
          .toLowerCase()
          .includes(lowerSearch);

        // Search in product names
        const productNameMatch = order.products.some((product) =>
          product.name.toLowerCase().includes(lowerSearch)
        );

        return orderIdMatch || productNameMatch;
      });
    }

    return results;
  }, [orders, debouncedSearchTerm, statusFilter]);

  return {
    filteredOrders,
    isSearching: searchTerm !== debouncedSearchTerm,
  };
};
