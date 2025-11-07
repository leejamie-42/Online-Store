/**
 * CancelOrderCard Component Tests
 * Tests for React Query mutation-based cancel order component
 */

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { CancelOrderCard } from "./CancelOrderCard";
import { useCancelOrder } from "@/hooks/useOrders";

// Mock the useCancelOrder hook
vi.mock("@/hooks/useOrders", () => ({
  useCancelOrder: vi.fn(),
}));

// Mock the formatters utility
vi.mock("@/utils/formatters", () => ({
  removePrfixToGetId: vi.fn((id: string) => id.replace("ORD-", "")),
}));

const mockMutate = vi.fn();
const mockUseCancelOrder = useCancelOrder as ReturnType<typeof vi.fn>;

const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
      mutations: { retry: false },
    },
  });

  return ({ children }: { children: React.ReactNode }) => (
    <QueryClientProvider client={queryClient}>{children}</QueryClientProvider>
  );
};

describe("CancelOrderCard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
      isError: false,
      isSuccess: false,
      data: undefined,
      error: null,
      reset: vi.fn(),
    } as any);
  });

  it("renders cancel order card", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    // "Cancel Order" appears in both heading and button, so check both exist
    expect(screen.getAllByText("Cancel Order").length).toBeGreaterThan(0);
    expect(
      screen.getByText(/You can cancel this order before it's dispatched/i)
    ).toBeInTheDocument();
  });

  it("displays refund information", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    expect(
      screen.getByText(
        /A full refund will be processed within 5-7 business days/i
      )
    ).toBeInTheDocument();
  });

  it("displays Cancel Order button initially", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    expect(cancelButton).toBeInTheDocument();
    expect(cancelButton).not.toBeDisabled();
  });

  it("shows confirmation dialog when Cancel Order is clicked", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    expect(
      screen.getByText(/Are you sure you want to cancel this order/i)
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Yes, Cancel Order/i })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /No, Keep Order/i })
    ).toBeInTheDocument();
  });

  it("shows warning in confirmation dialog", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    expect(
      screen.getByText(/This action cannot be undone/i)
    ).toBeInTheDocument();
    expect(
      screen.getByText(
        /A refund will be issued to your original payment method/i
      )
    ).toBeInTheDocument();
  });

  it("hides confirmation dialog when No, Keep Order is clicked", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    const keepOrderButton = screen.getByRole("button", {
      name: /No, Keep Order/i,
    });
    fireEvent.click(keepOrderButton);

    expect(
      screen.queryByText(/Are you sure you want to cancel this order/i)
    ).not.toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: /^Cancel Order$/i })
    ).toBeInTheDocument();
  });

  it("calls mutation when confirmed", () => {
    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    const confirmButton = screen.getByRole("button", {
      name: /Yes, Cancel Order/i,
    });
    fireEvent.click(confirmButton);

    expect(mockMutate).toHaveBeenCalledWith(
      "123",
      expect.objectContaining({
        onSuccess: expect.any(Function),
        onError: expect.any(Function),
      })
    );
  });

  it("shows loading state during cancellation", () => {
    // Start with isPending false, then set to true after click
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
      isError: false,
      isSuccess: false,
      data: undefined,
      error: null,
      reset: vi.fn(),
    } as any);

    const { rerender } = render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    // Click initial Cancel Order button to show confirmation
    const initialCancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(initialCancelButton);

    // Now set isPending to true to simulate loading state
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: true,
      isError: false,
      isSuccess: false,
      data: undefined,
      error: null,
      reset: vi.fn(),
    } as any);

    // Rerender to pick up the new mock value
    rerender(<CancelOrderCard orderId="ORD-123" />);

    // Now "Cancelling..." should be visible in the confirmation dialog
    expect(screen.getByText(/Cancelling.../i)).toBeInTheDocument();
  });

  it("disables buttons during cancellation", () => {
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: true,
      isError: false,
      isSuccess: false,
      data: undefined,
      error: null,
      reset: vi.fn(),
    } as any);

    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    const buttons = screen.getAllByRole("button");
    buttons.forEach((button) => {
      expect(button).toBeDisabled();
    });
  });

  it("shows error message when cancellation fails", () => {
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
      isError: true,
      isSuccess: false,
      data: undefined,
      error: new Error("Cancellation failed"),
      reset: vi.fn(),
    } as any);

    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    expect(
      screen.getByText(/Failed to cancel order. Please try again./i)
    ).toBeInTheDocument();
  });

  it("applies custom className", () => {
    const { container } = render(
      <CancelOrderCard orderId="ORD-123" className="custom-class" />,
      { wrapper: createWrapper() }
    );

    const card = container.querySelector(".custom-class");
    expect(card).toBeInTheDocument();
  });

  it("keeps dialog open on error for retry", () => {
    mockUseCancelOrder.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
      isError: true,
      isSuccess: false,
      data: undefined,
      error: new Error("Cancellation failed"),
      reset: vi.fn(),
    } as any);

    render(<CancelOrderCard orderId="ORD-123" />, {
      wrapper: createWrapper(),
    });

    const cancelButton = screen.getByRole("button", {
      name: /^Cancel Order$/i,
    });
    fireEvent.click(cancelButton);

    // Error should be visible
    expect(
      screen.getByText(/Failed to cancel order. Please try again./i)
    ).toBeInTheDocument();

    // Confirmation dialog should still be open
    expect(
      screen.getByText(/Are you sure you want to cancel this order/i)
    ).toBeInTheDocument();

    // User can retry
    const retryButton = screen.getByRole("button", {
      name: /Yes, Cancel Order/i,
    });
    expect(retryButton).not.toBeDisabled();
  });
});
