import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductFeatures } from './ProductFeatures';

describe('ProductFeatures', () => {
  const features = [
    'Secure payment processing',
    'Free shipping on orders over $100',
    '30-day return policy',
  ];

  it('renders all features', () => {
    render(<ProductFeatures features={features} />);

    features.forEach(feature => {
      expect(screen.getByText(feature)).toBeInTheDocument();
    });
  });

  it('renders checkmarks for each feature', () => {
    const { container } = render(<ProductFeatures features={features} />);

    const checkmarks = container.querySelectorAll('.checkmark');
    expect(checkmarks).toHaveLength(3);
  });

  it('applies correct styling', () => {
    const { container } = render(<ProductFeatures features={features} />);

    const wrapper = container.firstChild;
    expect(wrapper).toHaveClass('bg-blue-50');
    expect(wrapper).toHaveClass('rounded-[10px]');
  });

  it('handles empty features array', () => {
    const { container } = render(<ProductFeatures features={[]} />);

    expect(container.firstChild).toBeNull();
  });
});
