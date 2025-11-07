import { describe, it, expect, beforeEach, vi } from "vitest";
import { renderHook, waitFor } from "@testing-library/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { createElement } from "react";
import { useProducts, useProduct } from "../useProducts";
import { productService } from "@/api/services/product.service";
import type { Product } from "@/types/product.types";

// Mock the product service
vi.mock("@/api/services/product.service");

// Test wrapper with QueryClient
const createWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });
  return ({ children }: { children: React.ReactNode }) =>
    createElement(QueryClientProvider, { client: queryClient }, children);
};

describe("useProducts", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("should fetch products successfully", async () => {
    const mockProducts: Product[] = [
      {
        id: "PRD-1",
        name: "Wireless Mouse",
        price: 49.99,
        stock: 12,
        imageUrl: "https://example.com/mouse.jpg",
        published: true,
      },
    ];

    vi.mocked(productService.getProducts).mockResolvedValue(mockProducts);

    const { result } = renderHook(() => useProducts(), {
      wrapper: createWrapper(),
    });

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockProducts);
  });

  it("should handle errors", async () => {
    vi.mocked(productService.getProducts).mockRejectedValue(
      new Error("Failed to fetch"),
    );

    const { result } = renderHook(() => useProducts(), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isError).toBe(true);
    });

    expect(result.current.error).toBeDefined();
  });

  it("should accept filters parameter", async () => {
    const filters = { published: true, minPrice: 10 };

    vi.mocked(productService.getProducts).mockResolvedValue([]);

    renderHook(() => useProducts(filters), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(productService.getProducts).toHaveBeenCalledWith(filters);
    });
  });
});

describe("useProduct", () => {
  it("should fetch product detail successfully", async () => {
    const mockProduct = {
      id: "PRD-1",
      name: "Wireless Mouse",
      description: "Ergonomic",
      price: 49.99,
      stock: 25,
      available_quantity: 12,
      imageUrl: "https://example.com/mouse.jpg",
      published: true,
    };

    vi.mocked(productService.getProduct).mockResolvedValue(mockProduct);

    const { result } = renderHook(() => useProduct("PRD-1"), {
      wrapper: createWrapper(),
    });

    await waitFor(() => {
      expect(result.current.isSuccess).toBe(true);
    });

    expect(result.current.data).toEqual(mockProduct);
  });

  it("should not fetch if productId is empty", () => {
    const { result } = renderHook(() => useProduct(undefined), {
      wrapper: createWrapper(),
    });

    expect(result.current.isFetching).toBe(false);
  });
});
