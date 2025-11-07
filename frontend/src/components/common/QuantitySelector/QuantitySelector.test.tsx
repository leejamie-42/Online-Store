import { describe, it, expect, vi } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { QuantitySelector } from "./QuantitySelector";

describe("QuantitySelector", () => {
  it("renders with initial quantity", () => {
    render(<QuantitySelector value={1} onChange={vi.fn()} />);

    expect(screen.getByDisplayValue("1")).toBeInTheDocument();
  });

  it("increments quantity when plus button clicked", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} max={10} />);

    fireEvent.click(screen.getByLabelText("Increase quantity"));
    expect(handleChange).toHaveBeenCalledWith(2);
  });

  it("decrements quantity when minus button clicked", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={2} onChange={handleChange} />);

    fireEvent.click(screen.getByLabelText("Decrease quantity"));
    expect(handleChange).toHaveBeenCalledWith(1);
  });

  it("does not decrement below min value", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} min={1} />);

    const decrementButton = screen.getByLabelText("Decrease quantity");
    expect(decrementButton).toBeDisabled();
  });

  it("does not increment above max value", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={10} onChange={handleChange} max={10} />);

    const incrementButton = screen.getByLabelText("Increase quantity");
    expect(incrementButton).toBeDisabled();
  });

  it("allows direct input of quantity", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} />);

    const input = screen.getByDisplayValue("1");
    fireEvent.change(input, { target: { value: "5" } });

    expect(handleChange).toHaveBeenCalledWith(5);
  });

  it("clamps invalid input to max value", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={1} onChange={handleChange} max={10} />);

    const input = screen.getByDisplayValue("1");
    fireEvent.change(input, { target: { value: "20" } });

    expect(handleChange).toHaveBeenCalledWith(10);
  });

  it("clamps invalid input to min value", () => {
    const handleChange = vi.fn();
    render(<QuantitySelector value={5} onChange={handleChange} min={1} />);

    const input = screen.getByDisplayValue("5");
    fireEvent.change(input, { target: { value: "0" } });

    expect(handleChange).toHaveBeenCalledWith(1);
  });

  it("is keyboard accessible", () => {
    render(<QuantitySelector value={1} onChange={vi.fn()} />);

    const input = screen.getByDisplayValue("1");
    expect(input).toHaveAttribute("type", "number");
    expect(input).toHaveAttribute("aria-label", "Quantity");
  });
});
