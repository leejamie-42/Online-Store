/**
 * Tests for OrderConfirmationCard Component
 */

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { OrderConfirmationCard } from "./OrderConfirmationCard";

describe("OrderConfirmationCard", () => {
  const mockOrderId = "ORD-1760604922528";

  it("renders success message", () => {
    render(<OrderConfirmationCard orderId={mockOrderId} />);

    expect(
      screen.getByText("Your order has been confirmed"),
    ).toBeInTheDocument();
  });

  it("displays order ID correctly", () => {
    render(<OrderConfirmationCard orderId={mockOrderId} />);

    expect(screen.getByText("Order ID")).toBeInTheDocument();
    expect(screen.getByText(mockOrderId)).toBeInTheDocument();
  });

  it("shows all status checklist items", () => {
    render(<OrderConfirmationCard orderId={mockOrderId} />);

    expect(screen.getByText("Email confirmation sent")).toBeInTheDocument();
    expect(screen.getByText("Payment processed securely")).toBeInTheDocument();
    expect(screen.getByText("Order is being prepared")).toBeInTheDocument();
  });

  it("does not show redirect message by default", () => {
    render(<OrderConfirmationCard orderId={mockOrderId} />);

    expect(
      screen.queryByText(/Redirecting to order tracking/i),
    ).not.toBeInTheDocument();
  });

  it("shows redirect message when isRedirecting is true", () => {
    render(
      <OrderConfirmationCard orderId={mockOrderId} isRedirecting={true} />,
    );

    expect(
      screen.getByText("Redirecting to order tracking..."),
    ).toBeInTheDocument();
  });

  it("shows custom redirect message when provided", () => {
    const customMessage = "Taking you to your order...";
    render(
      <OrderConfirmationCard
        orderId={mockOrderId}
        isRedirecting={true}
        redirectMessage={customMessage}
      />,
    );

    expect(screen.getByText(customMessage)).toBeInTheDocument();
  });

  it("renders success icon", () => {
    const { container } = render(
      <OrderConfirmationCard orderId={mockOrderId} />,
    );

    // Check for the icon container with green background
    const iconContainer = container.querySelector(".bg-green-100");
    expect(iconContainer).toBeInTheDocument();
  });

  it("applies correct styling classes", () => {
    const { container } = render(
      <OrderConfirmationCard orderId={mockOrderId} />,
    );

    // Check for card styling
    const card = container.querySelector(".bg-white.rounded-lg");
    expect(card).toBeInTheDocument();
  });
});
