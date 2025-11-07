import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { ProductInfo } from "./ProductInfo";
import type { ProductDetail } from "@/types/product.types";

const mockProduct: ProductDetail = {
  id: "PRO-1",
  name: "Wireless Mouse",
  description: "Ergonomic and precise wireless mouse",
  price: 49.99,
  stock: 25,
  imageUrl: "https://example.com/mouse.jpg",
  published: true,
};

describe("ProductInfo", () => {
  it("renders product name", () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText("Wireless Mouse")).toBeInTheDocument();
  });

  it("renders category badge", () => {
    render(<ProductInfo product={mockProduct} category="Accessories" />);

    expect(screen.getByText("Accessories")).toBeInTheDocument();
  });

  it("renders product price", () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText("$49.99")).toBeInTheDocument();
  });

  it("renders description heading", () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText("Description")).toBeInTheDocument();
  });

  it("renders product description", () => {
    render(<ProductInfo product={mockProduct} />);

    expect(screen.getByText(/ergonomic and precise/i)).toBeInTheDocument();
  });

  it("applies correct styling", () => {
    const { container } = render(<ProductInfo product={mockProduct} />);

    const wrapper = container.firstChild;
    expect(wrapper).toHaveClass("space-y-[24px]");
  });
});
