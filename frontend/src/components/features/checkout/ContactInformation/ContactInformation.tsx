/**
 * ContactInformation Component
 * Form section for collecting customer contact details
 * Based on Figma design: Customer Details Page - Contact Information card
 */

import React from "react";
import { type UseFormRegister, type FieldErrors } from "react-hook-form";
import { Card } from "@/components/ui/Card/Card";
import { Input } from "@/components/ui/Input/Input";
import type { CheckoutDetailsFormData } from "@/schemas/checkout.schema";

export interface ContactInformationProps {
  register: UseFormRegister<CheckoutDetailsFormData>;
  errors: FieldErrors<CheckoutDetailsFormData>;
  disabled?: boolean;
}

/**
 * ContactInformation Component
 * Collects customer name, email, and phone number
 *
 * @param register - React Hook Form register function
 * @param errors - Form validation errors
 * @param disabled - Disable all inputs (during submission)
 */
export const ContactInformation: React.FC<ContactInformationProps> = ({
  register,
  errors,
  disabled = false,
}) => {
  return (
    <Card padding="lg">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">
        Contact Information
      </h2>

      <div className="space-y-4">
        {/* Name Fields (side by side) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* First Name */}
          <Input
            {...register("first_name")}
            label="First Name"
            type="text"
            placeholder="John"
            error={errors.first_name?.message}
            disabled={disabled}
            required
            fullWidth
            variant="filled"
            aria-invalid={errors.first_name ? "true" : "false"}
            aria-describedby={
              errors.first_name ? "first_name-error" : undefined
            }
          />

          {/* Last Name */}
          <Input
            {...register("last_name")}
            label="Last Name"
            type="text"
            placeholder="Smith"
            error={errors.last_name?.message}
            disabled={disabled}
            required
            fullWidth
            variant="filled"
            aria-invalid={errors.last_name ? "true" : "false"}
            aria-describedby={errors.last_name ? "last_name-error" : undefined}
          />
        </div>

        {/* Email and Phone (side by side) */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Email */}
          <Input
            {...register("email")}
            label="Email Address"
            type="email"
            placeholder="john@example.com"
            error={errors.email?.message}
            disabled={disabled}
            required
            fullWidth
            variant="filled"
            aria-invalid={errors.email ? "true" : "false"}
            aria-describedby={errors.email ? "email-error" : undefined}
          />

          {/* Mobile Number */}
          <Input
            {...register("mobile_number")}
            label="Mobile Number"
            type="tel"
            placeholder="+61 4XX XXX XXX"
            error={errors.mobile_number?.message}
            disabled={disabled}
            required
            fullWidth
            variant="filled"
            aria-invalid={errors.mobile_number ? "true" : "false"}
            aria-describedby={
              errors.mobile_number ? "mobile_number-error" : undefined
            }
          />
        </div>
      </div>
    </Card>
  );
};
