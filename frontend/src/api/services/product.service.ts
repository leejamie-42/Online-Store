import { apiClient } from "@/lib/axios";
import { API_ENDPOINTS } from "@/config/api.config";
import type {
  Product,
  ProductDetail,
  ProductResponse,
  ProductDetailResponse,
  ProductFilters,
} from "@/types/product.types";
import { convertIdWithPrifx, removePrfixToGetId } from "@/utils/formatters";
import { ID_PREFIX } from "@/utils/constants";

/**
 * Feature flag to use mock data instead of API
 * Set to true when backend API is not available
 * Exported for testing purposes
 */
export const USE_MOCK_DATA = import.meta.env.VITE_USE_MOCK_DATA === "true";

/**
 * Product Service
 * Handles all product-related API calls
 * Falls back to mock data when USE_MOCK_DATA is true
 */
export const productService = {
  /**
   * Get list of products with optional filters
   * @param filters - Optional filters for products
   * @returns Promise<Product[]>
   */
  async getProducts(filters?: ProductFilters): Promise<Product[]> {
    const response = await apiClient.get<ProductResponse[]>(
      API_ENDPOINTS.PRODUCTS,
      {
        params: filters,
      },
    );

    return response.data.map(transformProductResponse);
  },

  /**
   * Get detailed information about a specific product
   * @param productId - Product ID (number)
   * @returns Promise<ProductDetail>
   */
  async getProduct(productId: string): Promise<ProductDetail> {
    // Real API call - backend returns ProductDetail directly
    const response = await apiClient.get<ProductDetailResponse>(
      API_ENDPOINTS.PRODUCT_DETAIL(removePrfixToGetId(productId)),
    );
    return transformProductDetailResponse(response.data);
  },
};

function transformProductDetailResponse(
  productResponse: ProductDetailResponse,
): ProductDetail {
  const { id, name, price, imageUrl, stock, description, published } =
    productResponse;

  return {
    id: convertIdWithPrifx(ID_PREFIX.PRODUCT, id),
    name,
    price,
    imageUrl,
    stock,
    published,
    description,
  };
}

function transformProductResponse(productResponse: ProductResponse): Product {
  const { id, name, price, imageUrl, stock, published } = productResponse;

  return {
    id: convertIdWithPrifx(ID_PREFIX.PRODUCT, id),
    name,
    price,
    imageUrl,
    stock,
    published,
  };
}
