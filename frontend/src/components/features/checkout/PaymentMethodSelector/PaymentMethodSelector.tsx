/**
 * PaymentMethodSelector Component
 * Displays BPAY as the only available payment method
 * Based on Figma design: Customer Details Page - Payment Method card
 */

import React from "react";
import { type UseFormRegister } from "react-hook-form";
import { Card } from "@/components/ui/Card/Card";
import { Badge } from "@/components/ui/Badge/Badge";
import type { CheckoutDetailsFormData } from "@/schemas/checkout.schema";

export interface PaymentMethodSelectorProps {
  register: UseFormRegister<CheckoutDetailsFormData>;
  disabled?: boolean;
}

/**
 * PaymentMethodSelector Component
 * Shows BPAY as the pre-selected payment method with "Recommended" badge
 *
 * @param register - React Hook Form register function
 * @param disabled - Disable selection (during submission)
 */
export const PaymentMethodSelector: React.FC<PaymentMethodSelectorProps> = ({
  register,
  disabled = false,
}) => {
  return (
    <Card padding="lg">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">
        Payment Method
      </h2>

      {/* BPAY Option - Blue Background Card */}
      <div className="p-4 bg-blue-50 border-2 border-[#bedbff] rounded-lg">
        <div className="flex items-start space-x-4">
          {/* Radio Button */}
          <input
            {...register("payment_method")}
            type="radio"
            id="payment-bpay"
            value="BPAY"
            defaultChecked
            disabled={disabled}
            className="mt-1 h-4 w-4 text-primary-600 border-gray-300 focus:ring-primary-500 disabled:cursor-not-allowed"
          />

          {/* BPAY Details */}
          <label htmlFor="payment-bpay" className="flex-1 cursor-pointer">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-base font-medium text-gray-900">BPAY</span>
              <Badge
                variant="info"
                size="sm"
                className="bg-[#155dfc] text-white"
              >
                Recommended
              </Badge>
            </div>
            <p className="text-sm text-gray-600">
              Pay securely using your bank's BPAY service. You'll receive
              payment instructions after completing this step.
            </p>
          </label>
        </div>
      </div>

      {/* Info Message */}
      <div className="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
        <p className="text-xs text-blue-800">
          <strong>Note:</strong> BPAY is currently the only available payment
          method. Payment instructions will be displayed on the next page.
        </p>
      </div>

      {/* Security Reassurance */}
      <div className="mt-4 flex items-center gap-2 text-sm text-gray-600">
        <svg
          className="w-4 h-4 text-gray-500"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"
          />
        </svg>
        <span>Your payment information is encrypted and secure</span>
      </div>
    </Card>
  );
};
