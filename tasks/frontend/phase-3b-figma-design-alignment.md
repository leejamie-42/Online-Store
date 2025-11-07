# Phase 3b: Figma Design Alignment
## Product Detail Page - Design Implementation Guide

**Created:** October 2025
**Figma Source:** https://www.figma.com/design/BYlBng6s0iLmcrXxAaWvDt/Store-application?node-id=1-196
**Status:** Design Analysis Complete

---

## Overview

This document provides detailed specifications for aligning the Product Detail Page implementation with the Figma design. It identifies additional components, styling details, and modifications needed beyond the base task document.

---

## Visual Analysis Summary

### Layout Structure
- **2-column grid layout** with 32px gap (`gap-[32px]`)
- **Left column**: Product image in white card
- **Right column**: Product information and purchase controls
- **Container max-width**: ~1280px
- **Spacing**: Consistent 24px gaps between major sections

### Design Tokens

#### Colors
```typescript
export const designTokens = {
  colors: {
    primary: '#155dfc',        // Price, links
    text: {
      heading: '#030213',      // Near black for headings
      body: '#4a5565',         // Gray for descriptions
      muted: '#6b7280',        // Light gray for secondary text
    },
    background: {
      white: '#ffffff',
      gray: '#f3f3f5',         // Input backgrounds
      categoryBadge: '#eceef2', // Badge background
      infoBox: 'rgb(239, 246, 255)', // blue-50
    },
    border: 'rgba(0, 0, 0, 0.1)', // Subtle borders
    button: {
      primary: '#030213',      // Buy Now button
      primaryText: '#ffffff',
    },
  },
  typography: {
    productName: {
      fontSize: '16px',
      fontWeight: '400',
      lineHeight: '24px',
      letterSpacing: '-0.3125px',
    },
    price: {
      fontSize: '30px',
      fontWeight: '400',
      lineHeight: '36px',
      letterSpacing: '0.3955px',
      color: '#155dfc',
    },
    descriptionHeading: {
      fontSize: '18px',
      fontWeight: '500',
      lineHeight: '27px',
      letterSpacing: '-0.4395px',
    },
    body: {
      fontSize: '16px',
      fontWeight: '400',
      lineHeight: '24px',
      letterSpacing: '-0.3125px',
      color: '#4a5565',
    },
    small: {
      fontSize: '14px',
      fontWeight: '400',
      lineHeight: '20px',
      letterSpacing: '-0.1504px',
    },
  },
  borderRadius: {
    button: '8px',
    card: '14px',
    badge: '8px',
    infoBox: '10px',
  },
  spacing: {
    sectionGap: '24px',
    columnGap: '32px',
    cardPadding: '17px',
  },
};
```

---

## New Components Required

### 1. CategoryBadge Component

**Location:** Not in current task document
**Priority:** HIGH

**Visual Specs:**
- Background: `#eceef2`
- Text color: `#030213`
- Font: 12px, Inter Medium
- Padding: 4px 9px
- Border-radius: 8px
- Height: 22px

**Implementation:**

**frontend/src/components/features/product/CategoryBadge/CategoryBadge.test.tsx:**
```typescript
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
```

**frontend/src/components/features/product/CategoryBadge/CategoryBadge.tsx:**
```typescript
import React from 'react';

interface CategoryBadgeProps {
  category: string;
  className?: string;
}

/**
 * CategoryBadge Component
 * Display product category badge
 * Based on Figma design: node-id 1:211
 */
export const CategoryBadge: React.FC<CategoryBadgeProps> = ({
  category,
  className = '',
}) => {
  return (
    <span
      className={`
        inline-flex items-center
        px-[9px] py-[4px]
        bg-[#eceef2]
        text-[#030213]
        text-[12px]
        font-medium
        leading-[16px]
        tracking-[-0.1504px]
        rounded-[8px]
        h-[22px]
        ${className}
      `}
    >
      {category}
    </span>
  );
};
```

**frontend/src/components/features/product/CategoryBadge/index.ts:**
```typescript
export { CategoryBadge } from './CategoryBadge';
```

---

### 2. ProductFeatures Component

**Location:** Not in current task document
**Priority:** HIGH

**Visual Specs:**
- Background: `rgb(239, 246, 255)` (blue-50)
- Text color: `#364153`
- Font: 14px, Inter Regular
- Padding: 16px
- Border-radius: 10px
- Gap between items: 8px
- Checkmark: "✓" character

**Implementation:**

**frontend/src/components/features/product/ProductFeatures/ProductFeatures.test.tsx:**
```typescript
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
```

**frontend/src/components/features/product/ProductFeatures/ProductFeatures.tsx:**
```typescript
import React from 'react';

interface ProductFeaturesProps {
  features?: string[];
  className?: string;
}

const defaultFeatures = [
  'Secure payment processing',
  'Free shipping on orders over $100',
  '30-day return policy',
];

/**
 * ProductFeatures Component
 * Display product features/benefits with checkmarks
 * Based on Figma design: node-id 1:243
 */
export const ProductFeatures: React.FC<ProductFeaturesProps> = ({
  features = defaultFeatures,
  className = '',
}) => {
  if (features.length === 0) return null;

  return (
    <div
      className={`
        flex flex-col gap-[8px]
        p-[16px]
        bg-blue-50
        rounded-[10px]
        ${className}
      `}
    >
      {features.map((feature, index) => (
        <div key={index} className="flex items-start gap-[8px]">
          <span
            className="
              checkmark
              text-[#364153]
              text-[14px]
              font-normal
              leading-[20px]
              tracking-[-0.1504px]
              shrink-0
            "
          >
            ✓
          </span>
          <p
            className="
              text-[#364153]
              text-[14px]
              font-normal
              leading-[20px]
              tracking-[-0.1504px]
            "
          >
            {feature}
          </p>
        </div>
      ))}
    </div>
  );
};
```

**frontend/src/components/features/product/ProductFeatures/index.ts:**
```typescript
export { ProductFeatures } from './ProductFeatures';
```

---

## Modified Components

### 1. ProductInfo Component Updates

**Changes Required:**
1. Add CategoryBadge below product name
2. Update product name font size to 16px (not 3xl/24px)
3. Remove SKU display (not in Figma)
4. Update price color to `#155dfc`
5. Update description text color to `#4a5565`
6. Remove border above description section

**Updated Implementation:**

**frontend/src/components/features/product/ProductInfo/ProductInfo.tsx:**
```typescript
import React from 'react';
import { PriceDisplay } from '../PriceDisplay';
import { CategoryBadge } from '../CategoryBadge';
import type { ProductDetail } from '@/types/product.types';

interface ProductInfoProps {
  product: ProductDetail;
  category?: string; // Optional category from product
}

/**
 * ProductInfo Component
 * Detailed product information display
 * Updated to match Figma design
 */
export const ProductInfo: React.FC<ProductInfoProps> = ({
  product,
  category = 'Accessories', // Default category
}) => {
  return (
    <div className="space-y-[24px]">
      {/* Product Name and Category */}
      <div className="h-[56.5px] flex flex-col gap-[10.5px]">
        <h1
          className="
            text-[16px]
            font-normal
            leading-[24px]
            tracking-[-0.3125px]
            text-neutral-950
          "
        >
          {product.name}
        </h1>
        <CategoryBadge category={category} />
      </div>

      {/* Price */}
      <div className="h-[36px]">
        <p
          className="
            text-[30px]
            font-normal
            leading-[36px]
            tracking-[0.3955px]
            text-[#155dfc]
          "
        >
          ${product.price.toFixed(2)}
        </p>
      </div>

      {/* Description */}
      <div className="flex flex-col gap-[8px]">
        <h2
          className="
            text-[18px]
            font-medium
            leading-[27px]
            tracking-[-0.4395px]
            text-neutral-950
          "
        >
          Description
        </h2>
        <p
          className="
            text-[16px]
            font-normal
            leading-[24px]
            tracking-[-0.3125px]
            text-[#4a5565]
          "
        >
          {product.description}
        </p>
      </div>
    </div>
  );
};
```

---

### 2. QuantitySelector Component Updates

**Changes Required:**
1. Update input background to `#f3f3f5`
2. Update input width to 80px (not 64px/16 = w-16)
3. Remove borders on buttons (shown as disabled in Figma)
4. Update button styling
5. Change gap to 12px

**Updated Styling:**

```typescript
// In QuantitySelector.tsx, update className strings:

// Decrement button
className="
  p-2
  bg-white
  border border-[rgba(0,0,0,0.1)] border-solid
  rounded-[8px]
  hover:bg-gray-50
  disabled:opacity-50
  disabled:cursor-not-allowed
  w-[36px] h-[36px]
"

// Input
className="
  w-[80px]
  h-[36px]
  px-[12px]
  py-[4px]
  text-center
  text-[14px]
  font-normal
  leading-[20px]
  tracking-[-0.1504px]
  bg-[#f3f3f5]
  border-0
  rounded-[8px]
  focus:ring-2
  focus:ring-primary-500
"

// Increment button (same as decrement)

// Container gap
className={`flex items-center gap-[12px] ${className}`}
```

---

### 3. ProductDetail Page Major Updates

**Critical Changes:**

1. **Button Text**: Change from "Add to Cart" to "Buy Now"
2. **Add Purchase Card**: Wrap quantity selector and button in white card
3. **Add ProductFeatures**: Include feature box at bottom
4. **Stock Text Format**: Change to "X items available in stock"
5. **Remove Breadcrumb**: Not in Figma design
6. **Update Grid Gap**: Use 32px gap
7. **Update Card Styling**: Match Figma borders and shadows

**Updated ProductDetail.tsx (Key sections):**

```typescript
import { ProductGallery } from '@/components/features/product/ProductGallery';
import { ProductInfo } from '@/components/features/product/ProductInfo';
import { ProductFeatures } from '@/components/features/product/ProductFeatures';
import { QuantitySelector } from '@/components/common/QuantitySelector';
import { Button } from '@/components/ui/Button';
import { LuArrowLeft, LuShoppingCart } from 'react-icons/lu';

export const ProductDetail: React.FC = () => {
  // ... existing code ...

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Back Button */}
      <button
        onClick={handleBackToProducts}
        className="
          flex items-center gap-2
          text-[14px] font-medium
          leading-[20px]
          tracking-[-0.1504px]
          text-neutral-950
          hover:text-gray-700
          mb-8
        "
      >
        <LuArrowLeft className="w-4 h-4" />
        Back to Products
      </button>

      {/* Product Details Grid - Updated gap to 32px */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-[32px]">
        {/* Product Gallery - Left Column */}
        <div>
          <div
            className="
              bg-white
              border border-[rgba(0,0,0,0.1)] border-solid
              rounded-[14px]
              overflow-hidden
            "
          >
            <ProductGallery
              mainImage={product.image_url}
              altText={product.name}
            />
          </div>
        </div>

        {/* Product Info - Right Column */}
        <div className="flex flex-col gap-[24px]">
          {/* Product Information */}
          <ProductInfo product={product} />

          {/* Purchase Card */}
          <div
            className="
              bg-white
              border border-[rgba(0,0,0,0.1)] border-solid
              rounded-[14px]
              p-[17px]
              flex flex-col gap-[16px]
            "
          >
            {/* Quantity Selector Section */}
            {!isOutOfStock && (
              <div className="flex flex-col gap-[8px]">
                <label
                  className="
                    text-[14px]
                    font-medium
                    leading-[14px]
                    tracking-[-0.1504px]
                    text-neutral-950
                  "
                >
                  Quantity
                </label>
                <QuantitySelector
                  value={quantity}
                  onChange={setQuantity}
                  max={maxQuantity}
                />
                <p
                  className="
                    text-[14px]
                    font-normal
                    leading-[20px]
                    tracking-[-0.1504px]
                    text-[#4a5565]
                  "
                >
                  {product.stock} items available in stock
                </p>
              </div>
            )}

            {/* Buy Now Button */}
            <button
              onClick={handleBuyNow}
              disabled={isOutOfStock}
              className={`
                h-[40px]
                w-full
                flex items-center justify-center gap-2
                rounded-[8px]
                text-[14px]
                font-medium
                leading-[20px]
                tracking-[-0.1504px]
                ${isOutOfStock
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-[#030213] text-white hover:bg-gray-900'
                }
              `}
            >
              {!isOutOfStock && <LuShoppingCart className="w-4 h-4" />}
              {isOutOfStock ? 'Out of Stock' : 'Buy Now'}
            </button>
          </div>

          {/* Product Features */}
          <ProductFeatures />
        </div>
      </div>
    </div>
  );
};
```

---

## API Type Updates

**Add category field to ProductDetail type:**

```typescript
// In frontend/src/types/product.types.ts

export interface ProductDetail {
  id: string;
  name: string;
  description: string;
  price: number;
  stock: number;
  image_url: string;
  published: boolean;
  category?: string; // Optional: Add if API provides category
}
```

**Note:** If category is not provided by API, use a default value or derive from product name/description.

---

## Design Decisions

### 1. Breadcrumb Navigation

**Figma Design:** No breadcrumb visible
**Task Document:** Includes breadcrumb (Task 3.1)

**Recommendation:** Make breadcrumb optional or skip implementation to match Figma design exactly. The "Back to Products" button serves the same navigation purpose.

**Action:** Update Task 3.1 to be marked as "Optional - Not in Figma design"

---

### 2. Button Functionality

**Figma:** "Buy Now" button
**Current:** "Add to Cart" button

**Recommendation:** Change button text to "Buy Now" and update the handler name from `handleAddToCart` to `handleBuyNow`.

**Implementation Notes:**
- For MVP, both buttons can have same placeholder functionality
- In Phase 4 (cart), "Buy Now" might skip cart and go directly to checkout
- "Add to Cart" would add item and stay on page

---

### 3. Stock Display Format

**Figma:** "25 items available in stock"
**Current:** "25 available"

**Action:** Update StockBadge component or add separate stock text in ProductDetail page to match Figma format.

---

## Task Document Updates Required

### New Tasks to Add:

**Task 2.0: Create CategoryBadge Component (TDD)**
- Location: Before Task 2.1
- Time: 45 minutes
- Dependencies: Task 1.2

**Task 2.5: Create ProductFeatures Component (TDD)**
- Location: After Task 2.4
- Time: 1 hour
- Dependencies: Task 2.4

### Tasks to Update:

**Task 2.2: ProductInfo Component**
- Add CategoryBadge integration
- Update styling to match Figma specs
- Remove SKU display
- Update typography

**Task 2.3: QuantitySelector Component**
- Update styling (background colors, gaps, widths)
- Match exact Figma measurements

**Task 2.4: ProductDetail Page Integration**
- Change button text to "Buy Now"
- Add Purchase Card wrapper
- Add ProductFeatures component
- Update stock text format
- Update grid gap to 32px

**Task 3.1: Breadcrumb Navigation**
- Mark as Optional
- Note: Not in Figma design, only "Back to Products" button

---

## Implementation Checklist

### Phase 1: New Components
- [ ] Create CategoryBadge component with tests
- [ ] Create ProductFeatures component with tests
- [ ] Verify components render correctly

### Phase 2: Update Existing Components
- [ ] Update ProductInfo to include CategoryBadge
- [ ] Update ProductInfo typography and spacing
- [ ] Update QuantitySelector styling
- [ ] Remove SKU from ProductInfo

### Phase 3: ProductDetail Page Integration
- [ ] Add Purchase Card wrapper
- [ ] Change button to "Buy Now"
- [ ] Add ProductFeatures component
- [ ] Update stock text format
- [ ] Update grid gap to 32px
- [ ] Update card borders and styling

### Phase 4: Styling Refinements
- [ ] Match exact colors from Figma
- [ ] Match exact typography (sizes, weights, tracking)
- [ ] Match exact spacing (gaps, padding)
- [ ] Match exact border radii

### Phase 5: Testing
- [ ] Update all tests for component changes
- [ ] Add tests for new components
- [ ] E2E tests with updated button text
- [ ] Visual regression testing (if available)

---

## Color Palette Reference

```css
/* Primary Colors */
--color-primary-blue: #155dfc;
--color-primary-black: #030213;

/* Text Colors */
--color-text-heading: #030213;
--color-text-body: #4a5565;
--color-text-secondary: #364153;
--color-text-muted: #6b7280;

/* Background Colors */
--color-bg-white: #ffffff;
--color-bg-gray: #f3f3f5;
--color-bg-category: #eceef2;
--color-bg-info: rgb(239, 246, 255); /* blue-50 */

/* Border Colors */
--color-border: rgba(0, 0, 0, 0.1);

/* Button Colors */
--color-button-primary: #030213;
--color-button-primary-text: #ffffff;
```

---

## Typography Scale

```css
/* Product Name */
font-size: 16px;
font-weight: 400;
line-height: 24px;
letter-spacing: -0.3125px;

/* Price */
font-size: 30px;
font-weight: 400;
line-height: 36px;
letter-spacing: 0.3955px;
color: #155dfc;

/* Description Heading */
font-size: 18px;
font-weight: 500;
line-height: 27px;
letter-spacing: -0.4395px;

/* Body Text */
font-size: 16px;
font-weight: 400;
line-height: 24px;
letter-spacing: -0.3125px;
color: #4a5565;

/* Small Text / Labels */
font-size: 14px;
font-weight: 400;
line-height: 20px;
letter-spacing: -0.1504px;

/* Category Badge */
font-size: 12px;
font-weight: 500;
line-height: 16px;
```

---

## Summary

Total new tasks: **2** (CategoryBadge, ProductFeatures)
Total modified tasks: **3** (ProductInfo, QuantitySelector, ProductDetail)
Additional time estimate: **+2.5 hours**
New total time: **10.5-12.5 hours** (1.5-2 days)

**Critical Changes:**
1. Button text: "Buy Now" not "Add to Cart"
2. Add CategoryBadge component
3. Add ProductFeatures component
4. Update all typography and colors to match Figma
5. Add Purchase Card wrapper
6. Update stock text format

**Document Status:** Ready for implementation
**Next Step:** Update main task document with these changes

---

**Last Updated:** October 2025
**Figma Node:** 1:196
**Reviewers:** Development Team
