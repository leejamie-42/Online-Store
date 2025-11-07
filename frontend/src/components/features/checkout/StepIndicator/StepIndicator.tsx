/**
 * StepIndicator Component
 * Displays the current progress through the 4-step checkout flow
 * Based on Figma design: Checkout step progress indicator
 */

import React from "react";
import { LuCheck } from "react-icons/lu";
import type { CheckoutStep } from "@/config/checkout.constants";

export interface StepIndicatorProps {
  currentStep: 1 | 2 | 3 | 4;
  steps: CheckoutStep[];
  completedSteps?: number[]; // Array of completed step numbers
  className?: string;
}

/**
 * StepIndicator Component
 * Visual indicator showing user's progress through checkout steps
 *
 * @param currentStep - Current active step (1-4)
 * @param steps - Array of checkout step configurations
 * @param className - Optional additional CSS classes
 */
export const StepIndicator: React.FC<StepIndicatorProps> = ({
  currentStep,
  steps,
  completedSteps = [],
  className = "",
}) => {
  return (
    <div className={`w-full ${className}`}>
      {/* Desktop: Horizontal layout */}
      <div className="hidden sm:block">
        <div className="flex items-center justify-between">
          {steps.map((step, index) => {
            const isActive = step.number === currentStep;
            const isCompleted = completedSteps.includes(step.number);
            const isUpcoming = step.number > currentStep && !isCompleted;

            return (
              <React.Fragment key={step.number}>
                {/* Step indicator */}
                <div className="flex flex-col items-center flex-1">
                  {/* Circle with number or checkmark */}
                  <div
                    className={`
                      w-10 h-10 rounded-full flex items-center justify-center
                      font-semibold text-sm transition-colors
                      ${isActive ? "bg-primary-600 text-white" : ""}
                      ${isCompleted ? "bg-green-500 text-white" : ""}
                      ${isUpcoming ? "bg-gray-200 text-gray-500" : ""}
                    `}
                    aria-current={isActive ? "step" : undefined}
                  >
                    {isCompleted ? (
                      <LuCheck className="w-5 h-5" />
                    ) : (
                      step.number
                    )}
                  </div>

                  {/* Step label and description */}
                  <div className="mt-2 text-center">
                    <div
                      className={`
                        text-sm font-medium
                        ${isActive ? "text-primary-600" : ""}
                        ${isCompleted ? "text-gray-900" : ""}
                        ${isUpcoming ? "text-gray-500" : ""}
                      `}
                    >
                      {step.label}
                    </div>
                    <div className="text-xs text-gray-500 mt-1">
                      {step.description}
                    </div>
                  </div>
                </div>

                {/* Connector line (not after last step) */}
                {index < steps.length - 1 && (
                  <div
                    className={`
                      h-0.5 flex-1 mx-2 mt-[-50px]
                      ${completedSteps.includes(step.number) ? "bg-green-500" : step.number < currentStep ? "bg-primary-600" : "bg-gray-200"}
                    `}
                    aria-hidden="true"
                  />
                )}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      {/* Mobile: Vertical layout */}
      <div className="sm:hidden">
        <div className="space-y-4">
          {steps.map((step) => {
            const isActive = step.number === currentStep;
            const isCompleted = step.number < currentStep;
            const isUpcoming = step.number > currentStep;

            return (
              <div key={step.number} className="flex items-start gap-3">
                {/* Circle with number or checkmark */}
                <div
                  className={`
                    w-8 h-8 rounded-full flex items-center justify-center shrink-0
                    font-semibold text-sm transition-colors
                    ${isActive ? "bg-primary-600 text-white" : ""}
                    ${isCompleted ? "bg-green-500 text-white" : ""}
                    ${isUpcoming ? "bg-gray-200 text-gray-500" : ""}
                  `}
                  aria-current={isActive ? "step" : undefined}
                >
                  {isCompleted ? <LuCheck className="w-4 h-4" /> : step.number}
                </div>

                {/* Step label and description */}
                <div className="flex-1">
                  <div
                    className={`
                      text-sm font-medium
                      ${isActive ? "text-primary-600" : ""}
                      ${isCompleted ? "text-gray-900" : ""}
                      ${isUpcoming ? "text-gray-500" : ""}
                    `}
                  >
                    {step.label}
                  </div>
                  <div className="text-xs text-gray-500 mt-0.5">
                    {step.description}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Screen reader announcement */}
      <div className="sr-only" role="status" aria-live="polite">
        Step {currentStep} of {steps.length}: {steps[currentStep - 1]?.label}
      </div>
    </div>
  );
};
