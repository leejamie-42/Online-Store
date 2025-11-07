# UI Design System
## Online Store Application

**Version:** 1.0
**Last Updated:** October 2025

---

## Table of Contents

1. [Overview](#overview)
2. [Design Principles](#design-principles)
3. [Color Palette](#color-palette)
4. [Typography](#typography)
5. [Spacing System](#spacing-system)
6. [Components](#components)
7. [Layout Guidelines](#layout-guidelines)
8. [Responsive Design](#responsive-design)
9. [Accessibility](#accessibility)

---

## Overview

This document defines the visual design system for the Online Store application. It ensures consistency across the application and provides clear guidelines for implementing UI components.

### Design Goals

- **Consistency** - Unified look and feel across all pages
- **Usability** - Intuitive and easy to use
- **Accessibility** - WCAG 2.1 Level AA compliance
- **Responsiveness** - Works seamlessly on all devices
- **Performance** - Optimized for fast loading

---

## Design Principles

### 1. Clarity
Keep the interface clean and focused. Remove unnecessary elements and use clear, concise language.

### 2. Consistency
Use consistent patterns, components, and interactions throughout the application.

### 3. Efficiency
Help users accomplish tasks quickly with minimal friction.

### 4. Feedback
Provide clear feedback for user actions (loading states, success/error messages).

### 5. Accessibility
Design for all users, including those with disabilities.

---

## Color Palette

### Primary Colors

```css
/* Brand Colors */
--color-primary-50: #eff6ff;
--color-primary-100: #dbeafe;
--color-primary-200: #bfdbfe;
--color-primary-300: #93c5fd;
--color-primary-400: #60a5fa;
--color-primary-500: #3b82f6;  /* Main brand color */
--color-primary-600: #2563eb;
--color-primary-700: #1d4ed8;
--color-primary-800: #1e40af;
--color-primary-900: #1e3a8a;
```

**Usage:**
- Primary buttons
- Links
- Active states
- Brand elements

### Secondary Colors

```css
/* Accent Colors */
--color-secondary-50: #faf5ff;
--color-secondary-100: #f3e8ff;
--color-secondary-200: #e9d5ff;
--color-secondary-300: #d8b4fe;
--color-secondary-400: #c084fc;
--color-secondary-500: #a855f7;  /* Accent color */
--color-secondary-600: #9333ea;
--color-secondary-700: #7e22ce;
--color-secondary-800: #6b21a8;
--color-secondary-900: #581c87;
```

**Usage:**
- Secondary actions
- Highlights
- Special features

### Neutral Colors

```css
/* Grayscale */
--color-gray-50: #f9fafb;
--color-gray-100: #f3f4f6;
--color-gray-200: #e5e7eb;
--color-gray-300: #d1d5db;
--color-gray-400: #9ca3af;
--color-gray-500: #6b7280;
--color-gray-600: #4b5563;
--color-gray-700: #374151;
--color-gray-800: #1f2937;
--color-gray-900: #111827;
```

**Usage:**
- Text
- Borders
- Backgrounds
- Shadows

### Semantic Colors

```css
/* Success */
--color-success-50: #f0fdf4;
--color-success-500: #22c55e;
--color-success-700: #15803d;

/* Warning */
--color-warning-50: #fffbeb;
--color-warning-500: #f59e0b;
--color-warning-700: #b45309;

/* Error */
--color-error-50: #fef2f2;
--color-error-500: #ef4444;
--color-error-700: #b91c1c;

/* Info */
--color-info-50: #eff6ff;
--color-info-500: #3b82f6;
--color-info-700: #1d4ed8;
```

**Usage:**
- Success: Order confirmations, success messages
- Warning: Stock warnings, pending actions
- Error: Form errors, failed actions
- Info: Tips, informational messages

### Tailwind Config

```javascript
// tailwind.config.js
module.exports = {
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#eff6ff',
          100: '#dbeafe',
          200: '#bfdbfe',
          300: '#93c5fd',
          400: '#60a5fa',
          500: '#3b82f6',
          600: '#2563eb',
          700: '#1d4ed8',
          800: '#1e40af',
          900: '#1e3a8a',
        },
        // ... other colors
      },
    },
  },
};
```

---

## Typography

### Font Families

```css
/* Primary Font - Inter */
--font-primary: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;

/* Monospace Font */
--font-mono: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
```

### Font Sizes

```css
/* Text Scale */
--text-xs: 0.75rem;     /* 12px */
--text-sm: 0.875rem;    /* 14px */
--text-base: 1rem;      /* 16px */
--text-lg: 1.125rem;    /* 18px */
--text-xl: 1.25rem;     /* 20px */
--text-2xl: 1.5rem;     /* 24px */
--text-3xl: 1.875rem;   /* 30px */
--text-4xl: 2.25rem;    /* 36px */
--text-5xl: 3rem;       /* 48px */
```

### Font Weights

```css
--font-light: 300;
--font-normal: 400;
--font-medium: 500;
--font-semibold: 600;
--font-bold: 700;
--font-extrabold: 800;
```

### Line Heights

```css
--leading-tight: 1.25;
--leading-snug: 1.375;
--leading-normal: 1.5;
--leading-relaxed: 1.625;
--leading-loose: 2;
```

### Typography Usage

#### Headings

```typescript
// H1 - Page Titles
<h1 className="text-4xl font-bold text-gray-900 mb-4">
  Page Title
</h1>

// H2 - Section Headers
<h2 className="text-3xl font-semibold text-gray-800 mb-3">
  Section Header
</h2>

// H3 - Subsection Headers
<h3 className="text-2xl font-semibold text-gray-800 mb-2">
  Subsection Header
</h3>

// H4 - Component Headers
<h4 className="text-xl font-medium text-gray-700 mb-2">
  Component Header
</h4>
```

#### Body Text

```typescript
// Large Body
<p className="text-lg text-gray-700 leading-relaxed">
  Large body text for introductions or emphasis
</p>

// Normal Body
<p className="text-base text-gray-600 leading-normal">
  Standard body text for most content
</p>

// Small Text
<p className="text-sm text-gray-500">
  Smaller text for captions, labels, or secondary information
</p>
```

---

## Spacing System

### Spacing Scale (8px base)

```css
--space-0: 0;
--space-1: 0.25rem;   /* 4px */
--space-2: 0.5rem;    /* 8px */
--space-3: 0.75rem;   /* 12px */
--space-4: 1rem;      /* 16px */
--space-5: 1.25rem;   /* 20px */
--space-6: 1.5rem;    /* 24px */
--space-8: 2rem;      /* 32px */
--space-10: 2.5rem;   /* 40px */
--space-12: 3rem;     /* 48px */
--space-16: 4rem;     /* 64px */
--space-20: 5rem;     /* 80px */
--space-24: 6rem;     /* 96px */
```

### Spacing Guidelines

- **Tight spacing (4-8px):** Between related elements (icon + text, form label + input)
- **Normal spacing (16-24px):** Between components in a group
- **Loose spacing (32-48px):** Between sections
- **Extra loose spacing (64px+):** Between major page sections

---

## Components

### Buttons

#### Primary Button

```typescript
<button className="
  px-6 py-3
  bg-primary-600 hover:bg-primary-700
  text-white font-medium
  rounded-lg
  transition-colors duration-200
  focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2
  disabled:opacity-50 disabled:cursor-not-allowed
">
  Primary Action
</button>
```

#### Secondary Button

```typescript
<button className="
  px-6 py-3
  bg-white hover:bg-gray-50
  text-gray-700 font-medium
  border border-gray-300
  rounded-lg
  transition-colors duration-200
  focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2
">
  Secondary Action
</button>
```

#### Ghost Button

```typescript
<button className="
  px-4 py-2
  text-primary-600 hover:text-primary-700
  font-medium
  transition-colors duration-200
  focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2
">
  Ghost Button
</button>
```

#### Button Sizes

```typescript
// Small
<Button size="sm" className="px-3 py-1.5 text-sm">Small</Button>

// Medium (default)
<Button size="md" className="px-6 py-3 text-base">Medium</Button>

// Large
<Button size="lg" className="px-8 py-4 text-lg">Large</Button>
```

### Input Fields

```typescript
<div className="space-y-2">
  <label htmlFor="email" className="block text-sm font-medium text-gray-700">
    Email Address
  </label>
  <input
    id="email"
    type="email"
    className="
      w-full px-4 py-2
      border border-gray-300 rounded-lg
      focus:ring-2 focus:ring-primary-500 focus:border-transparent
      placeholder:text-gray-400
      disabled:bg-gray-50 disabled:text-gray-500
    "
    placeholder="Enter your email"
  />
  <p className="text-sm text-gray-500">We'll never share your email.</p>
</div>
```

#### Input States

```typescript
// Error State
<input className="border-red-500 focus:ring-red-500" />
<p className="text-sm text-red-600">This field is required</p>

// Success State
<input className="border-green-500 focus:ring-green-500" />
<p className="text-sm text-green-600">Looks good!</p>
```

### Cards

```typescript
<div className="
  bg-white
  border border-gray-200
  rounded-xl
  shadow-sm
  overflow-hidden
  hover:shadow-md
  transition-shadow duration-200
">
  <div className="p-6">
    <h3 className="text-xl font-semibold text-gray-900 mb-2">
      Card Title
    </h3>
    <p className="text-gray-600">
      Card content goes here
    </p>
  </div>
</div>
```

### Badges

```typescript
// Success Badge
<span className="
  inline-flex items-center px-2.5 py-0.5
  rounded-full text-xs font-medium
  bg-green-100 text-green-800
">
  In Stock
</span>

// Warning Badge
<span className="
  inline-flex items-center px-2.5 py-0.5
  rounded-full text-xs font-medium
  bg-yellow-100 text-yellow-800
">
  Low Stock
</span>

// Error Badge
<span className="
  inline-flex items-center px-2.5 py-0.5
  rounded-full text-xs font-medium
  bg-red-100 text-red-800
">
  Out of Stock
</span>
```

### Modals

```typescript
<div className="fixed inset-0 z-50 overflow-y-auto">
  {/* Backdrop */}
  <div className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" />

  {/* Modal Container */}
  <div className="flex min-h-full items-center justify-center p-4">
    {/* Modal Content */}
    <div className="
      relative
      bg-white
      rounded-xl
      shadow-xl
      max-w-lg w-full
      p-6
    ">
      <h2 className="text-2xl font-semibold text-gray-900 mb-4">
        Modal Title
      </h2>
      <p className="text-gray-600 mb-6">
        Modal content goes here
      </p>
      <div className="flex justify-end space-x-3">
        <Button variant="secondary">Cancel</Button>
        <Button variant="primary">Confirm</Button>
      </div>
    </div>
  </div>
</div>
```

### Toast Notifications

```typescript
// Success Toast
<div className="
  flex items-center gap-3
  bg-white border-l-4 border-green-500
  rounded-lg shadow-lg
  p-4 mb-3
">
  <CheckCircleIcon className="w-5 h-5 text-green-500" />
  <div>
    <p className="font-medium text-gray-900">Success!</p>
    <p className="text-sm text-gray-600">Your item was added to cart.</p>
  </div>
</div>

// Error Toast
<div className="
  flex items-center gap-3
  bg-white border-l-4 border-red-500
  rounded-lg shadow-lg
  p-4 mb-3
">
  <XCircleIcon className="w-5 h-5 text-red-500" />
  <div>
    <p className="font-medium text-gray-900">Error</p>
    <p className="text-sm text-gray-600">Something went wrong.</p>
  </div>
</div>
```

### Loading States

```typescript
// Spinner
<div className="
  animate-spin
  rounded-full
  h-8 w-8
  border-4 border-gray-200
  border-t-primary-600
" />

// Skeleton
<div className="animate-pulse space-y-4">
  <div className="h-4 bg-gray-200 rounded w-3/4"></div>
  <div className="h-4 bg-gray-200 rounded"></div>
  <div className="h-4 bg-gray-200 rounded w-5/6"></div>
</div>

// Progress Bar
<div className="w-full bg-gray-200 rounded-full h-2">
  <div
    className="bg-primary-600 h-2 rounded-full transition-all duration-300"
    style={{ width: '60%' }}
  />
</div>
```

---

## Layout Guidelines

### Container Widths

```typescript
// Full Width
<div className="w-full px-4">

// Constrained Container
<div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

// Narrow Container (for text content)
<div className="max-w-3xl mx-auto px-4">
```

### Grid Layouts

```typescript
// Product Grid
<div className="
  grid
  grid-cols-1
  sm:grid-cols-2
  lg:grid-cols-3
  xl:grid-cols-4
  gap-6
">
  {products.map(product => (
    <ProductCard key={product.id} product={product} />
  ))}
</div>

// Two Column Layout
<div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
  <div>Left Column</div>
  <div>Right Column</div>
</div>
```

### Flexbox Layouts

```typescript
// Header Layout
<header className="flex items-center justify-between p-4">
  <Logo />
  <Navigation />
  <UserMenu />
</header>

// Card Footer
<div className="flex items-center justify-between mt-4">
  <PriceDisplay price={product.price} />
  <Button>Add to Cart</Button>
</div>
```

---

## Responsive Design

### Breakpoints

```javascript
// Tailwind breakpoints
{
  'sm': '640px',   // Mobile landscape / small tablet
  'md': '768px',   // Tablet
  'lg': '1024px',  // Laptop
  'xl': '1280px',  // Desktop
  '2xl': '1536px', // Large desktop
}
```

### Mobile-First Approach

Always design for mobile first, then add styles for larger screens.

```typescript
// Mobile: Stack vertically
// Desktop: Side by side
<div className="
  flex flex-col
  lg:flex-row
  gap-4 lg:gap-8
">
  <div className="w-full lg:w-1/2">Content 1</div>
  <div className="w-full lg:w-1/2">Content 2</div>
</div>
```

### Responsive Typography

```typescript
<h1 className="text-3xl sm:text-4xl lg:text-5xl font-bold">
  Responsive Heading
</h1>

<p className="text-sm sm:text-base lg:text-lg">
  Responsive paragraph text
</p>
```

### Responsive Spacing

```typescript
<div className="p-4 sm:p-6 lg:p-8">
  <div className="space-y-4 sm:space-y-6 lg:space-y-8">
    {/* Content */}
  </div>
</div>
```

---

## Accessibility

### Color Contrast

All text must meet WCAG 2.1 Level AA standards:
- **Normal text:** 4.5:1 contrast ratio
- **Large text (18px+ or 14px+ bold):** 3:1 contrast ratio

```typescript
// Good contrast
<p className="text-gray-900 bg-white">High contrast text</p>

// Avoid low contrast
<p className="text-gray-400 bg-gray-300">Low contrast - hard to read</p>
```

### Keyboard Navigation

Ensure all interactive elements are keyboard accessible.

```typescript
// Visible focus states
<button className="
  focus:outline-none
  focus:ring-2
  focus:ring-primary-500
  focus:ring-offset-2
">
  Accessible Button
</button>
```

### ARIA Labels

```typescript
// Icon buttons need labels
<button aria-label="Close modal">
  <XIcon className="w-5 h-5" />
</button>

// Form inputs need labels
<label htmlFor="email" className="sr-only">Email</label>
<input id="email" type="email" aria-describedby="email-help" />
<p id="email-help" className="text-sm">We'll never share your email</p>
```

### Screen Reader Support

```typescript
// Skip to main content
<a href="#main-content" className="sr-only focus:not-sr-only">
  Skip to main content
</a>

// Screen reader only text
<span className="sr-only">Loading...</span>

// Live regions for dynamic updates
<div aria-live="polite" aria-atomic="true">
  Item added to cart
</div>
```

### Semantic HTML

Use semantic HTML elements:

```typescript
// Good
<nav>
  <ul>
    <li><a href="/">Home</a></li>
  </ul>
</nav>

// Avoid
<div className="nav">
  <div><a href="/">Home</a></div>
</div>
```

---

## Animation Guidelines

### Transition Timing

```css
/* Fast transitions for small changes */
--duration-fast: 150ms;

/* Normal transitions for most interactions */
--duration-normal: 200ms;

/* Slow transitions for large movements */
--duration-slow: 300ms;
```

### Easing Functions

```css
/* Default easing */
--ease-default: cubic-bezier(0.4, 0, 0.2, 1);

/* Ease in (accelerating) */
--ease-in: cubic-bezier(0.4, 0, 1, 1);

/* Ease out (decelerating) */
--ease-out: cubic-bezier(0, 0, 0.2, 1);
```

### Animation Examples

```typescript
// Hover effects
<button className="
  transition-colors duration-200
  hover:bg-primary-700
">
  Hover Me
</button>

// Fade in
<div className="
  opacity-0
  animate-fade-in
">
  Fading in...
</div>

// Slide in
<div className="
  transform translate-x-full
  animate-slide-in
">
  Sliding in...
</div>
```

---

## Icons

### Icon Library

Use [React Icons](https://react-icons.github.io/react-icons/) with Lucide icons (/lu) for consistent icon design.

```typescript
import { LuShoppingCart, LuUser, LuSearch, LuHeart } from 'react-icons/lu';

<LuShoppingCart className="w-5 h-5 text-gray-600" />
```

### Icon Sizes

```typescript
// Small icons
<Icon className="w-4 h-4" />

// Medium icons (default)
<Icon className="w-5 h-5" />

// Large icons
<Icon className="w-6 h-6" />

// Extra large icons
<Icon className="w-8 h-8" />
```

---

## Images

### Image Optimization

```typescript
// Lazy loading
<img
  src={product.image}
  alt={product.name}
  loading="lazy"
  className="w-full h-auto"
/>

// Responsive images
<img
  srcSet="
    image-320w.jpg 320w,
    image-640w.jpg 640w,
    image-1280w.jpg 1280w
  "
  sizes="(max-width: 640px) 100vw, 640px"
  src="image-640w.jpg"
  alt="Product"
/>
```

### Aspect Ratios

```typescript
// Square (1:1) - Profile pictures
<div className="aspect-square">
  <img src="..." className="w-full h-full object-cover" />
</div>

// Product images (4:3)
<div className="aspect-4/3">
  <img src="..." className="w-full h-full object-cover" />
</div>

// Banner (16:9)
<div className="aspect-video">
  <img src="..." className="w-full h-full object-cover" />
</div>
```

---

## Form Design

### Form Layout

```typescript
<form className="space-y-6 max-w-md">
  {/* Text Input */}
  <div>
    <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
      Full Name
    </label>
    <input
      id="name"
      type="text"
      className="w-full px-4 py-2 border border-gray-300 rounded-lg"
    />
  </div>

  {/* Select */}
  <div>
    <label htmlFor="country" className="block text-sm font-medium text-gray-700 mb-2">
      Country
    </label>
    <select
      id="country"
      className="w-full px-4 py-2 border border-gray-300 rounded-lg"
    >
      <option>Select a country</option>
      <option>United States</option>
      <option>Canada</option>
    </select>
  </div>

  {/* Checkbox */}
  <div className="flex items-start">
    <input
      id="terms"
      type="checkbox"
      className="mt-1 h-4 w-4 text-primary-600 rounded"
    />
    <label htmlFor="terms" className="ml-2 text-sm text-gray-700">
      I agree to the terms and conditions
    </label>
  </div>

  {/* Submit Button */}
  <button
    type="submit"
    className="w-full px-6 py-3 bg-primary-600 text-white rounded-lg"
  >
    Submit
  </button>
</form>
```

---

## Dark Mode (Optional)

### Color Scheme

```typescript
// Light mode (default)
<div className="bg-white text-gray-900">

// Dark mode
<div className="dark:bg-gray-900 dark:text-white">

// Toggle
<button
  onClick={() => setTheme(theme === 'light' ? 'dark' : 'light')}
  className="p-2 rounded-lg bg-gray-200 dark:bg-gray-800"
>
  {theme === 'light' ? <MoonIcon /> : <SunIcon />}
</button>
```

---

**Document Maintained By:** Design Team
**Last Reviewed:** October 2025
