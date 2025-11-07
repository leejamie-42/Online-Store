/**
 * useOrderSearch Hook Tests
 */

import { renderHook, waitFor } from "@testing-library/react";
import { useOrderSearch } from "../useOrderSearch";
import type { OrderHistoryResponse } from "@/types";

const mockOrders: OrderHistoryResponse[] = [
  {
    orderId: 1,
    status: "delivered",
    products: [
      {
        id: 1,
        name: "Laptop",
        price: 999,
        quantity: 1,
        imageUrl: "/laptop.jpg",
      },
    ],
    totalAmount: 999,
    createdAt: "2025-10-15T10:00:00Z",
  },
  {
    orderId: 2,
    status: "pending",
    products: [
      {
        id: 2,
        name: "Mouse",
        price: 50,
        quantity: 1,
        imageUrl: "/mouse.jpg",
      },
    ],
    totalAmount: 50,
    createdAt: "2025-10-16T10:00:00Z",
  },
  {
    orderId: 3,
    status: "processing",
    products: [
      {
        id: 3,
        name: "Keyboard",
        price: 120,
        quantity: 2,
        imageUrl: "/keyboard.jpg",
      },
    ],
    totalAmount: 240,
    createdAt: "2025-10-17T10:00:00Z",
  },
];

describe("useOrderSearch", () => {
  it("returns all orders when no filters applied", () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "",
        statusFilter: "all",
      })
    );

    expect(result.current.filteredOrders).toHaveLength(3);
  });

  it("filters by status", () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "",
        statusFilter: "delivered",
      })
    );

    expect(result.current.filteredOrders).toHaveLength(1);
    expect(result.current.filteredOrders[0].status).toBe("delivered");
  });

  it("filters by search term - product name (case insensitive)", async () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "laptop",
        statusFilter: "all",
      })
    );

    // Wait for debounce
    await waitFor(
      () => {
        expect(result.current.filteredOrders).toHaveLength(1);
      },
      { timeout: 500 }
    );

    expect(result.current.filteredOrders[0].products[0].name).toBe("Laptop");
  });

  it("filters by search term - order ID", async () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "2",
        statusFilter: "all",
      })
    );

    // Wait for debounce
    await waitFor(
      () => {
        expect(result.current.filteredOrders).toHaveLength(1);
      },
      { timeout: 500 }
    );

    expect(result.current.filteredOrders[0].orderId).toBe(2);
  });

  it("combines search and status filters", async () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "mouse",
        statusFilter: "pending",
      })
    );

    // Wait for debounce
    await waitFor(
      () => {
        expect(result.current.filteredOrders).toHaveLength(1);
      },
      { timeout: 500 }
    );

    expect(result.current.filteredOrders[0].status).toBe("pending");
    expect(result.current.filteredOrders[0].products[0].name).toBe("Mouse");
  });

  it("returns empty array when no matches", async () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "nonexistent",
        statusFilter: "all",
      })
    );

    // Wait for debounce
    await waitFor(
      () => {
        expect(result.current.filteredOrders).toHaveLength(0);
      },
      { timeout: 500 }
    );
  });

  it("indicates when search is in progress", () => {
    const { result, rerender } = renderHook(
      ({ searchTerm }) =>
        useOrderSearch({
          orders: mockOrders,
          searchTerm,
          statusFilter: "all",
        }),
      {
        initialProps: { searchTerm: "" },
      }
    );

    // Initially, not searching
    expect(result.current.isSearching).toBe(false);

    // Update search term
    rerender({ searchTerm: "laptop" });

    // Immediately after updating, isSearching should be true (debounce hasn't completed)
    expect(result.current.isSearching).toBe(true);
  });

  it("handles empty search term", () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "   ",
        statusFilter: "all",
      })
    );

    expect(result.current.filteredOrders).toHaveLength(3);
  });

  it('filters by status and ignores search when status is not "all"', () => {
    const { result } = renderHook(() =>
      useOrderSearch({
        orders: mockOrders,
        searchTerm: "",
        statusFilter: "processing",
      })
    );

    expect(result.current.filteredOrders).toHaveLength(1);
    expect(result.current.filteredOrders[0].status).toBe("processing");
  });
});
