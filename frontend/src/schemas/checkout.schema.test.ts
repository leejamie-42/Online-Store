/**
 * Checkout Schema Tests
 * Validates shipping and payment form validation logic
 */

import { describe, it, expect } from "vitest";
import {
  shippingSchema,
  paymentMethodSchema,
  checkoutDetailsSchema,
} from "./checkout.schema";

describe("Checkout Schema Validation", () => {
  describe("shippingSchema", () => {
    describe("first_name and last_name validation", () => {
      it("should accept valid names", () => {
        const validFirstNames = ["John", "Mary-Jane", "José", "Li"];
        const validLastNames = ["Smith", "O'Connor", "García", "Wei"];

        validFirstNames.forEach((firstName, index) => {
          const result = shippingSchema.safeParse({
            first_name: firstName,
            last_name: validLastNames[index],
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid names", () => {
        const invalidNames = ["", "A", "Name123", "Name@Test"];

        invalidNames.forEach((name) => {
          const result = shippingSchema.safeParse({
            first_name: name,
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });

    describe("email validation", () => {
      it("should accept valid email addresses", () => {
        const validEmails = [
          "user@example.com",
          "test.user@company.com.au",
          "admin+tag@domain.co",
        ];

        validEmails.forEach((email) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: email,
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid email addresses", () => {
        const invalidEmails = ["", "notanemail", "@example.com", "user@"];

        invalidEmails.forEach((email) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: email,
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });

    describe("mobile_number validation", () => {
      it("should accept valid Australian phone numbers", () => {
        const validPhones = [
          "0412345678",
          "0412 345 678",
          "0412-345-678",
          "02 9876 5432",
          "+61 2 9876 5432",
          "+61412345678",
        ];

        validPhones.forEach((phone) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: phone,
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid phone numbers", () => {
        const invalidPhones = ["", "123", "abcdefgh", "12-34"];

        invalidPhones.forEach((phone) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: phone,
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });

    describe("address_line1 validation", () => {
      it("should accept valid addresses", () => {
        const result = shippingSchema.safeParse({
          first_name: "John",
          last_name: "Smith",
          email: "test@example.com",
          mobile_number: "0412345678",
          address_line1: "123 Test Street, Unit 4",
          city: "Sydney",
          state: "NSW",
          postcode: "2000",
          country: "Australia",
        });
        expect(result.success).toBe(true);
      });

      it("should reject too short addresses", () => {
        const result = shippingSchema.safeParse({
          first_name: "John",
          last_name: "Smith",
          email: "test@example.com",
          mobile_number: "0412345678",
          address_line1: "123",
          city: "Sydney",
          state: "NSW",
          postcode: "2000",
          country: "Australia",
        });
        expect(result.success).toBe(false);
      });
    });

    describe("city validation", () => {
      it("should accept valid city names", () => {
        const validCities = ["Sydney", "Melbourne", "Brisbane", "Port Douglas"];

        validCities.forEach((city) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: city,
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid city names", () => {
        const invalidCities = ["", "A", "City123"];

        invalidCities.forEach((city) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: city,
            state: "NSW",
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });

    describe("state validation", () => {
      it("should accept valid Australian states", () => {
        const validStates = [
          "NSW",
          "VIC",
          "QLD",
          "SA",
          "WA",
          "TAS",
          "NT",
          "ACT",
        ];

        validStates.forEach((state) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: state,
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid states", () => {
        const invalidStates = ["", "XX", "nsw", "California", "N"];

        invalidStates.forEach((state) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: state,
            postcode: "2000",
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });

    describe("postcode validation", () => {
      it("should accept valid 4-digit postcodes", () => {
        const validPostcodes = ["2000", "3000", "4000", "5000"];

        validPostcodes.forEach((postcode) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: postcode,
            country: "Australia",
          });
          expect(result.success).toBe(true);
        });
      });

      it("should reject invalid postcodes", () => {
        const invalidPostcodes = ["", "200", "20000", "ABCD"];

        invalidPostcodes.forEach((postcode) => {
          const result = shippingSchema.safeParse({
            first_name: "John",
            last_name: "Smith",
            email: "test@example.com",
            mobile_number: "0412345678",
            address_line1: "123 Test St",
            city: "Sydney",
            state: "NSW",
            postcode: postcode,
            country: "Australia",
          });
          expect(result.success).toBe(false);
        });
      });
    });
  });

  describe("paymentMethodSchema", () => {
    it("should accept BPAY as payment method", () => {
      const result = paymentMethodSchema.safeParse({
        payment_method: "BPAY",
      });
      expect(result.success).toBe(true);
    });

    it("should reject non-BPAY payment methods", () => {
      const invalidMethods = ["", "Credit Card", "PayPal", "Cash"];

      invalidMethods.forEach((method) => {
        const result = paymentMethodSchema.safeParse({
          payment_method: method,
        });
        expect(result.success).toBe(false);
      });
    });
  });

  describe("checkoutDetailsSchema", () => {
    it("should accept valid complete checkout details", () => {
      const validData = {
        first_name: "John",
        last_name: "Smith",
        email: "john@example.com",
        mobile_number: "0412345678",
        address_line1: "123 Test Street",
        city: "Sydney",
        state: "NSW",
        postcode: "2000",
        country: "Australia",
        payment_method: "BPAY",
      };

      const result = checkoutDetailsSchema.safeParse(validData);
      expect(result.success).toBe(true);
    });

    it("should reject incomplete checkout details", () => {
      const incompleteData = {
        first_name: "John",
        last_name: "Smith",
        email: "john@example.com",
        // Missing other required fields
      };

      const result = checkoutDetailsSchema.safeParse(incompleteData);
      expect(result.success).toBe(false);
    });

    it("should provide detailed error messages for multiple validation failures", () => {
      const invalidData = {
        first_name: "",
        last_name: "",
        email: "invalid-email",
        mobile_number: "123",
        address_line1: "",
        city: "",
        state: "XX",
        postcode: "200",
        country: "",
        payment_method: "Cash",
      };

      const result = checkoutDetailsSchema.safeParse(invalidData);
      expect(result.success).toBe(false);
      if (!result.success) {
        expect(result.error.issues.length).toBeGreaterThan(0);
      }
    });
  });
});
