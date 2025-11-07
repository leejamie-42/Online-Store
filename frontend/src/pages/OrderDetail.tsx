import { useParams, useNavigate } from "react-router-dom";
import { useOrder } from "@/hooks/useOrders";
import { OrderStatusStepper } from "@/components/features/order/OrderStatusStepper";
import { OrderStatusMessage } from "@/components/features/order/OrderStatusMessage";
import { OrderProductCard } from "@/components/features/order/OrderProductCard";
import { OrderInfoPanel } from "@/components/features/order/OrderInfoPanel";
import { CancelOrderCard } from "@/components/features/order/CancelOrderCard";
import { Card, CardContent } from "@/components/ui/Card/Card";
import { Spinner } from "@/components/ui/Spinner/Spinner";
import { Button } from "@/components/ui/Button/Button";
import { handleApiError } from "@/utils/errorHandling";

/**
 * OrderDetail Page
 * Displays comprehensive order information with status tracking
 * Uses React Query for data fetching with automatic caching and refetching
 */
const OrderDetail: React.FC = () => {
  const { orderId } = useParams<{ orderId: string }>();
  const navigate = useNavigate();

  // Fetch order data using React Query hook
  const { data: order, isLoading, isError, error } = useOrder(orderId);

  const handleBackToOrders = () => {
    navigate("/orders");
  };

  // Check if order can be cancelled (only pending or processing)
  const canCancelOrder =
    order?.status === "pending" || order?.status === "processing";

  // Check if order is cancelled or refunded (show message instead of stepper)
  const isCancelledOrRefunded =
    order?.status === "cancelled" || order?.status === "refunded";

  // Loading state
  if (isLoading) {
    return (
      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center justify-center min-h-[400px]">
          <div>
            <Spinner size="lg" />
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (isError || !order) {
    const errorInfo = handleApiError(error);

    return (
      <div className="container mx-auto px-4 py-8">
        <div className="bg-red-50 border border-red-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-red-800 mb-2">
            {errorInfo.message}
          </h3>
          <p className="text-red-600 mb-4">
            {errorInfo.details || "Failed to load order details."}
          </p>
          {orderId && errorInfo.status === 404 && (
            <p className="text-sm text-gray-600 mb-4">
              Order ID:{" "}
              <code className="bg-white px-2 py-1 rounded">{orderId}</code>
            </p>
          )}
          <Button onClick={handleBackToOrders} variant="primary">
            Back to Orders
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8 max-w-7xl">
      {/* Back navigation */}
      <div className="mb-4 sm:mb-6">
        <button
          onClick={handleBackToOrders}
          className="flex items-center gap-2 text-sm sm:text-base text-gray-600 hover:text-gray-900 transition-colors"
        >
          <span>←</span>
          <span>Back to Orders</span>
        </button>
      </div>

      {/* Main content grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 sm:gap-6">
        {/* Left column - Order status and product info */}
        <div className="lg:col-span-2 space-y-4 sm:space-y-6">
          {/* Order Status - Show message for cancelled/refunded, stepper for others */}
          <Card>
            <CardContent className="p-4 sm:p-6">
              {isCancelledOrRefunded ? (
                <OrderStatusMessage status={order.status} />
              ) : (
                <OrderStatusStepper currentStatus={order.status} />
              )}
            </CardContent>
          </Card>

          {/* Product Information */}
          <Card>
            <CardContent className="p-4 sm:p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                Order Items
              </h2>
              <div className="space-y-4">
                {order.products.map((product) => (
                  <OrderProductCard key={product.id} product={product} />
                ))}
              </div>

              {/* Order Total */}
              <div className="mt-6 pt-4 border-t border-gray-200">
                <div className="flex items-center justify-between">
                  <span className="text-lg font-semibold text-gray-900">
                    Order Total
                  </span>
                  <span className="text-lg font-bold text-gray-900">
                    {new Intl.NumberFormat("en-US", {
                      style: "currency",
                      currency: "USD",
                    }).format(order.total)}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Cancel Order Card - Only show for pending/processing orders */}
          {canCancelOrder && <CancelOrderCard orderId={order.id} />}
        </div>

        {/* Right column - Order details and support */}
        <div className="space-y-4 sm:space-y-6">
          {/* Order Info Panel */}
          <OrderInfoPanel
            orderId={order.id}
            orderDate={order.createdAt}
            lastUpdated={order.updatedAt}
            shippingInfo={order.shippingInfo}
          />
        </div>
      </div>
    </div>
  );
};

export default OrderDetail;
