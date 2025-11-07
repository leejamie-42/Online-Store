/**
 * BpayProcessingView Component
 * Displays payment processing state with countdown timer and progress bar
 * Based on Figma design: node-id=16-418
 */

import React, { useState, useEffect } from "react";
import { LuCreditCard } from "react-icons/lu";

export interface BpayProcessingViewProps {
  /** Initial time remaining in seconds (default: 10s) */
  maxTime?: number;
  /** Callback when time expires */
  onTimeout?: () => void;
}

/**
 * BpayProcessingView Component
 *
 * Displays a processing state with:
 * - Icon indicator (credit card)
 * - Processing message
 * - Countdown timer
 * - Progress bar
 * - Warning message
 */
export const BpayProcessingView: React.FC<BpayProcessingViewProps> = ({
  maxTime = 10,
  onTimeout,
}) => {
  const [timeRemaining, setTimeRemaining] = useState(maxTime);
  const progress = ((maxTime - timeRemaining) / maxTime) * 100;

  useEffect(() => {
    if (timeRemaining <= 0) {
      onTimeout?.();
      return;
    }

    const timer = setInterval(() => {
      setTimeRemaining((prev) => Math.max(0, prev - 1));
    }, 1000);

    return () => clearInterval(timer);
  }, [timeRemaining, onTimeout]);

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
      <div className="max-w-md mx-auto text-center space-y-6">
        {/* Icon */}
        <div className="flex justify-center">
          <div className="w-16 h-16 bg-orange-100 rounded-xl flex items-center justify-center">
            <LuCreditCard className="w-8 h-8 text-orange-500" />
          </div>
        </div>

        {/* Title */}
        <div>
          <h2 className="text-xl font-semibold text-gray-900 mb-2">
            Processing BPAY Transfer...
          </h2>
          <p className="text-sm text-gray-600">
            Completing your secure bank transfer
          </p>
        </div>

        {/* Status Badge with Countdown */}
        <div className="bg-orange-50 border border-orange-200 rounded-lg p-4">
          <div className="flex items-center justify-center gap-2 mb-2">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-medium bg-orange-500 text-white">
              LIVE
            </span>
            <span className="text-sm font-medium text-orange-700">
              Transfer in progress
            </span>
          </div>
          <div className="text-2xl font-bold text-orange-600">
            {timeRemaining}s
          </div>
          <div className="text-xs text-gray-600 mt-1">Time remaining</div>
        </div>

        {/* Progress Bar */}
        <div className="space-y-2">
          <div className="w-full bg-gray-200 rounded-full h-2 overflow-hidden">
            <div
              className="bg-gradient-to-r from-orange-500 to-orange-400 h-full transition-all duration-1000 ease-linear"
              style={{ width: `${progress}%` }}
            />
          </div>
          <p className="text-xs text-gray-500">{Math.round(progress)}% complete</p>
        </div>

        {/* Warning Message */}
        <div className="pt-4 border-t border-gray-200">
          <p className="text-sm text-gray-700 font-medium">
            Please do not close this window
          </p>
        </div>
      </div>
    </div>
  );
};
