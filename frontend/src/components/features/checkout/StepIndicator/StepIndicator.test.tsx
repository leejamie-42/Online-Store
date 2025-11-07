/**
 * StepIndicator Component Tests
 */

import { describe, it, expect } from "vitest";
import { render, screen } from "@testing-library/react";
import { StepIndicator } from "./StepIndicator";
import { CHECKOUT_STEPS } from "@/config/checkout.constants";

describe("StepIndicator", () => {
  it("renders all steps", () => {
    render(<StepIndicator currentStep={1} steps={CHECKOUT_STEPS} />);

    // Component renders both desktop and mobile versions, so use getAllByText
    expect(screen.getAllByText("Review").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Details").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Payment").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Confirmation").length).toBeGreaterThan(0);
  });

  it("highlights current step", () => {
    render(<StepIndicator currentStep={2} steps={CHECKOUT_STEPS} />);

    // Check that the Details step is marked as primary color
    const detailsSteps = screen.getAllByText("Details");
    const hasHighlightedStep = detailsSteps.some((el) =>
      el.className.includes("text-primary-600"),
    );
    expect(hasHighlightedStep).toBe(true);
  });

  it("shows correct step numbers", () => {
    render(<StepIndicator currentStep={1} steps={CHECKOUT_STEPS} />);

    // Component renders both desktop and mobile versions
    expect(screen.getAllByText("1").length).toBeGreaterThan(0);
    expect(screen.getAllByText("2").length).toBeGreaterThan(0);
    expect(screen.getAllByText("3").length).toBeGreaterThan(0);
    expect(screen.getAllByText("4").length).toBeGreaterThan(0);
  });

  it("has aria-current on active step", () => {
    const { container } = render(
      <StepIndicator currentStep={1} steps={CHECKOUT_STEPS} />,
    );

    const activeSteps = container.querySelectorAll('[aria-current="step"]');
    expect(activeSteps.length).toBeGreaterThan(0);
  });

  it("renders screen reader announcement", () => {
    render(<StepIndicator currentStep={2} steps={CHECKOUT_STEPS} />);

    expect(screen.getByText(/Step 2 of 4: Details/i)).toBeInTheDocument();
  });
});
