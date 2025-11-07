import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import { NeedHelpSection } from "./NeedHelpSection";

describe("NeedHelpSection", () => {
  it("renders Need Help title", () => {
    render(<NeedHelpSection />);

    expect(screen.getByText("Need Help?")).toBeInTheDocument();
  });

  it("renders Contact Support button", () => {
    render(<NeedHelpSection />);

    const button = screen.getByRole("button", { name: /contact support/i });
    expect(button).toBeInTheDocument();
  });

  it("displays availability message", () => {
    render(<NeedHelpSection />);

    expect(
      screen.getByText("Available 24/7 to assist you"),
    ).toBeInTheDocument();
  });

  it("calls custom onContactSupport handler when provided", () => {
    const handleContactSupport = vi.fn();
    render(<NeedHelpSection onContactSupport={handleContactSupport} />);

    const button = screen.getByRole("button", { name: /contact support/i });
    fireEvent.click(button);

    expect(handleContactSupport).toHaveBeenCalledTimes(1);
  });

  it("opens mailto link when no custom handler provided", () => {
    // Mock window.location.href
    const originalLocation = window.location;
    // @ts-expect-error - Mocking window.location for testing
    delete window.location;
    // @ts-expect-error - Mocking window.location for testing
    window.location = { href: "" };

    render(<NeedHelpSection />);

    const button = screen.getByRole("button", { name: /contact support/i });
    fireEvent.click(button);

    expect(window.location.href).toBe("mailto:support@shophub.com");

    // Restore original location
    window.location = originalLocation;
  });

  it("applies custom className when provided", () => {
    const { container } = render(<NeedHelpSection className="custom-class" />);

    const cardElement = container.querySelector(".custom-class");
    expect(cardElement).toBeInTheDocument();
  });

  it("button has outline variant styling", () => {
    render(<NeedHelpSection />);

    const button = screen.getByRole("button", { name: /contact support/i });
    expect(button).toHaveClass("border-2", "border-gray-300");
  });

  it("button is full width", () => {
    render(<NeedHelpSection />);

    const button = screen.getByRole("button", { name: /contact support/i });
    expect(button).toHaveClass("w-full");
  });

  it("availability text is centered", () => {
    render(<NeedHelpSection />);

    const availabilityText = screen.getByText("Available 24/7 to assist you");
    expect(availabilityText).toHaveClass("text-center");
  });

  it("renders within Card component structure", () => {
    const { container } = render(<NeedHelpSection />);

    // Check for Card structure classes
    const cardElement = container.querySelector(".bg-white");
    expect(cardElement).toBeInTheDocument();
  });
});
