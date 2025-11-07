/**
 * BpayPaymentInfo Component
 * Displays BPAY payment information with copy-to-clipboard functionality
 */

import React from "react";
import { LuCopy, LuCreditCard } from "react-icons/lu";
import { Card } from "@/components/ui/Card/Card";
import { useClipboard } from "@/hooks/useClipboard";
import type { BpayInfoResponse } from "@/types";

export interface BpayPaymentInfoProps {
  bpayDetails: BpayInfoResponse;
  onConfirmPayment?: () => void;
  isProcessing?: boolean;
  showConfirmButton?: boolean;
}

/**
 * BpayPaymentInfo Component
 * Displays BPAY payment information with copy buttons
 *
 * Design: Clean card layout with three sections:
 * 1. Biller Code with copy button
 * 2. Reference Number with copy button
 * 3. Amount Due (displayed in blue)
 * 4. Note box with payment instructions
 * 5. Optional confirm payment button
 */
export const BpayPaymentInfo: React.FC<BpayPaymentInfoProps> = ({
  bpayDetails,
  onConfirmPayment,
  isProcessing = false,
  showConfirmButton = true,
}) => {
  const billerCodeClipboard = useClipboard();
  const referenceClipboard = useClipboard();

  return (
    <Card className="w-full max-w-2xl mx-auto space-y-8 py-10 px-10">
      {/* Page Header */}
      <div className="text-center space-y-3">
        <h2 className="text-3xl font-semibold text-gray-900">
          BPAY Payment Information
        </h2>
        <p className="text-base text-gray-600">
          Use the following details to complete your payment
        </p>
      </div>

      {/* Payment Details Card */}
      <Card className="p-8 bg-gray-300 border border-gray-200 shadow-sm">
        {/* Biller Code */}
        <div className="flex items-center justify-between py-6 border-b border-gray-200">
          <div className="space-y-1">
            <p className="text-sm text-gray-600 font-normal">Biller Code</p>
            <p className="text-3xl font-normal text-gray-900 tracking-wide">
              {bpayDetails.billerCode}
            </p>
          </div>
          <button
            onClick={() => billerCodeClipboard.copy(bpayDetails.billerCode)}
            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2"
          >
            <LuCopy className="w-4 h-4" />
            <span>Copy</span>
          </button>
        </div>

        {/* Reference Number */}
        <div className="flex items-center justify-between py-6 border-b border-gray-200">
          <div className="space-y-1">
            <p className="text-sm text-gray-600 font-normal">
              Reference Number
            </p>
            <p className="text-2xl font-normal text-gray-900 tracking-wide">
              {bpayDetails.referenceNumber}
            </p>
          </div>
          <button
            onClick={() => referenceClipboard.copy(bpayDetails.referenceNumber)}
            className="flex items-center gap-2 px-4 py-2 text-sm text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-400 focus:ring-offset-2"
          >
            <LuCopy className="w-4 h-4" />
            <span>Copy</span>
          </button>
        </div>

        {/* Amount Due */}
        <div className="space-y-1 py-6">
          <p className="text-sm text-gray-600 font-normal">Amount Due</p>
          <p className="text-3xl font-normal text-blue-600">
            ${bpayDetails.amount.toFixed(2)} AUD
          </p>
        </div>

        {/* Payment Instructions Note */}
        <div className="p-4 bg-blue-50 border border-blue-200 rounded-lg space-y-2">
          <p className="text-sm text-gray-900 leading-relaxed">
            <span className="font-semibold">Note:</span> Pay via your banking
            app/website within 24 hours.
          </p>
          <p className="text-sm text-gray-700 leading-relaxed">
            We'll email you when payment is confirmed.
          </p>
        </div>
      </Card>
      {/* Confirm Payment Button */}
      {showConfirmButton && onConfirmPayment && (
        <div className="mt-6 flex flex-col items-center gap-3">
          <button
            onClick={onConfirmPayment}
            disabled={isProcessing}
            className="w-full max-w-md px-6 py-3 bg-gray-900 text-white text-base font-medium rounded-lg hover:bg-gray-800 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-900 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
          >
            <LuCreditCard className="w-5 h-5" />
            {isProcessing ? "Processing..." : "I've Completed the Transfer"}
          </button>
          <p className="text-sm text-gray-600 text-center">
            Click after you've made the payment through your bank
          </p>
        </div>
      )}
    </Card>
  );
};
