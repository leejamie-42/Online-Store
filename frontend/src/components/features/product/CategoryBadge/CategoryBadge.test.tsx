import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CategoryBadge } from './CategoryBadge';

describe('CategoryBadge', () => {
  it('renders category text', () => {
    render(<CategoryBadge category="Accessories" />);

    expect(screen.getByText('Accessories')).toBeInTheDocument();
  });

  it('applies correct styling', () => {
    const { container } = render(<CategoryBadge category="Electronics" />);

    const badge = container.firstChild;
    expect(badge).toHaveClass('bg-[#eceef2]');
    expect(badge).toHaveClass('text-[#030213]');
    expect(badge).toHaveClass('rounded-[8px]');
  });

  it('applies custom className', () => {
    const { container } = render(
      <CategoryBadge category="Test" className="custom-class" />
    );

    expect(container.firstChild).toHaveClass('custom-class');
  });
});
