/**
 * OrderItemCard Component Tests
 */

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { OrderItemCard } from "./OrderItemCard";
import type { OrderItem } from "@/types/order.types";

describe("OrderItemCard", () => {
  const mockOrderItem: OrderItem = {
    product: {
      id: "PRO-1",
      name: "Test Product",
      price: 50,
      stock: 10,
      imageUrl: "https://example.com/image.jpg",
      published: true,
    },
    quantity: 2,
    price: 100,
  };

  it("renders product name", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    expect(screen.getByText("Test Product")).toBeInTheDocument();
  });

  it("renders product price", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    expect(screen.getByText("$100.00")).toBeInTheDocument();
  });

  it("renders product quantity", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    expect(screen.getByText("2")).toBeInTheDocument();
  });

  it("renders product image with correct src", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    const image = screen.getByAltText("Test Product") as HTMLImageElement;
    expect(image.src).toBe("https://example.com/image.jpg");
  });

  it("shows unit price for multiple quantity items", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    expect(screen.getByText("$50.00 each")).toBeInTheDocument();
  });

  it("renders category badge", () => {
    render(<OrderItemCard item={mockOrderItem} />);
    expect(screen.getByText("Accessories")).toBeInTheDocument();
  });
});
