import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { ProductCard } from "./ProductCard";
import type { ProductResponse } from "@/types/product.types";

describe("ProductCard", () => {
  const mockProduct: ProductResponse = {
    id: 1,
    name: "Wireless Mouse",
    price: 49.99,
    stock: 12,
    imageUrl: "https://example.com/mouse.jpg",
    published: true,
  };

  it("renders product name", () => {
    render(<ProductCard product={mockProduct} />);

    expect(screen.getByText("Wireless Mouse")).toBeInTheDocument();
  });

  it("renders product price using PriceDisplay", () => {
    render(<ProductCard product={mockProduct} />);

    expect(screen.getByText("$49.99")).toBeInTheDocument();
  });

  it("renders product image using ProductImage", () => {
    render(<ProductCard product={mockProduct} />);

    const img = screen.getByAltText("Wireless Mouse");
    expect(img).toBeInTheDocument();
    expect(img).toHaveAttribute("src", "https://example.com/mouse.jpg");
  });

  it("renders stock badge using StockBadge", () => {
    render(<ProductCard product={mockProduct} />);

    expect(screen.getByText("In Stock")).toBeInTheDocument();
  });

  it("calls onClick when card is clicked", () => {
    const handleClick = vi.fn();
    render(<ProductCard product={mockProduct} onClick={handleClick} />);

    const card = screen.getByText("Wireless Mouse").closest(".cursor-pointer");
    fireEvent.click(card!);

    expect(handleClick).toHaveBeenCalledWith(mockProduct);
  });

  it("does not have cursor-pointer when onClick is not provided", () => {
    render(<ProductCard product={mockProduct} />);

    const card =
      screen.getByText("Wireless Mouse").parentElement?.parentElement;
    expect(card).not.toHaveClass("cursor-pointer");
  });

  it("renders low stock badge for low stock product", () => {
    const lowStockProduct: ProductResponse = {
      ...mockProduct,
      stock: 5,
    };

    render(<ProductCard product={lowStockProduct} />);

    expect(screen.getByText("Low Stock")).toBeInTheDocument();
  });

  it("renders out of stock badge for zero stock", () => {
    const outOfStockProduct: ProductResponse = {
      ...mockProduct,
      stock: 0,
    };

    render(<ProductCard product={outOfStockProduct} />);

    expect(screen.getByText("Out of Stock")).toBeInTheDocument();
  });

  it("applies custom className", () => {
    const { container } = render(
      <ProductCard product={mockProduct} className="custom-class" />,
    );

    expect(container.firstChild).toHaveClass("custom-class");
  });

  it("renders with hover effect when clickable", () => {
    render(<ProductCard product={mockProduct} onClick={vi.fn()} />);

    const card = screen.getByText("Wireless Mouse").closest(".cursor-pointer");
    expect(card).toHaveClass("hover:shadow-lg");
  });
});
