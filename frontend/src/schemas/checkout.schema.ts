import { z } from "zod";

/**
 * Australian States and Territories
 */
const AUSTRALIAN_STATES = [
  "NSW",
  "VIC",
  "QLD",
  "SA",
  "WA",
  "TAS",
  "NT",
  "ACT",
] as const;

/**
 * Shipping Information Schema
 * Validates customer contact and delivery address information
 * Matches backend API specification from SYSTEM_INTERFACE_SPEC.md
 * POST /api/orders → shipping_info fields
 */
export const shippingSchema = z.object({
  first_name: z
    .string()
    .min(1, "First name is required")
    .min(2, "First name must be at least 2 characters")
    .max(50, "First name must be less than 50 characters")
    .regex(
      /^[a-zA-Z\u00C0-\u017F\s'-]+$/,
      "First name can only contain letters, spaces, hyphens, and apostrophes",
    ),

  last_name: z
    .string()
    .min(1, "Last name is required")
    .min(2, "Last name must be at least 2 characters")
    .max(50, "Last name must be less than 50 characters")
    .regex(
      /^[a-zA-Z\u00C0-\u017F\s'-]+$/,
      "Last name can only contain letters, spaces, hyphens, and apostrophes",
    ),

  email: z
    .string()
    .min(1, "Email is required")
    .email("Please enter a valid email address"),

  mobile_number: z
    .string()
    .min(1, "Mobile number is required")
    .regex(/^[+\d\s\-()]+$/, "Please enter a valid mobile number")
    .min(8, "Mobile number must be at least 8 digits")
    .max(20, "Mobile number is too long"),

  address_line1: z
    .string()
    .min(1, "Street address is required")
    .min(5, "Please enter a complete street address")
    .max(200, "Address must be less than 200 characters"),

  city: z
    .string()
    .min(1, "City is required")
    .min(2, "City name must be at least 2 characters")
    .max(100, "City name must be less than 100 characters")
    .regex(
      /^[a-zA-Z\s'-]+$/,
      "City name can only contain letters, spaces, hyphens, and apostrophes",
    ),

  state: z
    .string()
    .min(1, "State/Territory is required")
    .refine(
      (val) =>
        AUSTRALIAN_STATES.includes(val as (typeof AUSTRALIAN_STATES)[number]),
      {
        message: "Please select a valid Australian state or territory",
      },
    ),

  postcode: z
    .string()
    .min(1, "Postcode is required")
    .regex(/^\d{4}$/, "Postcode must be exactly 4 digits"),

  country: z.string().min(1, "Country is required"),
});

/**
 * Payment Method Schema
 * Currently only BPAY is supported
 */
export const paymentMethodSchema = z.object({
  payment_method: z.literal("BPAY", {
    message: "Only BPAY is currently supported",
  }),
});

/**
 * Complete Checkout Details Schema
 * Combines shipping and payment information
 */
export const checkoutDetailsSchema = z.object({
  ...shippingSchema.shape,
  ...paymentMethodSchema.shape,
});

/**
 * TypeScript type inference from Zod schemas
 */
export type ShippingFormData = z.infer<typeof shippingSchema>;
export type PaymentMethodFormData = z.infer<typeof paymentMethodSchema>;
export type CheckoutDetailsFormData = z.infer<typeof checkoutDetailsSchema>;

/**
 * Helper: Australian States for dropdowns
 */
export const australianStates = AUSTRALIAN_STATES.map((code) => ({
  value: code,
  label: getStateName(code),
}));

/**
 * Helper: Get full state name from abbreviation
 */
function getStateName(code: string): string {
  const stateNames: Record<string, string> = {
    NSW: "New South Wales",
    VIC: "Victoria",
    QLD: "Queensland",
    SA: "South Australia",
    WA: "Western Australia",
    TAS: "Tasmania",
    NT: "Northern Territory",
    ACT: "Australian Capital Territory",
  };
  return stateNames[code] || code;
}
