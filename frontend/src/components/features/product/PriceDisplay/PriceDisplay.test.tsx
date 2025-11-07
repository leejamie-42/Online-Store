import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { PriceDisplay } from './PriceDisplay';

describe('PriceDisplay', () => {
  it('renders price with default currency (USD)', () => {
    render(<PriceDisplay price={49.99} />);

    expect(screen.getByText('$49.99')).toBeInTheDocument();
  });

  it('renders price with custom currency', () => {
    render(<PriceDisplay price={49.99} currency="EUR" />);

    expect(screen.getByText(/49.99/)).toBeInTheDocument();
  });

  it('handles whole numbers correctly', () => {
    render(<PriceDisplay price={50} />);

    expect(screen.getByText('$50.00')).toBeInTheDocument();
  });

  it('applies custom className', () => {
    const { container } = render(
      <PriceDisplay price={49.99} className="text-red-500" />
    );

    expect(container.firstChild).toHaveClass('text-red-500');
  });

  it('formats large numbers correctly', () => {
    render(<PriceDisplay price={1299.99} />);

    expect(screen.getByText('$1,299.99')).toBeInTheDocument();
  });
});
