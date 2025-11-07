import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import type { UseQueryResult } from "@tanstack/react-query";
import Home from "./Home";
import type { Product } from "@/types/product.types";

// Mock the hooks
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("@/hooks/useProducts", () => ({
  useProducts: vi.fn(),
}));

import { useProducts } from "@/hooks/useProducts";

describe("Home", () => {
  const mockProducts: Product[] = [
    {
      id: "PRO-1",
      name: "Wireless Mouse",
      price: 49.99,
      stock: 12,
      imageUrl: "https://example.com/mouse.jpg",
      published: true,
    },
    {
      id: "PRO-2",
      name: "Mechanical Keyboard",
      price: 129.99,
      stock: 8,
      imageUrl: "https://example.com/keyboard.jpg",
      published: true,
    },
  ];

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders hero section", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: [] as Product[],
      isLoading: false,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    expect(screen.getByText("Welcome to Our Store")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Discover our collection of quality products at great prices",
      ),
    ).toBeInTheDocument();
  });

  it("renders loading state", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: undefined,
      isLoading: true,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    expect(screen.getByText("Featured Products")).toBeInTheDocument();
    // ProductSkeleton components should be rendered (8 skeleton cards)
    const skeletons = document.querySelectorAll(".animate-pulse");
    expect(skeletons.length).toBeGreaterThan(0);
  });

  it("renders error state", () => {
    const errorMessage = "Failed to fetch products";
    vi.mocked(useProducts).mockReturnValue({
      data: undefined,
      isLoading: false,
      isError: true,
      error: new Error(errorMessage),
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    // Updated error message from enhanced error handling
    expect(screen.getByText("Failed to fetch products")).toBeInTheDocument();
    expect(
      screen.getByText(/Please try again or contact support/),
    ).toBeInTheDocument();
  });

  it("renders products grid", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: mockProducts,
      isLoading: false,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    expect(screen.getByText("Wireless Mouse")).toBeInTheDocument();
    expect(screen.getByText("Mechanical Keyboard")).toBeInTheDocument();
  });

  it("renders empty state when no products", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: [] as Product[],
      isLoading: false,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    expect(screen.getByText("No products available")).toBeInTheDocument();
  });

  it("navigates to product detail when product card is clicked", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: mockProducts,
      isLoading: false,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    // Click on the first product card
    const productCard = screen
      .getByText("Wireless Mouse")
      .closest(".cursor-pointer");
    fireEvent.click(productCard!);

    // Verify navigate was called with the correct path (using numeric ID)
    expect(mockNavigate).toHaveBeenCalledWith("/products/PRO-1");
  });

  it("navigates to correct product detail for second product", () => {
    vi.mocked(useProducts).mockReturnValue({
      data: mockProducts,
      isLoading: false,
      isError: false,
      error: null,
    } as UseQueryResult<Product[], Error>);

    render(
      <BrowserRouter>
        <Home />
      </BrowserRouter>,
    );

    // Click on the second product card
    const productCard = screen
      .getByText("Mechanical Keyboard")
      .closest(".cursor-pointer");
    fireEvent.click(productCard!);

    // Verify navigate was called with the correct path (using numeric ID)
    expect(mockNavigate).toHaveBeenCalledWith("/products/PRO-2");
  });
});
