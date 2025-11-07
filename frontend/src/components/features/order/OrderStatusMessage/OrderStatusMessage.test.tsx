/**
 * OrderStatusMessage Component Tests
 */

import { render, screen } from "@testing-library/react";
import { OrderStatusMessage } from "./OrderStatusMessage";
import type { OrderStatus } from "@/types";

describe("OrderStatusMessage", () => {
  it("renders cancelled status message", () => {
    render(<OrderStatusMessage status="cancelled" />);

    expect(screen.getByText("Order Status")).toBeInTheDocument();
    expect(screen.getByText("Cancelled")).toBeInTheDocument();
    expect(
      screen.getByText(
        "This order has been cancelled. Refund is being processed."
      )
    ).toBeInTheDocument();
  });

  it("renders refunded status message", () => {
    render(<OrderStatusMessage status="refunded" />);

    expect(screen.getByText("Order Status")).toBeInTheDocument();
    expect(screen.getByText("Refunded")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Refund has been successfully processed to your original payment method."
      )
    ).toBeInTheDocument();
  });

  it("applies correct styling for cancelled status", () => {
    const { container } = render(<OrderStatusMessage status="cancelled" />);

    const badge = screen.getByText("Cancelled");
    expect(badge).toHaveClass("bg-red-600", "text-white");

    const message = container.querySelector('[role="alert"]');
    expect(message).toHaveClass("bg-red-50", "text-red-800", "border-red-200");
  });

  it("applies correct styling for refunded status", () => {
    const { container } = render(<OrderStatusMessage status="refunded" />);

    const badge = screen.getByText("Refunded");
    expect(badge).toHaveClass("bg-green-600", "text-white");

    const message = container.querySelector('[role="alert"]');
    expect(message).toHaveClass(
      "bg-green-50",
      "text-green-800",
      "border-green-200"
    );
  });

  it("returns null for non-cancelled/refunded statuses", () => {
    const statuses: OrderStatus[] = [
      "pending",
      "processing",
      "picked_up",
      "delivering",
      "delivered",
    ];

    statuses.forEach((status) => {
      const { container } = render(<OrderStatusMessage status={status} />);
      expect(container.firstChild).toBeNull();
    });
  });

  it("applies custom className", () => {
    const { container } = render(
      <OrderStatusMessage status="cancelled" className="custom-class" />
    );

    const wrapper = container.querySelector(".custom-class");
    expect(wrapper).toBeInTheDocument();
  });

  it("has proper accessibility attributes", () => {
    render(<OrderStatusMessage status="cancelled" />);

    const alert = screen.getByRole("alert");
    expect(alert).toBeInTheDocument();
  });

  it("displays message as medium weight text", () => {
    render(<OrderStatusMessage status="cancelled" />);

    const message = screen.getByText(
      "This order has been cancelled. Refund is being processed."
    );
    expect(message).toHaveClass("text-sm", "font-medium");
  });

  it("displays header with proper spacing", () => {
    const { container } = render(<OrderStatusMessage status="cancelled" />);

    const header = container.querySelector(
      ".flex.items-center.justify-between"
    );
    expect(header).toHaveClass("mb-4");
  });

  it("displays badge with proper styling", () => {
    render(<OrderStatusMessage status="cancelled" />);

    const badge = screen.getByText("Cancelled");
    expect(badge).toHaveClass(
      "inline-flex",
      "items-center",
      "px-3",
      "py-1",
      "rounded-md",
      "text-sm",
      "font-medium"
    );
  });
});
