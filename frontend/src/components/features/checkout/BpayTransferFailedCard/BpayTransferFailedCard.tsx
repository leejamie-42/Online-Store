/**
 * BpayTransferFailedCard Component
 * Displays BPAY transfer failure information with action buttons
 */

import React from "react";
import { LuCircleX } from "react-icons/lu";
import { Card, CardContent } from "@/components/ui/Card/Card";
import { Button } from "@/components/ui/Button";

export interface BpayTransferFailedCardProps {
  onRetry: () => void;
  onUpdateDetails: () => void;
  onCancel: () => void;
  isRetrying?: boolean;
  className?: string;
}

export const BpayTransferFailedCard: React.FC<BpayTransferFailedCardProps> = ({
  onRetry,
  onUpdateDetails,
  onCancel,
  isRetrying = false,
  className = "",
}) => {
  return (
    <Card className={className}>
      <CardContent className="p-6 sm:p-8">
        {/* Error Icon */}
        <div className="flex justify-center mb-6">
          <div className="relative">
            <div className="absolute inset-0 bg-red-100 rounded-full blur-xl opacity-50" />
            <div className="relative bg-red-50 rounded-full p-4">
              <LuCircleX className="w-12 h-12 text-red-600" />
            </div>
          </div>
        </div>

        {/* Error Title */}
        <h2 className="text-xl font-semibold text-red-600 text-center mb-2">
          BPAY Transfer Failed
        </h2>

        {/* Error Message */}
        <p className="text-center text-gray-700 mb-6">
          We couldn't complete your BPAY transfer. Please try again.
        </p>

        {/* Common Reasons Box */}
        <div className="bg-red-50 border border-red-100 rounded-lg p-4 mb-6">
          <h3 className="text-sm font-semibold text-gray-900 mb-3">
            Common reasons for BPAY failure:
          </h3>
          <ul className="space-y-2 text-sm text-gray-700">
            <li className="flex items-start">
              <span className="mr-2">•</span>
              <span>Insufficient funds in your account</span>
            </li>
            <li className="flex items-start">
              <span className="mr-2">•</span>
              <span>Bank declined the transaction</span>
            </li>
            <li className="flex items-start">
              <span className="mr-2">•</span>
              <span>Connection timeout with bank</span>
            </li>
            <li className="flex items-start">
              <span className="mr-2">•</span>
              <span>Invalid BPAY reference number</span>
            </li>
            <li className="flex items-start">
              <span className="mr-2">•</span>
              <span>Daily transfer limit reached</span>
            </li>
          </ul>
        </div>

        {/* Action Buttons */}
        <div className="space-y-3">
          <Button
            onClick={onRetry}
            disabled={isRetrying}
            className="w-full"
            size="lg"
          >
            {isRetrying ? "Retrying..." : "Retry Payment"}
          </Button>

          <Button
            onClick={onUpdateDetails}
            variant="outline"
            disabled={isRetrying}
            className="w-full"
            size="lg"
          >
            Update Details
          </Button>

          <button
            onClick={onCancel}
            disabled={isRetrying}
            className="w-full text-center text-sm text-gray-600 hover:text-gray-900 py-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Cancel & Return Home
          </button>
        </div>
      </CardContent>
    </Card>
  );
};
