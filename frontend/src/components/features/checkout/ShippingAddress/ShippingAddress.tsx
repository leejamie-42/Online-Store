/**
 * ShippingAddress Component
 * Form section for collecting delivery address details
 * Based on Figma design: Customer Details Page - Shipping Address card
 */

import React from "react";
import { type UseFormRegister, type FieldErrors } from "react-hook-form";
import { Card } from "@/components/ui/Card/Card";
import { Input } from "@/components/ui/Input/Input";
import { Textarea } from "@/components/ui/Textarea/Textarea";
import { Select } from "@/components/ui/Select/Select";
import {
  australianStates,
  type CheckoutDetailsFormData,
} from "@/schemas/checkout.schema";

export interface ShippingAddressProps {
  register: UseFormRegister<CheckoutDetailsFormData>;
  errors: FieldErrors<CheckoutDetailsFormData>;
  disabled?: boolean;
}

/**
 * ShippingAddress Component
 * Collects delivery address: street, city, state, postcode
 *
 * @param register - React Hook Form register function
 * @param errors - Form validation errors
 * @param disabled - Disable all inputs (during submission)
 */
export const ShippingAddress: React.FC<ShippingAddressProps> = ({
  register,
  errors,
  disabled = false,
}) => {
  return (
    <Card padding="lg">
      <h2 className="text-xl font-semibold text-gray-900 mb-6">
        Shipping Address
      </h2>

      <div className="space-y-4">
        {/* Street Address */}
        <Textarea
          {...register("address_line1")}
          label="Street Address"
          placeholder="123 Main Street, Unit 4"
          rows={3}
          error={errors.address_line1?.message}
          disabled={disabled}
          required
          fullWidth
          variant="filled"
          aria-invalid={errors.address_line1 ? "true" : "false"}
          aria-describedby={
            errors.address_line1 ? "address_line1-error" : undefined
          }
        />

        {/* City, State, Postcode Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* City */}
          <div className="md:col-span-1">
            <Input
              {...register("city")}
              label="City"
              type="text"
              placeholder="Sydney"
              error={errors.city?.message}
              disabled={disabled}
              required
              fullWidth
              variant="filled"
              aria-invalid={errors.city ? "true" : "false"}
              aria-describedby={errors.city ? "receiver_city-error" : undefined}
            />
          </div>

          {/* State */}
          <div className="md:col-span-1">
            <Select
              {...register("state")}
              label="State"
              options={australianStates}
              placeholder="Select state"
              error={errors.state?.message}
              disabled={disabled}
              required
              fullWidth
              variant="filled"
              aria-invalid={errors.state ? "true" : "false"}
              aria-describedby={errors.state ? "state-error" : undefined}
            />
          </div>

          {/* Postcode */}
          <div className="md:col-span-1">
            <Input
              {...register("postcode")}
              label="Postcode"
              type="text"
              placeholder="2000"
              maxLength={4}
              error={errors.postcode?.message}
              disabled={disabled}
              required
              fullWidth
              variant="filled"
              aria-invalid={errors.postcode ? "true" : "false"}
              aria-describedby={errors.postcode ? "postcode-error" : undefined}
            />
          </div>
        </div>

        {/* Country (hidden field, defaults to Australia) */}
        <input type="hidden" {...register("country")} value="Australia" />

        {/* Delivery Estimate */}
        <p className="text-sm text-gray-600">
          Estimated delivery: 3-5 business days
        </p>
      </div>
    </Card>
  );
};
