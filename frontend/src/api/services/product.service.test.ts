import { describe, it, expect, beforeEach, vi } from "vitest";
import type {
  ProductResponse,
  ProductDetailResponse,
  ProductDetail,
  Product,
  ProductFilters,
} from "@/types/product.types";

// Mock the environment before importing the service
vi.stubEnv("VITE_USE_MOCK_DATA", "false");

// Mock axios
vi.mock("@/lib/axios", () => ({
  apiClient: {
    get: vi.fn(),
  },
}));

// Mock the mock data module
vi.mock("@/mocks/products.mock", () => ({
  mockProducts: [],
  mockProductDetails: {},
  delay: vi.fn().mockResolvedValue(undefined),
  filterMockProducts: vi.fn().mockReturnValue([]),
}));

// Import after mocking
const { apiClient } = await import("@/lib/axios");
const { productService } = await import("./product.service");

describe("productService", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("getProducts", () => {
    it("should fetch products successfully", async () => {
      const mockProducts: ProductResponse[] = [
        {
          id: 1,
          name: "Wireless Mouse",
          price: 49.99,
          stock: 12,
          imageUrl: "https://example.com/mouse.jpg",
          published: true,
        },
      ];

      const exptectedProducts: Product[] = [
        {
          id: "PRO-1",
          name: "Wireless Mouse",
          price: 49.99,
          stock: 12,
          imageUrl: "https://example.com/mouse.jpg",
          published: true,
        },
      ];

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProducts });

      const result = await productService.getProducts();

      expect(apiClient.get).toHaveBeenCalledWith("/products", {
        params: undefined,
      });
      expect(result).toEqual(exptectedProducts);
    });

    it("should fetch products with filters", async () => {
      const filters: ProductFilters = {
        published: true,
        minPrice: 10,
        maxPrice: 100,
        search: "mouse",
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: [] });

      await productService.getProducts(filters);

      expect(apiClient.get).toHaveBeenCalledWith("/products", {
        params: filters,
      });
    });

    it("should handle errors when fetching products", async () => {
      vi.mocked(apiClient.get).mockRejectedValue(new Error("Network error"));

      await expect(productService.getProducts()).rejects.toThrow(
        "Network error",
      );
    });
  });

  describe("getProduct", () => {
    it("should fetch product detail successfully", async () => {
      const mockProduct: ProductDetailResponse = {
        id: 1,
        name: "Wireless Mouse",
        description: "Ergonomic and precise",
        price: 49.99,
        stock: 25,
        imageUrl: "https://example.com/mouse.jpg",
        published: true,
      };

      const expectedProduct: ProductDetail = {
        id: "PRO-1",
        name: "Wireless Mouse",
        description: "Ergonomic and precise",
        price: 49.99,
        stock: 25,
        imageUrl: "https://example.com/mouse.jpg",
        published: true,
      };

      vi.mocked(apiClient.get).mockResolvedValue({ data: mockProduct });

      const result = await productService.getProduct("PRD-1");

      expect(apiClient.get).toHaveBeenCalledWith("/products/1");
      expect(result).toEqual(expectedProduct);
    });

    it("should handle 404 when product not found", async () => {
      vi.mocked(apiClient.get).mockRejectedValue({
        response: { status: 404 },
      });

      await expect(productService.getProduct("PRD-999")).rejects.toThrow();
    });
  });
});
