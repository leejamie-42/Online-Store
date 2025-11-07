/**
 * CheckoutReview Page
 * Step 1 of 4-step checkout flow: Review order items and pricing
 * Based on Figma design: node-id=16-3
 */

import React from "react";
import { useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/Button/Button";
import { StepIndicator } from "@/components/features/checkout/StepIndicator/StepIndicator";
import { OrderSummary } from "@/components/features/checkout/OrderSummary/OrderSummary";
import { OrderTotal } from "@/components/features/checkout/OrderTotal/OrderTotal";
import { useCartStore } from "@/stores/cart.store";
import { calculateCartSummary } from "@/utils/order.utils";
import { CHECKOUT_STEPS } from "@/config/checkout.constants";
import { ROUTES } from "@/config/routes";
import type { OrderItem } from "@/types/order.types";

/**
 * CheckoutReview Page Component
 * Displays order review with step indicator, order summary, and pricing
 */
export const CheckoutReview: React.FC = () => {
  const navigate = useNavigate();
  const { items } = useCartStore();

  // Convert cart items to order items
  const orderItems: OrderItem[] = items.map((item) => ({
    product: item.product,
    quantity: item.quantity,
    price: item.product.price * item.quantity,
  }));

  // Calculate pricing summary
  const summary = calculateCartSummary(items);

  // Handle continue to next step
  const handleContinue = () => {
    navigate("/checkout/details");
  };

  // Handle back to shopping
  const handleContinueShopping = () => {
    navigate(ROUTES.HOME);
  };

  // Redirect if cart is empty (would happen on direct URL access)
  React.useEffect(() => {
    if (items.length === 0) {
      // Could show a toast notification here
      navigate(ROUTES.HOME);
    }
  }, [items.length, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-7xl">
        {/* Step Indicator */}
        <StepIndicator currentStep={1} steps={CHECKOUT_STEPS} />

        {/* Main Content: Two-column layout */}
        <div className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Left Column: Order Summary (2/3 width on desktop) */}
          <div className="lg:col-span-2">
            <OrderSummary items={orderItems} />

            {/* Action Buttons */}
            <div className="mt-6 flex flex-col sm:flex-row gap-4">
              <Button
                variant="outline"
                onClick={handleContinueShopping}
                className="sm:w-auto w-full"
              >
                ← Continue Shopping
              </Button>
              <Button
                variant="primary"
                onClick={handleContinue}
                disabled={items.length === 0}
                className="sm:flex-1 w-full"
              >
                Continue to Details →
              </Button>
            </div>
          </div>

          {/* Right Column: Order Total (1/3 width on desktop) */}
          <div className="lg:col-span-1">
            <OrderTotal summary={summary} />
          </div>
        </div>
      </div>
    </div>
  );
};
