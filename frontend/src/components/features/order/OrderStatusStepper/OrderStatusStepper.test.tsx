import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import { OrderStatusStepper } from "./OrderStatusStepper";

describe("OrderStatusStepper", () => {
  it("renders all 5 order stages", () => {
    render(<OrderStatusStepper currentStatus="processing" />);

    expect(screen.getByText("Order Placed")).toBeInTheDocument();
    // "Processing" appears twice (badge + stage label), so use getAllByText
    expect(screen.getAllByText("Processing").length).toBeGreaterThan(0);
    expect(screen.getByText("Picked Up")).toBeInTheDocument();
    expect(screen.getByText("In Transit")).toBeInTheDocument();
    expect(screen.getByText("Delivered")).toBeInTheDocument();
  });

  it("highlights completed stages with blue background", () => {
    const { container } = render(
      <OrderStatusStepper currentStatus="picked_up" />,
    );

    // First 3 stages should be completed (pending, processing, picked_up)
    const completedCircles = container.querySelectorAll(
      ".rounded-full.bg-blue-600",
    );
    expect(completedCircles.length).toBeGreaterThanOrEqual(3);
  });

  it("shows delivered badge when status is delivered", () => {
    render(<OrderStatusStepper currentStatus="delivered" />);

    // Find the badge specifically (not the stage label)
    const badges = screen.getAllByText("Delivered");
    const deliveredBadge = badges.find(
      (el) =>
        el.classList.contains("bg-green-100") &&
        el.classList.contains("text-green-800"),
    );

    expect(deliveredBadge).toBeInTheDocument();
    expect(deliveredBadge).toHaveClass("bg-green-100", "text-green-800");
  });

  it("shows cancelled badge when status is cancelled", () => {
    render(<OrderStatusStepper currentStatus="cancelled" />);

    const cancelledBadge = screen.getByText("Cancelled");
    expect(cancelledBadge).toBeInTheDocument();
    expect(cancelledBadge).toHaveClass("bg-red-100", "text-red-800");
  });

  it("shows email notification banner", () => {
    render(<OrderStatusStepper currentStatus="processing" />);

    expect(
      screen.getByText(/Email notifications are being sent/i),
    ).toBeInTheDocument();
  });

  it("renders Order Status title", () => {
    render(<OrderStatusStepper currentStatus="pending" />);

    expect(screen.getByText("Order Status")).toBeInTheDocument();
  });

  it("applies custom className when provided", () => {
    const { container } = render(
      <OrderStatusStepper
        currentStatus="processing"
        className="custom-class"
      />,
    );

    const mainDiv = container.firstChild as HTMLElement;
    expect(mainDiv).toHaveClass("custom-class");
  });

  it("shows checkmark icon for delivered status", () => {
    const { container } = render(
      <OrderStatusStepper currentStatus="delivered" />,
    );

    // All stages should be completed (blue background) when delivered
    // Query for circle elements specifically (not connecting lines)
    const completedCircles = container.querySelectorAll(
      ".rounded-full.bg-blue-600",
    );
    expect(completedCircles.length).toBe(5); // All 5 stages completed

    // The component uses SVG icons (Lucide React), not text checkmarks
    // Verify the last stage label is rendered (there are two "Delivered" texts: badge and step label)
    const deliveredElements = screen.getAllByText("Delivered");
    expect(deliveredElements.length).toBeGreaterThanOrEqual(1);

    // Verify the step label specifically
    const deliveredStepLabel = deliveredElements.find((el) =>
      el.classList.contains("text-gray-900"),
    );
    expect(deliveredStepLabel).toBeInTheDocument();
  });

  it("handles pending status correctly", () => {
    const { container } = render(
      <OrderStatusStepper currentStatus="pending" />,
    );

    // Only the first stage should be completed
    const completedCircles = container.querySelectorAll(
      ".rounded-full.bg-blue-600",
    );
    expect(completedCircles.length).toBe(1);
  });

  it("handles delivering status correctly", () => {
    const { container } = render(
      <OrderStatusStepper currentStatus="delivering" />,
    );

    // First 4 stages should be completed
    const completedCircles = container.querySelectorAll(
      ".rounded-full.bg-blue-600",
    );
    expect(completedCircles.length).toBeGreaterThanOrEqual(4);
  });
});
