import { useNavigate } from "react-router-dom";
import { useProducts } from "@/hooks/useProducts";
import { ProductCard } from "@/components/features/product/ProductCard";
import { ProductSkeleton } from "@/components/features/product/ProductSkeleton";
import { handleApiError } from "@/utils/errorHandling";

function Home() {
  const navigate = useNavigate();
  const { data: products, isLoading, isError, error } = useProducts();

  const errorInfo = isError ? handleApiError(error) : null;

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Hero Section */}
      <div className="mb-12 text-center">
        <h1 className="text-4xl font-bold text-gray-900 mb-4">
          Welcome to Our Store
        </h1>
        <p className="text-lg text-gray-600 max-w-2xl mx-auto">
          Discover our collection of quality products at great prices
        </p>
      </div>

      {/* Products Section */}
      <div className="mb-8">
        <h2 className="text-2xl font-semibold text-gray-900 mb-6">
          Featured Products
        </h2>

        {/* Loading State */}
        {isLoading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {[...Array(8)].map((_, index) => (
              <ProductSkeleton key={index} />
            ))}
          </div>
        )}

        {/* Error State - Enhanced */}
        {isError && errorInfo && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-red-800 mb-2">
              {errorInfo.message}
            </h3>
            <p className="text-red-600 mb-4">{errorInfo.details}</p>
            {errorInfo.status === undefined && (
              <div className="bg-white rounded-md p-3 text-sm text-gray-700">
                <p className="font-medium mb-1">💡 Quick Fix:</p>
                <ol className="list-decimal list-inside space-y-1">
                  <li>
                    Ensure backend is running:{" "}
                    <code className="bg-gray-100 px-1 rounded">
                      cd store-backend && ../gradlew bootRun
                    </code>
                  </li>
                  <li>
                    Verify backend is accessible:{" "}
                    <code className="bg-gray-100 px-1 rounded">
                      curl http://localhost:8081/api/products
                    </code>
                  </li>
                  <li>Check CORS configuration in backend</li>
                </ol>
              </div>
            )}
          </div>
        )}

        {/* Products Grid */}
        {!isLoading && !isError && products && (
          <>
            {products.length === 0 ? (
              <div className="bg-gray-50 border border-gray-200 rounded-lg p-12 text-center">
                <p className="text-gray-600 text-lg">No products available</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                  <ProductCard
                    key={product.id}
                    product={product}
                    onClick={(product) => {
                      navigate(`/products/${product.id}`);
                    }}
                  />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}

export default Home;
