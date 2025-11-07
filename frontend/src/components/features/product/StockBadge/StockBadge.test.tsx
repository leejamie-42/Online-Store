import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { StockBadge } from './StockBadge';

describe('StockBadge', () => {
  it('renders "In Stock" badge when stock > 10', () => {
    render(<StockBadge stock={25} />);

    const badge = screen.getByText('In Stock');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('bg-green-100', 'text-green-800');
  });

  it('renders "Low Stock" badge when stock between 1-10', () => {
    render(<StockBadge stock={5} />);

    const badge = screen.getByText('Low Stock');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('bg-yellow-100', 'text-yellow-800');
  });

  it('renders "Out of Stock" badge when stock is 0', () => {
    render(<StockBadge stock={0} />);

    const badge = screen.getByText('Out of Stock');
    expect(badge).toBeInTheDocument();
    expect(badge).toHaveClass('bg-red-100', 'text-red-800');
  });

  it('renders exact stock count 1-10 as "Low Stock (5)"', () => {
    render(<StockBadge stock={5} showCount />);

    expect(screen.getByText('Low Stock (5)')).toBeInTheDocument();
  });

  it('does not show count when showCount is false', () => {
    render(<StockBadge stock={5} showCount={false} />);

    expect(screen.getByText('Low Stock')).toBeInTheDocument();
    expect(screen.queryByText(/\(\d+\)/)).not.toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(
      <StockBadge stock={10} className="custom-class" />
    );

    expect(container.firstChild).toHaveClass('custom-class');
  });

  it('handles edge case stock = 10 as "Low Stock"', () => {
    render(<StockBadge stock={10} />);

    expect(screen.getByText('Low Stock')).toBeInTheDocument();
  });
});
