/**
 * Order confirmation Component
 * Step 4 of 4
 */

import React, { useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { StepIndicator } from "@/components/features/checkout/StepIndicator/StepIndicator";
import { OrderConfirmationCard } from "@/components/features/checkout/OrderConfirmationCard";
import { CHECKOUT_STEPS } from "@/config/checkout.constants";
// import { ROUTES } from "@/config/routes";

export const CheckoutConfirmationPage: React.FC = () => {
  const navigate = useNavigate();
  const params = useParams();
  const orderId = params.orderId;
  const [isRedirecting, setIsRedirecting] = React.useState(false);

  // Redirect to order tracking after 3 seconds
  useEffect(() => {
    if (orderId) {
      setIsRedirecting(true);
      const timer = setTimeout(() => {
        // TODO: navigate to order detail page
        // navigate(`${ROUTES.ORDERS}/${orderId}`);
      }, 5000);

      return () => clearTimeout(timer);
    }
  }, [orderId, navigate]);

  // Handle missing order ID
  if (!orderId) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <p className="text-red-700">
              Order ID not found. Please check your email for order
              confirmation.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-3xl">
        {/* Step Indicator */}
        <StepIndicator
          currentStep={4}
          steps={CHECKOUT_STEPS}
          completedSteps={[1, 2, 3, 4]}
        />

        {/* Order Confirmation Card */}
        <div className="mt-8">
          <OrderConfirmationCard
            orderId={orderId}
            isRedirecting={isRedirecting}
          />
        </div>
      </div>
    </div>
  );
};
