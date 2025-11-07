import { describe, it, expect } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { ProductGallery } from './ProductGallery';

describe('ProductGallery', () => {
  it('renders main image', () => {
    render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
      />
    );

    const img = screen.getByAltText('Product');
    expect(img).toHaveAttribute('src', 'https://example.com/main.jpg');
  });

  it('renders with aspect ratio container', () => {
    const { container } = render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
      />
    );

    expect(container.querySelector('.aspect-square')).toBeInTheDocument();
  });

  it('handles missing image gracefully', () => {
    render(<ProductGallery mainImage="" altText="Product" />);

    expect(screen.getByText('No Image')).toBeInTheDocument();
  });

  it('displays fallback on image error', async () => {
    render(
      <ProductGallery
        mainImage="https://example.com/broken.jpg"
        altText="Product"
      />
    );

    const img = screen.getByAltText('Product') as HTMLImageElement;

    // Simulate image error
    img.dispatchEvent(new Event('error'));

    await waitFor(() => {
      expect(screen.getByText('No Image')).toBeInTheDocument();
    });
  });

  it('applies custom className', () => {
    const { container } = render(
      <ProductGallery
        mainImage="https://example.com/main.jpg"
        altText="Product"
        className="custom-gallery"
      />
    );

    expect(container.firstChild).toHaveClass('custom-gallery');
  });
});
