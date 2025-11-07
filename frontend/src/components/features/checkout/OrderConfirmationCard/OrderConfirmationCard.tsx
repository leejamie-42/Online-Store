/**
 * Order Confirmation Card Component
 * Displays order confirmation with success state, order ID, and status checklist
 */

import React from "react";
import { LuCircleCheck } from "react-icons/lu";
import { Card, CardContent } from "@/components/ui/Card/Card";

export interface OrderConfirmationCardProps {
  orderId: string;
  isRedirecting?: boolean;
  redirectMessage?: string;
}

export const OrderConfirmationCard: React.FC<OrderConfirmationCardProps> = ({
  orderId,
  isRedirecting = false,
  redirectMessage = "Redirecting to order tracking...",
}) => {
  return (
    <Card className="text-center" padding="lg">
      <CardContent>
        {/* Success Icon */}
        <div className="flex justify-center mb-6">
          <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center">
            <LuCircleCheck
              className="w-12 h-12 text-green-600"
              strokeWidth={2}
            />
          </div>
        </div>

        {/* Success Message */}
        <h1 className="text-2xl font-semibold text-green-600 mb-2">
          Your order has been confirmed
        </h1>

        {/* Order ID Section */}
        <div className="bg-gray-50 rounded-lg p-4 mb-6">
          <div className="text-left">
            <p className="text-sm text-gray-600 mb-1">Order ID</p>
            <p className="text-lg font-mono font-semibold text-gray-900">
              {orderId}
            </p>
          </div>
        </div>

        {/* Status Checklist */}
        <div className="text-left space-y-3 mb-6">
          <div className="flex items-center gap-2">
            <LuCircleCheck className="w-5 h-5 text-green-600 flex-shrink-0" />
            <span className="text-gray-700">Email confirmation sent</span>
          </div>
          <div className="flex items-center gap-2">
            <LuCircleCheck className="w-5 h-5 text-green-600 flex-shrink-0" />
            <span className="text-gray-700">Payment processed securely</span>
          </div>
          <div className="flex items-center gap-2">
            <LuCircleCheck className="w-5 h-5 text-green-600 flex-shrink-0" />
            <span className="text-gray-700">Order is being prepared</span>
          </div>
        </div>

        {/* Redirect Message */}
        {isRedirecting && (
          <div className="text-sm text-gray-500 italic">{redirectMessage}</div>
        )}
      </CardContent>
    </Card>
  );
};
