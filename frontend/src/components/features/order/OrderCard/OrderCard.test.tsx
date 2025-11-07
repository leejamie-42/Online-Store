/**
 * OrderCard Component Tests
 */

import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { OrderCard } from "./OrderCard";
import type { OrderHistoryResponse } from "@/types";

const mockOrder: OrderHistoryResponse = {
  orderId: 1760604922528,
  status: "delivered",
  products: [
    {
      id: 1,
      name: "Wireless Mouse",
      price: 63.98,
      quantity: 1,
      imageUrl: "/images/mouse.jpg",
    },
  ],
  totalAmount: 63.98,
  createdAt: "2025-10-16T07:55:00Z",
};

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("OrderCard", () => {
  it("renders order information", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    expect(screen.getByText(/Order #1760604922528/i)).toBeInTheDocument();
    expect(screen.getByText(/Wireless Mouse/i)).toBeInTheDocument();
    expect(screen.getByText(/Quantity: 1/i)).toBeInTheDocument();
  });

  it("displays product image", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    const image = screen.getByAltText("Wireless Mouse") as HTMLImageElement;
    expect(image).toBeInTheDocument();
    expect(image.src).toContain("mouse.jpg");
  });

  it("shows status badge", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    expect(screen.getByText("Delivered")).toBeInTheDocument();
  });

  it("displays formatted total amount", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    // Check for currency formatted amount (will be formatted by formatCurrency)
    const priceElement = screen.getByText(/63\.98/i);
    expect(priceElement).toBeInTheDocument();
  });

  it("renders View Details button", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    const button = screen.getByRole("button", { name: /view details/i });
    expect(button).toBeInTheDocument();
  });

  it("handles multiple products", () => {
    const multiProductOrder: OrderHistoryResponse = {
      ...mockOrder,
      products: [
        mockOrder.products[0],
        {
          id: 2,
          name: "Keyboard",
          price: 99.99,
          quantity: 1,
          imageUrl: "/images/keyboard.jpg",
        },
      ],
    };

    renderWithRouter(<OrderCard order={multiProductOrder} />);

    expect(screen.getByText(/\+1 more/i)).toBeInTheDocument();
  });

  it("displays first product image when multiple products", () => {
    const multiProductOrder: OrderHistoryResponse = {
      ...mockOrder,
      products: [
        mockOrder.products[0],
        {
          id: 2,
          name: "Keyboard",
          price: 99.99,
          quantity: 1,
          imageUrl: "/images/keyboard.jpg",
        },
      ],
    };

    renderWithRouter(<OrderCard order={multiProductOrder} />);

    const image = screen.getByAltText("Wireless Mouse") as HTMLImageElement;
    expect(image.src).toContain("mouse.jpg");
  });

  it("applies custom className", () => {
    const { container } = renderWithRouter(
      <OrderCard order={mockOrder} className="custom-class" />
    );

    const card = container.querySelector(".custom-class");
    expect(card).toBeInTheDocument();
  });

  it("handles image loading error with fallback", () => {
    renderWithRouter(<OrderCard order={mockOrder} />);

    const image = screen.getByAltText("Wireless Mouse") as HTMLImageElement;

    // Trigger error event
    fireEvent.error(image);

    // Check if fallback placeholder is set
    expect(image.src).toContain("placeholder");
  });
});
