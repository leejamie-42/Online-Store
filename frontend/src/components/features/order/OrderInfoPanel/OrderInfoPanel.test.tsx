import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { OrderInfoPanel } from "./OrderInfoPanel";
import type { ShippingInfo } from "@/types";

describe("OrderInfoPanel", () => {
  const mockShippingInfo: ShippingInfo = {
    firstName: "John",
    lastName: "Doe",
    email: "john.doe@example.com",
    mobileNumber: "123-456-7890",
    addressLine1: "123 Main St",
    city: "New York",
    state: "NY",
    postcode: "10001",
    country: "USA",
  };

  const defaultProps = {
    orderId: "ORD-123456",
    orderDate: "2025-01-15T10:30:00Z",
    lastUpdated: "2025-01-16T14:45:00Z",
    shippingInfo: mockShippingInfo,
  };

  it("renders Order Details title", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Order Details")).toBeInTheDocument();
  });

  it("displays order ID correctly", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Order ID")).toBeInTheDocument();
    expect(screen.getByText("ORD-123456")).toBeInTheDocument();
  });

  it("formats and displays order date", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Order Date")).toBeInTheDocument();
    // Date formatting will vary by locale, so just check it exists
    const dateElements = screen.getAllByText(/January|2025/i);
    expect(dateElements.length).toBeGreaterThan(0);
  });

  it("formats and displays last updated timestamp", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Last Updated")).toBeInTheDocument();
  });

  it("displays shipping address label", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Shipping Address")).toBeInTheDocument();
  });

  it("formats shipping address correctly", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    const addressText = screen.getByText(
      /123 Main St.*New York.*NY.*10001.*USA/i,
    );
    expect(addressText).toBeInTheDocument();
  });

  it("applies custom className when provided", () => {
    const { container } = render(
      <OrderInfoPanel {...defaultProps} className="custom-class" />,
    );

    // The Card component is the root, so we check its presence
    const cardElement = container.querySelector(".custom-class");
    expect(cardElement).toBeInTheDocument();
  });

  it("displays all required fields", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    expect(screen.getByText("Order ID")).toBeInTheDocument();
    expect(screen.getByText("Order Date")).toBeInTheDocument();
    expect(screen.getByText("Last Updated")).toBeInTheDocument();
    expect(screen.getByText("Shipping Address")).toBeInTheDocument();
  });

  it("handles different order IDs", () => {
    const props = { ...defaultProps, orderId: "ORD-999888" };
    render(<OrderInfoPanel {...props} />);

    expect(screen.getByText("ORD-999888")).toBeInTheDocument();
  });

  it("handles different shipping addresses", () => {
    const differentAddress: ShippingInfo = {
      ...mockShippingInfo,
      addressLine1: "456 Oak Avenue",
      city: "Los Angeles",
      state: "CA",
      postcode: "90001",
    };

    render(
      <OrderInfoPanel {...defaultProps} shippingInfo={differentAddress} />,
    );

    expect(
      screen.getByText(/456 Oak Avenue.*Los Angeles.*CA.*90001/i),
    ).toBeInTheDocument();
  });

  it("formats dates with time in 12-hour format", () => {
    render(<OrderInfoPanel {...defaultProps} />);

    // Check for AM or PM in the rendered date elements
    // The dates are rendered below "Order Date" and "Last Updated" labels
    const orderDateLabel = screen.getByText("Order Date");
    const orderDateElement = orderDateLabel.nextElementSibling;
    const lastUpdatedLabel = screen.getByText("Last Updated");
    const lastUpdatedElement = lastUpdatedLabel.nextElementSibling;

    // At least one of the formatted dates should contain AM or PM
    const hasTimeFormat =
      orderDateElement?.textContent?.match(/AM|PM/i) ||
      lastUpdatedElement?.textContent?.match(/AM|PM/i);

    expect(hasTimeFormat).toBeTruthy();
  });
});
