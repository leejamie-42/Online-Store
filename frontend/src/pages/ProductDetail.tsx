import { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useProduct } from "@/hooks/useProducts";
import { useAuth } from "@/hooks/useAuth";
import { ProductGallery } from "@/components/features/product/ProductGallery";
import { ProductInfo } from "@/components/features/product/ProductInfo";
import { ProductFeatures } from "@/components/features/product/ProductFeatures";
import { QuantitySelector } from "@/components/common/QuantitySelector";
import { LuArrowLeft, LuShoppingCart, LuLogIn } from "react-icons/lu";
import { ROUTES } from "@/config/routes";
import { handleApiError } from "@/utils/errorHandling";
import { useCartStore } from "@/stores/cart.store";

/**
 * ProductDetail Page
 * Display detailed product information
 * Based on Figma design: node-id 1:196
 */
function ProductDetail() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const [quantity, setQuantity] = useState(1);
  const { addItem } = useCartStore();

  // Convert URL parameter (string) to number for the hook
  const { data: product, isLoading, isError, error } = useProduct(id);

  const handleBuyNow = () => {
    if (!product) {
      return;
    }

    const { id, name, price, stock, imageUrl, published } = product;

    if (!isAuthenticated) {
      // Redirect to login with return URL
      navigate(ROUTES.LOGIN, { state: { from: `/products/${id}` } });
      return;
    }

    console.log("Proceeding to checkout:", id, "quantity:", quantity);

    addItem(
      {
        id,
        name,
        price,
        stock,
        imageUrl,
        published,
      },
      quantity,
    );
    navigate("/checkout"); // This will be a protected route
  };

  const handleBackToProducts = () => {
    navigate("/");
  };

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div className="text-center">
            <p className="text-gray-600 mt-4">Loading product details...</p>
          </div>
        </div>
      </div>
    );
  }

  if (isError || !product) {
    const errorInfo = handleApiError(error);

    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            {errorInfo.message}
          </h3>
          <p className="text-red-600 mb-4">{errorInfo.details}</p>
          {id && errorInfo.status === 404 && (
            <p className="text-sm text-gray-600 mb-4">
              Product ID:{" "}
              <code className="bg-white px-2 py-1 rounded">{id}</code>
            </p>
          )}
          <button
            onClick={handleBackToProducts}
            className="px-4 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
          >
            Back to Products
          </button>
        </div>
      </div>
    );
  }

  const isOutOfStock = product.stock === 0;
  const maxQuantity = Math.min(product.stock, 99);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back Button */}
      <button
        onClick={handleBackToProducts}
        className="
          flex items-center gap-2
          text-[14px] font-medium
          leading-[20px]
          tracking-[-0.1504px]
          text-neutral-950
          hover:text-gray-700
          mb-8
        "
      >
        <LuArrowLeft className="w-4 h-4" />
        Back to Products
      </button>

      {/* Product Details Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-[32px]">
        {/* Product Gallery - Left Column */}
        <div>
          <div
            className="
              bg-white
              border border-[rgba(0,0,0,0.1)] border-solid
              rounded-[14px]
              overflow-hidden
            "
          >
            <ProductGallery
              mainImage={product.imageUrl}
              altText={product.name}
            />
          </div>
        </div>

        {/* Product Info - Right Column */}
        <div className="flex flex-col gap-[24px]">
          {/* Product Information */}
          <ProductInfo product={product} />

          {/* Purchase Card */}
          <div
            className="
              bg-white
              border border-[rgba(0,0,0,0.1)] border-solid
              rounded-[14px]
              p-[17px]
              flex flex-col gap-[16px]
            "
          >
            {/* Quantity Selector Section - Show only if authenticated and in stock */}
            {!isOutOfStock && isAuthenticated && (
              <div className="flex flex-col gap-[8px]">
                <label
                  className="
                    text-[14px]
                    font-medium
                    leading-[14px]
                    tracking-[-0.1504px]
                    text-neutral-950
                  "
                >
                  Quantity
                </label>
                <QuantitySelector
                  value={quantity}
                  onChange={setQuantity}
                  max={maxQuantity}
                />
                <p
                  className="
                    text-[14px]
                    font-normal
                    leading-[20px]
                    tracking-[-0.1504px]
                    text-[#4a5565]
                  "
                >
                  {product.stock} items available in stock
                </p>
              </div>
            )}

            {/* Conditional Buy Now / Login to Buy Button */}
            <button
              onClick={handleBuyNow}
              disabled={isOutOfStock}
              className={`
                h-[40px]
                w-full
                flex items-center justify-center gap-2
                rounded-[8px]
                text-[14px]
                font-medium
                leading-[20px]
                tracking-[-0.1504px]
                ${
                  isOutOfStock
                    ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                    : isAuthenticated
                      ? "bg-[#030213] text-white hover:bg-gray-900"
                      : "bg-blue-600 text-white hover:bg-blue-700"
                }
              `}
            >
              {isOutOfStock ? (
                "Out of Stock"
              ) : isAuthenticated ? (
                <>
                  <LuShoppingCart className="w-4 h-4" />
                  Buy Now
                </>
              ) : (
                <>
                  <LuLogIn className="w-4 h-4" />
                  Login to Buy
                </>
              )}
            </button>

            {/* Helper text for non-authenticated users */}
            {!isAuthenticated && !isOutOfStock && (
              <p className="text-xs text-gray-500 text-center">
                Sign in to purchase this product
              </p>
            )}
          </div>

          {/* Product Features */}
          <ProductFeatures />
        </div>
      </div>
    </div>
  );
}

export default ProductDetail;
