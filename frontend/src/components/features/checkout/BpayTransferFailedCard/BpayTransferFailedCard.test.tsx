/**
 * BpayTransferFailedCard Component Tests
 */

import { beforeEach, describe, expect, it, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { BpayTransferFailedCard } from "./BpayTransferFailedCard";

const mockOnRetry = vi.fn();
const mockOnUpdateDetails = vi.fn();
const mockOnCancel = vi.fn();

describe("BpayTransferFailedCard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders error icon and title", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    expect(screen.getByText("BPAY Transfer Failed")).toBeInTheDocument();
  });

  it("renders error message", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    expect(
      screen.getByText(
        "We couldn't complete your BPAY transfer. Please try again."
      )
    ).toBeInTheDocument();
  });

  it("renders common reasons for failure", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    expect(
      screen.getByText("Common reasons for BPAY failure:")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Insufficient funds in your account")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Bank declined the transaction")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Connection timeout with bank")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Invalid BPAY reference number")
    ).toBeInTheDocument();
    expect(
      screen.getByText("Daily transfer limit reached")
    ).toBeInTheDocument();
  });

  it("renders all action buttons", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    expect(
      screen.getByRole("button", { name: /Retry Payment/i })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Update Details/i })
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Cancel & Return Home/i })
    ).toBeInTheDocument();
  });

  it("calls onRetry when Retry Payment button is clicked", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    const retryButton = screen.getByRole("button", { name: /Retry Payment/i });
    fireEvent.click(retryButton);

    expect(mockOnRetry).toHaveBeenCalledTimes(1);
  });

  it("calls onUpdateDetails when Update Details button is clicked", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    const updateButton = screen.getByRole("button", {
      name: /Update Details/i,
    });
    fireEvent.click(updateButton);

    expect(mockOnUpdateDetails).toHaveBeenCalledTimes(1);
  });

  it("calls onCancel when Cancel & Return Home button is clicked", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    const cancelButton = screen.getByRole("button", {
      name: /Cancel & Return Home/i,
    });
    fireEvent.click(cancelButton);

    expect(mockOnCancel).toHaveBeenCalledTimes(1);
  });

  it("shows loading state when isRetrying is true", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
        isRetrying={true}
      />
    );

    expect(screen.getByText("Retrying...")).toBeInTheDocument();
  });

  it("disables all buttons when isRetrying is true", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
        isRetrying={true}
      />
    );

    const buttons = screen.getAllByRole("button");
    buttons.forEach((button) => {
      expect(button).toBeDisabled();
    });
  });

  it("enables all buttons when isRetrying is false", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
        isRetrying={false}
      />
    );

    const buttons = screen.getAllByRole("button");
    buttons.forEach((button) => {
      expect(button).not.toBeDisabled();
    });
  });

  it("applies custom className", () => {
    const { container } = render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
        className="custom-test-class"
      />
    );

    const card = container.querySelector(".custom-test-class");
    expect(card).toBeInTheDocument();
  });

  it("renders Retry Payment button as primary by default", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    const retryButton = screen.getByRole("button", { name: /Retry Payment/i });
    // Primary button should not have 'outline' variant class
    expect(retryButton).not.toHaveClass("outline");
  });

  it("renders Update Details button as outline variant", () => {
    render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    const updateButton = screen.getByRole("button", {
      name: /Update Details/i,
    });
    // Check that it's an outline variant button
    expect(updateButton.className).toContain("outline");
  });

  it("renders error state with red color scheme", () => {
    const { container } = render(
      <BpayTransferFailedCard
        onRetry={mockOnRetry}
        onUpdateDetails={mockOnUpdateDetails}
        onCancel={mockOnCancel}
      />
    );

    // Check for red color classes in title
    const title = screen.getByText("BPAY Transfer Failed");
    expect(title.className).toContain("text-red-600");

    // Check for red-themed reasons box
    const reasonsBox = container.querySelector(".bg-red-50");
    expect(reasonsBox).toBeInTheDocument();
  });
});
