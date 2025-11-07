/**
 * CheckoutDetails Page
 * Step 2 of 4-step checkout flow: Collect shipping information and payment method
 * Based on Figma design: Customer Details Page
 */

import React, { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Button } from "@/components/ui/Button/Button";
import { StepIndicator } from "@/components/features/checkout/StepIndicator/StepIndicator";
import { ContactInformation } from "@/components/features/checkout/ContactInformation/ContactInformation";
import { ShippingAddress } from "@/components/features/checkout/ShippingAddress/ShippingAddress";
import { PaymentMethodSelector } from "@/components/features/checkout/PaymentMethodSelector/PaymentMethodSelector";
import { CheckoutOrderSummary } from "@/components/features/checkout/CheckoutOrderSummary/CheckoutOrderSummary";
import { useCartStore } from "@/stores/cart.store";
import { useCheckout } from "@/hooks/useCheckout";
import { calculateCartSummary } from "@/utils/order.utils";
import { CHECKOUT_STEPS } from "@/config/checkout.constants";
import { ROUTES } from "@/config/routes";
import {
  checkoutDetailsSchema,
  type CheckoutDetailsFormData,
} from "@/schemas/checkout.schema";

/**
 * CheckoutDetails Page Component
 * Collects customer contact info, shipping address, and payment method
 */
export const CheckoutDetails: React.FC = () => {
  const navigate = useNavigate();
  const { items, setShippingInfo } = useCartStore();
  const { proceedToPayment, isProcessing } = useCheckout();

  // Calculate pricing summary
  const summary = calculateCartSummary(items);

  // React Hook Form setup with Zod validation
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CheckoutDetailsFormData>({
    resolver: zodResolver(checkoutDetailsSchema),
    mode: "onBlur", //ate on blur for better UX
    defaultValues: {
      country: "Australia",
    },
  });
  // Handle form submission
  const onSubmit = async (data: CheckoutDetailsFormData) => {
    try {
      // Store shipping information in cart store
      setShippingInfo({
        firstName: data.first_name,
        lastName: data.last_name,
        email: data.email,
        mobileNumber: data.mobile_number,
        addressLine1: data.address_line1,
        city: data.city,
        state: data.state,
        postcode: data.postcode,
        country: data.country || "Australia",
      });

      // Create order and payment, then navigate to payment page
      proceedToPayment();
    } catch (error) {
      console.error("Error processing checkout:", error);
      // Error is handled by React Query mutations
    }
  };

  // Handle back to review
  const handleBack = () => {
    navigate("/checkout/review");
  };

  // Redirect if cart is empty
  useEffect(() => {
    if (items.length === 0) {
      navigate(ROUTES.HOME);
    }
  }, [items.length, navigate]);

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4 max-w-7xl">
        {/* Step Indicator */}
        <StepIndicator
          currentStep={2}
          steps={CHECKOUT_STEPS}
          completedSteps={[1]}
        />

        {/* Main Content: Two-column layout */}
        <form onSubmit={handleSubmit(onSubmit)}>
          <div className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column: Form Sections (2/3 width on desktop) */}
            <div className="lg:col-span-2 space-y-6">
              {/* Contact Information */}
              <ContactInformation
                register={register}
                errors={errors}
                disabled={isSubmitting}
              />

              {/* Shipping Address */}
              <ShippingAddress
                register={register}
                errors={errors}
                disabled={isSubmitting}
              />

              {/* Payment Method */}
              <PaymentMethodSelector
                register={register}
                disabled={isSubmitting}
              />

              {/* Action Buttons */}
              <div className="flex flex-col sm:flex-row gap-4">
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleBack}
                  disabled={isSubmitting}
                  className="sm:w-auto w-full"
                >
                  ← Back to Review
                </Button>
                <Button
                  type="submit"
                  variant="primary"
                  disabled={isSubmitting || isProcessing || items.length === 0}
                  className="sm:flex-1 w-full"
                >
                  {isProcessing
                    ? "Creating order..."
                    : isSubmitting
                      ? "Validating..."
                      : "Proceed to Payment →"}
                </Button>
              </div>
            </div>

            {/* Right Column: Order Summary (1/3 width on desktop) */}
            <div className="lg:col-span-1">
              <CheckoutOrderSummary summary={summary} />
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};
