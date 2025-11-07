/**
 * OrderListEmpty Component Tests
 */

import { render, screen, fireEvent } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import { OrderListEmpty } from "./OrderListEmpty";

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>);
};

describe("OrderListEmpty", () => {
  it("renders empty state message", () => {
    renderWithRouter(<OrderListEmpty />);

    expect(screen.getByText("No orders yet")).toBeInTheDocument();
  });

  it("renders description text", () => {
    renderWithRouter(<OrderListEmpty />);

    expect(
      screen.getByText(/You haven't placed any orders yet/i)
    ).toBeInTheDocument();
  });

  it("shows Start Shopping button when not filtered", () => {
    renderWithRouter(<OrderListEmpty />);

    const button = screen.getByRole("button", { name: /start shopping/i });
    expect(button).toBeInTheDocument();
  });

  it("shows filtered message when isFiltered is true", () => {
    renderWithRouter(<OrderListEmpty isFiltered={true} />);

    expect(screen.getByText("No orders found")).toBeInTheDocument();
    expect(
      screen.getByText(/Try adjusting your search or filter criteria/i)
    ).toBeInTheDocument();
  });

  it("does not show Start Shopping button when filtered", () => {
    renderWithRouter(<OrderListEmpty isFiltered={true} />);

    const button = screen.queryByRole("button", { name: /start shopping/i });
    expect(button).not.toBeInTheDocument();
  });

  it("displays package icon", () => {
    renderWithRouter(<OrderListEmpty />);

    expect(screen.getByText("📦")).toBeInTheDocument();
  });

  it("applies custom className", () => {
    const { container } = renderWithRouter(
      <OrderListEmpty className="custom-class" />
    );

    const emptyState = container.querySelector(".custom-class");
    expect(emptyState).toBeInTheDocument();
  });

  it("has centered layout", () => {
    const { container } = renderWithRouter(<OrderListEmpty />);

    const emptyState = container.querySelector(".flex.flex-col.items-center");
    expect(emptyState).toBeInTheDocument();
  });
});
