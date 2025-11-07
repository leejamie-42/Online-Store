/**
 * BpayPaymentPage Component
 * Step 3 of 4: Display BPAY payment instructions and handle payment transfer
 */

import React, { useState } from "react";
import { useParams, useNavigate, useSearchParams } from "react-router-dom";
import { LuCircleAlert } from "react-icons/lu";
import { Button } from "@/components/ui/Button/Button";
import { Alert, AlertDescription } from "@/components/ui/Alert/Alert";
import { StepIndicator } from "@/components/features/checkout/StepIndicator/StepIndicator";
import { BpayPaymentInfo } from "@/components/features/checkout/BpayPaymentInfo/BpayPaymentInfo";
import { BpayProcessingView } from "@/components/features/checkout/BpayProcessingView/BpayProcessingView";
import { BpayTransferFailedCard } from "@/components/features/checkout/BpayTransferFailedCard";
import { usePayment } from "@/hooks/usePayment";
import { useBpayMutation } from "@/hooks/useBpayMutation";
import { useOrderStatusPolling } from "@/hooks/useOrderStatusPolling";
import { useCartStore } from "@/stores/cart.store";
import { CHECKOUT_STEPS } from "@/config/checkout.constants";
import { ROUTES } from "@/config/routes";

/**
 * BpayPaymentPage Component
 *
 * Flow:
 * 1. Get payment_id from URL params and order_id from query params
 * 2. Fetch BPAY details: GET /api/payments/:id (via usePayment)
 * 3. Display payment information
 * 4. User confirms payment → Trigger Bank Service transfer → Show processing view
 * 5. Poll order status until payment confirmed → Navigate to confirmation
 */
export const BpayPaymentPage: React.FC = () => {
  const navigate = useNavigate();
  const { paymentId } = useParams<{ paymentId: string }>();
  const [searchParams] = useSearchParams();
  const orderId = searchParams.get("order_id");
  const { clearCart } = useCartStore();

  // View state: 'payment-info' | 'processing' | 'transfer-failed'
  const [viewState, setViewState] = useState<
    "payment-info" | "processing" | "transfer-failed"
  >("payment-info");

  // Fetch BPAY details using payment_id
  const { bpayDetails, isLoading, error } = usePayment(paymentId);

  // Handle payment transfer completion
  const { completePaymentTransfer, isCompletingTransfer, transferError } =
    useBpayMutation();

  // Poll order status after payment transfer initiated
  const { error: pollingError } = useOrderStatusPolling({
    orderId: orderId || "",
    enabled: viewState === "processing" && !!orderId,
    interval: 2000, // Poll every 2 seconds
    maxDuration: 60000, // Timeout after 60 seconds
    onSuccess: (order) => {
      // Payment confirmed, clear cart and navigate to confirmation
      if (order.status == "processing") {
        clearCart();
        navigate(ROUTES.CHECKOUT_CONFIRMATION(order.id));
      }
    },
    onTimeout: () => {
      // Timeout occurred, show transfer failed view
      console.error("Payment verification timeout");
      setViewState("transfer-failed");
    },
    onError: (error) => {
      // Polling error, show transfer failed view
      console.error("Order status polling error:", error);
      setViewState("transfer-failed");
    },
  });

  // Handle payment confirmation - triggers Bank Service and switches to processing view
  const handleConfirmPayment = async () => {
    if (!bpayDetails || !orderId) return;

    try {
      // Switch to processing view immediately
      setViewState("processing");
      // Complete BPAY transfer (simulates user paying through their bank)
      // This is a fire-and-forget operation - we poll for status instead of waiting
      completePaymentTransfer(bpayDetails, orderId);
    } catch (error) {
      console.error("Payment transfer failed:", error);
      setViewState("transfer-failed");
    }
  };

  const handleProcessingTimeout = () => {
    setViewState("transfer-failed");
  };

  // Handle retry payment - reset to payment info view
  const handleRetryPayment = () => {
    setViewState("payment-info");
  };

  // Handle update details - navigate back to checkout details
  const handleUpdateDetails = () => {
    navigate(ROUTES.CHECKOUT_DETAILS);
  };

  // Handle cancel - return to home
  const handleCancelAndReturnHome = () => {
    clearCart();
    navigate(ROUTES.HOME);
  };

  // Loading state
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          <StepIndicator
            currentStep={3}
            steps={CHECKOUT_STEPS}
            completedSteps={[1, 2]}
          />
          <div className="mt-8 text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600 mx-auto mb-4" />
            <p className="text-gray-600">Loading payment details...</p>
          </div>
        </div>
      </div>
    );
  }

  // Error state
  if (error || !bpayDetails || !orderId) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="container mx-auto px-4 max-w-3xl">
          <StepIndicator
            currentStep={3}
            steps={CHECKOUT_STEPS}
            completedSteps={[1, 2]}
          />
          <div className="mt-8">
            <Alert variant="error" className="mb-4">
              <LuCircleAlert className="w-4 h-4" />
              <AlertDescription>
                {error?.message || !orderId
                  ? "Missing order information"
                  : "Failed to load payment details"}
              </AlertDescription>
            </Alert>
            <div className="flex gap-4 justify-center">
              <Button variant="outline" onClick={() => navigate(ROUTES.HOME)}>
                Return Home
              </Button>
              <Button onClick={() => navigate(ROUTES.CHECKOUT_DETAILS)}>
                Back to Checkout
              </Button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Success: Display appropriate view based on state
  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-3xl">
        {/* Step Indicator */}
        <StepIndicator
          currentStep={3}
          steps={CHECKOUT_STEPS}
          completedSteps={[1, 2]}
        />

        {/* Content Area - Toggle between Payment Info, Processing View, and Failed View */}
        <div className="mt-8">
          {viewState === "payment-info" ? (
            <>
              {/* BPAY Payment Information */}
              <BpayPaymentInfo
                bpayDetails={bpayDetails}
                onConfirmPayment={handleConfirmPayment}
                isProcessing={isCompletingTransfer}
                showConfirmButton={true}
              />

              {/* Transfer Error Alert */}
              {transferError && (
                <div className="mt-4">
                  <Alert variant="error">
                    <LuCircleAlert className="w-4 h-4" />
                    <AlertDescription>
                      Payment transfer failed: {transferError.message}
                    </AlertDescription>
                  </Alert>
                </div>
              )}

              {/* Polling Error Alert */}
              {pollingError && (
                <div className="mt-4">
                  <Alert variant="error">
                    <LuCircleAlert className="w-4 h-4" />
                    <AlertDescription>
                      Payment verification failed: {pollingError.message}
                    </AlertDescription>
                  </Alert>
                </div>
              )}
            </>
          ) : viewState === "processing" ? (
            /* Processing View */
            <BpayProcessingView
              maxTime={10}
              onTimeout={handleProcessingTimeout}
            />
          ) : (
            /* Transfer Failed View */
            <BpayTransferFailedCard
              onRetry={handleRetryPayment}
              onUpdateDetails={handleUpdateDetails}
              onCancel={handleCancelAndReturnHome}
              isRetrying={isCompletingTransfer}
            />
          )}
        </div>
      </div>
    </div>
  );
};
