/**
 * OrderHistory Page Tests
 */

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import OrderHistory from "./OrderHistory";
import { orderService } from "@/api/services/order.service";
import type { OrderHistoryResponse } from "@/types";

// Mock order service
vi.mock("@/api/services/order.service");

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
];

const renderWithProviders = (component: React.ReactElement) => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

  return render(
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>{component}</BrowserRouter>
    </QueryClientProvider>
  );
};

describe("OrderHistory Page", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders page header", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    // Wait for the component to load and render
    await waitFor(() => {
      expect(screen.getByText("Order History")).toBeInTheDocument();
    });
    
    expect(
      screen.getByText("View and track all your orders")
    ).toBeInTheDocument();
  });

  it("displays loading state initially", () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockImplementation(
      () => new Promise(() => {}) // Never resolves
    );

    const { container } = renderWithProviders(<OrderHistory />);

    // Check for spinner element by its class
    const spinner = container.querySelector('.animate-spin');
    expect(spinner).toBeInTheDocument();
  });

  it("displays orders after loading", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Order #1/i)).toBeInTheDocument();
    });

    expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
    expect(screen.getByText(/Mouse/i)).toBeInTheDocument();
  });

  it("displays error state on API failure", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error("API Error")
    );

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Error Loading Orders/i)).toBeInTheDocument();
    });

    expect(
      screen.getByText(/Failed to load your order history/i)
    ).toBeInTheDocument();
  });

  it("displays empty state when no orders", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue([]);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText("No orders yet")).toBeInTheDocument();
    });

    expect(
      screen.getByRole("button", { name: /start shopping/i })
    ).toBeInTheDocument();
  });

  it("renders search input", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Order #1/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(
      /Search by order ID or product name/i
    );
    expect(searchInput).toBeInTheDocument();
  });

  it("renders status filter dropdown", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Order #1/i)).toBeInTheDocument();
    });

    const statusFilter = screen.getByRole("combobox");
    expect(statusFilter).toBeInTheDocument();
  });

  it("filters orders by search term", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(
      /Search by order ID or product name/i
    );

    fireEvent.change(searchInput, { target: { value: "laptop" } });

    // Wait for debounce and filtering
    await waitFor(
      () => {
        expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
        expect(screen.queryByText(/Mouse/i)).not.toBeInTheDocument();
      },
      { timeout: 500 }
    );
  });

  it("filters orders by status", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
    });

    const statusFilter = screen.getByRole("combobox");

    fireEvent.change(statusFilter, { target: { value: "delivered" } });

    await waitFor(() => {
      expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
      expect(screen.queryByText(/Mouse/i)).not.toBeInTheDocument();
    });
  });

  it("shows empty filtered state when no matches", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(
      /Search by order ID or product name/i
    );

    fireEvent.change(searchInput, { target: { value: "nonexistent" } });

    await waitFor(
      () => {
        expect(screen.getByText("No orders found")).toBeInTheDocument();
      },
      { timeout: 500 }
    );
  });

  it("displays View Details buttons for each order", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      const buttons = screen.getAllByRole("button", { name: /view details/i });
      expect(buttons).toHaveLength(2);
    });
  });

  it("does not show filters when no orders exist", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue([]);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText("No orders yet")).toBeInTheDocument();
    });

    expect(
      screen.queryByPlaceholderText(/Search by order ID or product name/i)
    ).not.toBeInTheDocument();
  });

  it("combines search and status filters", async () => {
    (orderService.getUserOrders as ReturnType<typeof vi.fn>).mockResolvedValue(mockOrders);

    renderWithProviders(<OrderHistory />);

    await waitFor(() => {
      expect(screen.getByText(/Laptop/i)).toBeInTheDocument();
    });

    const searchInput = screen.getByPlaceholderText(
      /Search by order ID or product name/i
    );
    const statusFilter = screen.getByRole("combobox");

    // Apply status filter
    fireEvent.change(statusFilter, { target: { value: "pending" } });

    await waitFor(() => {
      expect(screen.queryByText(/Laptop/i)).not.toBeInTheDocument();
      expect(screen.getByText(/Mouse/i)).toBeInTheDocument();
    });

    // Apply search
    fireEvent.change(searchInput, { target: { value: "mouse" } });

    await waitFor(
      () => {
        expect(screen.getByText(/Mouse/i)).toBeInTheDocument();
      },
      { timeout: 500 }
    );
  });
});
