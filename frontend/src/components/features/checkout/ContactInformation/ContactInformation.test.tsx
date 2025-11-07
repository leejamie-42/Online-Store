/**
 * ContactInformation Component Tests
 */

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { useForm } from "react-hook-form";
import { ContactInformation } from "./ContactInformation";
import type { CheckoutDetailsFormData } from "@/schemas/checkout.schema";

// Wrapper component to provide form context
const ContactInformationWrapper = ({
  disabled = false,
}: {
  disabled?: boolean;
}) => {
  const {
    register,
    formState: { errors },
  } = useForm<CheckoutDetailsFormData>();

  return (
    <ContactInformation
      register={register}
      errors={errors}
      disabled={disabled}
    />
  );
};

describe("ContactInformation Component", () => {
  it("should render contact information section title", () => {
    render(<ContactInformationWrapper />);
    expect(screen.getByText("Contact Information")).toBeInTheDocument();
  });

  it("should render all required input fields", () => {
    render(<ContactInformationWrapper />);

    // Check for Last Name input
    expect(screen.getByPlaceholderText("John")).toBeInTheDocument();

    // Check for First Name input
    expect(screen.getByPlaceholderText("Smith")).toBeInTheDocument();

    // Check for Email input
    expect(screen.getByPlaceholderText("john@example.com")).toBeInTheDocument();

    // Check for Phone input
    expect(screen.getByPlaceholderText("+61 4XX XXX XXX")).toBeInTheDocument();
  });

  it("should show required indicators on all fields", () => {
    render(<ContactInformationWrapper />);

    const requiredLabels = screen.getAllByText("*");
    expect(requiredLabels).toHaveLength(4); // 3 required fields
  });

  it("should have correct input types", () => {
    render(<ContactInformationWrapper />);

    const firstnameInput = screen.getByPlaceholderText("John");
    const lastnameInput = screen.getByPlaceholderText("Smith");
    const emailInput = screen.getByPlaceholderText("john@example.com");
    const phoneInput = screen.getByPlaceholderText("+61 4XX XXX XXX");

    expect(firstnameInput).toHaveAttribute("type", "text");
    expect(lastnameInput).toHaveAttribute("type", "text");
    expect(emailInput).toHaveAttribute("type", "email");
    expect(phoneInput).toHaveAttribute("type", "tel");
  });

  it("should have appropriate placeholders", () => {
    render(<ContactInformationWrapper />);

    expect(screen.getByPlaceholderText("John")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Smith")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("john@example.com")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("+61 4XX XXX XXX")).toBeInTheDocument();
  });

  it("should disable all inputs when disabled prop is true", () => {
    render(<ContactInformationWrapper disabled={true} />);

    const firstnameInput = screen.getByPlaceholderText("John");
    const lastnameInput = screen.getByPlaceholderText("Smith");
    const emailInput = screen.getByPlaceholderText("john@example.com");
    const phoneInput = screen.getByPlaceholderText("+61 4XX XXX XXX");

    expect(firstnameInput).toBeDisabled();
    expect(lastnameInput).toBeDisabled();
    expect(emailInput).toBeDisabled();
    expect(phoneInput).toBeDisabled();
  });

  it("should have proper ARIA attributes", () => {
    render(<ContactInformationWrapper />);

    const firstnameInput = screen.getByPlaceholderText("John");
    const lastnameInput = screen.getByPlaceholderText("Smith");
    const emailInput = screen.getByPlaceholderText("john@example.com");
    const phoneInput = screen.getByPlaceholderText("+61 4XX XXX XXX");

    expect(firstnameInput).toHaveAttribute("aria-invalid", "false");
    expect(lastnameInput).toHaveAttribute("aria-invalid", "false");
    expect(emailInput).toHaveAttribute("aria-invalid", "false");
    expect(phoneInput).toHaveAttribute("aria-invalid", "false");
  });
});
