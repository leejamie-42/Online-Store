/**
 * CancelOrderCard Component
 * Displays order cancellation information and button
 * Only shown for orders with status "pending" or "processing"
 * Uses React Query mutation for automatic state management and cache updates
 */

import React, { useState } from "react";
import { Card, CardContent } from "@/components/ui/Card/Card";
import { Button } from "@/components/ui/Button";
import { Spinner } from "@/components/ui/Spinner";
import { useCancelOrder } from "@/hooks/useOrders";
import { removePrfixToGetId } from "@/utils/formatters";

export interface CancelOrderCardProps {
  orderId: string;
  className?: string;
}

export const CancelOrderCard: React.FC<CancelOrderCardProps> = ({
  orderId,
  className = "",
}) => {
  const [showConfirmation, setShowConfirmation] = useState(false);
  const cancelOrderMutation = useCancelOrder();

  const handleCancelClick = () => {
    setShowConfirmation(true);
  };

  const handleConfirmCancel = () => {
    // Extract numeric ID from formatted ID (e.g., "ORD-123" -> "123")
    const numericId = removePrfixToGetId(orderId);

    cancelOrderMutation.mutate(numericId, {
      onSuccess: () => {
        // Close confirmation dialog on success
        setShowConfirmation(false);
      },
      onError: (error) => {
        console.error("Failed to cancel order:", error);
        // Keep dialog open on error so user can retry
      },
    });
  };

  const handleCancelConfirmation = () => {
    setShowConfirmation(false);
  };

  return (
    <Card className={className}>
      <CardContent className="p-4 sm:p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-3">
          Cancel Order
        </h3>

        <p className="text-sm text-gray-700 mb-4">
          You can cancel this order before it's dispatched for delivery. A full
          refund will be processed within 5-7 business days.
        </p>

        {!showConfirmation ? (
          <Button
            onClick={handleCancelClick}
            variant="danger"
            disabled={cancelOrderMutation.isPending}
            className="w-full sm:w-auto"
          >
            Cancel Order
          </Button>
        ) : (
          <div className="space-y-3">
            <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
              <p className="text-sm font-medium text-yellow-800 mb-1">
                Are you sure you want to cancel this order?
              </p>
              <p className="text-xs text-yellow-700">
                This action cannot be undone. A refund will be issued to your
                original payment method.
              </p>
            </div>

            {/* Error message if cancellation fails */}
            {cancelOrderMutation.isError && (
              <div className="p-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm font-medium text-red-800">
                  Failed to cancel order. Please try again.
                </p>
              </div>
            )}

            <div className="flex gap-3">
              <Button
                onClick={handleConfirmCancel}
                variant="danger"
                disabled={cancelOrderMutation.isPending}
                className="flex-1 sm:flex-initial"
              >
                {cancelOrderMutation.isPending ? (
                  <>
                    <Spinner size="sm" className="mr-2" />
                    Cancelling...
                  </>
                ) : (
                  "Yes, Cancel Order"
                )}
              </Button>
              <Button
                onClick={handleCancelConfirmation}
                variant="outline"
                disabled={cancelOrderMutation.isPending}
                className="flex-1 sm:flex-initial"
              >
                No, Keep Order
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
};
