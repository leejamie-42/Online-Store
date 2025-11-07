import React from "react";
import {
  LuShoppingCart,
  LuPackage,
  LuPackageCheck,
  LuTruck,
  LuCircleCheck,
  LuMail,
} from "react-icons/lu";
import type { OrderStatus } from "@/types";
import type { IconType } from "react-icons/lib";

export interface OrderStatusStepperProps {
  currentStatus: OrderStatus;
  className?: string;
}

type OrderStep = {
  id: OrderStatus;
  label: string;
  icon: IconType;
};

const ORDER_STEPS: OrderStep[] = [
  { id: "pending", label: "Order Placed", icon: LuShoppingCart },
  { id: "processing", label: "Processing", icon: LuPackage },
  { id: "picked_up", label: "Picked Up", icon: LuPackageCheck },
  { id: "delivering", label: "In Transit", icon: LuTruck },
  { id: "delivered", label: "Delivered", icon: LuCircleCheck },
];

export const OrderStatusStepper: React.FC<OrderStatusStepperProps> = ({
  currentStatus,
  className = "",
}) => {
  // Get current step index
  const currentStepIndex = ORDER_STEPS.findIndex(
    (step) => step.id === currentStatus
  );

  // Check if step is completed
  const isStepCompleted = (stepIndex: number): boolean => {
    return stepIndex <= currentStepIndex;
  };

  // Status badge configuration
  const STATUS_BADGE_CONFIG: Record<
    OrderStatus,
    { label: string; className: string }
  > = {
    pending: {
      label: "Order Pending",
      className: "bg-orange-100 text-orange-800",
    },
    processing: {
      label: "Processing",
      className: "bg-yellow-100 text-yellow-800",
    },
    picked_up: {
      label: "Picked Up",
      className: "bg-blue-100 text-blue-800",
    },
    delivering: {
      label: "In Transit",
      className: "bg-blue-100 text-blue-800",
    },
    delivered: {
      label: "Delivered",
      className: "bg-green-100 text-green-800",
    },
    cancelled: {
      label: "Cancelled",
      className: "bg-red-100 text-red-800",
    },
    refunded: {
      label: "Refunded",
      className: "bg-green-100 text-green-800",
    },
  };

  // Get status badge for current status
  const getStatusBadge = () => {
    const config = STATUS_BADGE_CONFIG[currentStatus];
    if (!config) return null;

    return (
      <span
        className={`inline-flex items-center px-3 py-1 rounded-md text-sm font-medium ${config.className}`}
      >
        {config.label}
      </span>
    );
  };

  return (
    <div className={`bg-white ${className}`}>
      {/* Header with title and status badge */}
      <div className="flex items-center justify-between mb-8">
        <h2 className="text-xl font-semibold text-gray-900">Order Status</h2>
        {getStatusBadge()}
      </div>

      {/* Stepper */}
      <div className="relative">
        {/* Steps container */}
        <div className="flex items-center justify-between">
          {ORDER_STEPS.map((step, index) => {
            const isCompleted = isStepCompleted(index);
            const isLast = index === ORDER_STEPS.length - 1;
            const StepIcon = step.icon;

            return (
              <div key={step.id} className="flex-1 relative">
                <div className="flex flex-col items-center">
                  {/* Step circle */}
                  <div
                    className={`
                      relative z-10 flex items-center justify-center w-10 h-10 sm:w-12 sm:h-12 rounded-full border-2
                      ${
                        isCompleted
                          ? "bg-blue-600 border-blue-600"
                          : "bg-white border-gray-300"
                      }
                    `}
                  >
                    <StepIcon
                      className={`w-4 h-4 sm:w-5 sm:h-5 ${
                        isCompleted ? "text-white" : "text-gray-400"
                      }`}
                    />
                  </div>

                  {/* Step label */}
                  <div className="mt-2 sm:mt-3 text-center px-1">
                    <p
                      className={`text-xs sm:text-sm font-medium ${
                        isCompleted ? "text-gray-900" : "text-gray-500"
                      }`}
                    >
                      {step.label}
                    </p>
                  </div>
                </div>

                {/* Connecting line */}
                {!isLast && (
                  <div
                    className="absolute top-5 sm:top-6 left-1/2 w-full h-0.5 -z-0"
                    style={{ transform: "translateY(-50%)" }}
                  >
                    <div
                      className={`h-full ${
                        isStepCompleted(index + 1)
                          ? "bg-blue-600"
                          : "bg-gray-300"
                      }`}
                    />
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {/* Email notification banner */}
      <div className="mt-6 flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
        <LuMail className="text-blue-600 w-5 h-5 flex-shrink-0 mt-0.5" />
        <p className="text-sm text-gray-700">
          Email notifications are being sent for each status update
        </p>
      </div>
    </div>
  );
};
