# Phase 2: Layout & Navigation - Implementation Tasks
## Online Store Application - Frontend

**Phase Duration:** 3-4 days
**Priority:** HIGH
**Status:** Not Started
**Last Updated:** October 2025

---

## Overview

This document contains detailed, actionable tasks for Phase 2 of the frontend implementation. Phase 2 focuses on building the application layout, navigation system, and authentication pages.

**Prerequisites:** Phase 1 must be completed before starting Phase 2. Phase 1 includes utilities, base UI components, API layer, and authentication context.

### Phase 2 Goals
- ✅ Create responsive layout system with header and footer
- ✅ Build authentication pages (Login, Register, Forgot Password)
- ✅ Implement navigation infrastructure
- ✅ Set up route structure with protected routes

---

## Table of Contents

1. [Section 1: Main Layout Components](#section-1-main-layout-components)
2. [Section 2: Authentication Pages](#section-2-authentication-pages)
3. [Section 3: Navigation System](#section-3-navigation-system)
4. [Testing & Validation](#testing--validation)

---

## Section 1: Main Layout Components

**Estimated Time:** 3-4 hours
**Dependencies:** Phase 1 complete

### Task 1.1: Create Header Component

**Status:** ⬜ Not Started
**Depends On:** Phase 1 complete

**Description:**
Create the main navigation header with logo, navigation links, cart icon, and user menu.

**Files to Create:**

**src/components/layout/Header/Header.tsx:**
```typescript
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Menu, ShoppingCart, User, LogOut, Package, Settings, X } from 'react-icons/lu';
import { useAuth } from '@/context/AuthContext';
import { ROUTES } from '@/config/routes';
import { Button } from '@/components/ui/Button';

export const Header: React.FC = () => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const { isAuthenticated, user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate(ROUTES.LOGIN);
  };

  const navigationLinks = [
    { label: 'Home', path: ROUTES.HOME },
    { label: 'Products', path: ROUTES.PRODUCTS },
    { label: 'Orders', path: ROUTES.ORDERS, protected: true },
  ];

  return (
    <header className="sticky top-0 z-50 bg-white shadow-sm border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <div className="flex items-center">
            <Link to={ROUTES.HOME} className="flex items-center space-x-2">
              <ShoppingCart className="h-8 w-8 text-primary-600" />
              <span className="text-xl font-bold text-gray-900">Store</span>
            </Link>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center space-x-8">
            {navigationLinks.map((link) => {
              if (link.protected && !isAuthenticated) return null;
              return (
                <Link
                  key={link.path}
                  to={link.path}
                  className="text-gray-700 hover:text-primary-600 font-medium transition-colors"
                >
                  {link.label}
                </Link>
              );
            })}
          </nav>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            {/* Cart Icon */}
            {isAuthenticated && (
              <Link to={ROUTES.CART} className="relative">
                <ShoppingCart className="h-6 w-6 text-gray-700 hover:text-primary-600 transition-colors" />
                <span className="absolute -top-2 -right-2 bg-primary-600 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center">
                  0
                </span>
              </Link>
            )}

            {/* User Menu or Auth Buttons */}
            {isAuthenticated ? (
              <div className="relative">
                <button
                  onClick={() => setIsUserMenuOpen(!isUserMenuOpen)}
                  className="flex items-center space-x-2 p-2 rounded-lg hover:bg-gray-100 transition-colors"
                >
                  <div className="h-8 w-8 rounded-full bg-primary-600 flex items-center justify-center">
                    <span className="text-white font-semibold">
                      {user?.fullName?.charAt(0) || 'U'}
                    </span>
                  </div>
                  <span className="hidden md:block text-sm font-medium text-gray-700">
                    {user?.fullName}
                  </span>
                </button>

                {/* User Dropdown Menu */}
                {isUserMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1">
                    <Link
                      to={ROUTES.PROFILE}
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center space-x-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      <User className="h-4 w-4" />
                      <span>Profile</span>
                    </Link>
                    <Link
                      to={ROUTES.ORDERS}
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center space-x-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      <Package className="h-4 w-4" />
                      <span>Orders</span>
                    </Link>
                    <Link
                      to={ROUTES.SETTINGS}
                      onClick={() => setIsUserMenuOpen(false)}
                      className="flex items-center space-x-2 px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                    >
                      <Settings className="h-4 w-4" />
                      <span>Settings</span>
                    </Link>
                    <hr className="my-1" />
                    <button
                      onClick={handleLogout}
                      className="flex items-center space-x-2 w-full px-4 py-2 text-sm text-red-600 hover:bg-gray-100"
                    >
                      <LogOut className="h-4 w-4" />
                      <span>Logout</span>
                    </button>
                  </div>
                )}
              </div>
            ) : (
              <div className="hidden md:flex items-center space-x-2">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => navigate(ROUTES.LOGIN)}
                >
                  Login
                </Button>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => navigate(ROUTES.REGISTER)}
                >
                  Sign Up
                </Button>
              </div>
            )}

            {/* Mobile Menu Button */}
            <button
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              className="md:hidden p-2 rounded-lg hover:bg-gray-100 transition-colors"
            >
              {isMobileMenuOpen ? (
                <X className="h-6 w-6 text-gray-700" />
              ) : (
                <Menu className="h-6 w-6 text-gray-700" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-gray-200">
            <nav className="flex flex-col space-y-2">
              {navigationLinks.map((link) => {
                if (link.protected && !isAuthenticated) return null;
                return (
                  <Link
                    key={link.path}
                    to={link.path}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition-colors"
                  >
                    {link.label}
                  </Link>
                );
              })}

              {!isAuthenticated && (
                <>
                  <hr className="my-2" />
                  <Link
                    to={ROUTES.LOGIN}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg font-medium transition-colors"
                  >
                    Login
                  </Link>
                  <Link
                    to={ROUTES.REGISTER}
                    onClick={() => setIsMobileMenuOpen(false)}
                    className="px-4 py-2 text-primary-600 hover:bg-gray-100 rounded-lg font-medium transition-colors"
                  >
                    Sign Up
                  </Link>
                </>
              )}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};
```

**src/components/layout/Header/index.ts:**
```typescript
export { Header } from './Header';
```

**Acceptance Criteria:**
- [ ] Header component created with responsive design
- [ ] Logo and navigation links work
- [ ] User menu dropdown works
- [ ] Cart icon displays with badge
- [ ] Mobile menu toggles correctly
- [ ] Auth buttons show/hide based on auth state

**Estimated Time:** 60 minutes

---

### Task 1.2: Create Footer Component

**Status:** ⬜ Not Started
**Depends On:** Phase 1 complete

**Description:**
Create footer component with links and information.

**Files to Create:**

**src/components/layout/Footer/Footer.tsx:**
```typescript
import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, Mail, Phone, MapPin } from 'react-icons/lu';
import { ROUTES } from '@/config/routes';

export const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand Section */}
          <div className="col-span-1 md:col-span-2">
            <Link to={ROUTES.HOME} className="flex items-center space-x-2 mb-4">
              <ShoppingCart className="h-8 w-8 text-primary-400" />
              <span className="text-xl font-bold text-white">Store</span>
            </Link>
            <p className="text-sm text-gray-400 mb-4">
              Your trusted online shopping destination. Quality products, fast delivery, and excellent customer service.
            </p>
            <div className="flex space-x-4">
              <a href="#" className="text-gray-400 hover:text-white transition-colors">
                <span className="sr-only">Facebook</span>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" />
                </svg>
              </a>
              <a href="#" className="text-gray-400 hover:text-white transition-colors">
                <span className="sr-only">Twitter</span>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M8.29 20.251c7.547 0 11.675-6.253 11.675-11.675 0-.178 0-.355-.012-.53A8.348 8.348 0 0022 5.92a8.19 8.19 0 01-2.357.646 4.118 4.118 0 001.804-2.27 8.224 8.224 0 01-2.605.996 4.107 4.107 0 00-6.993 3.743 11.65 11.65 0 01-8.457-4.287 4.106 4.106 0 001.27 5.477A4.072 4.072 0 012.8 9.713v.052a4.105 4.105 0 003.292 4.022 4.095 4.095 0 01-1.853.07 4.108 4.108 0 003.834 2.85A8.233 8.233 0 012 18.407a11.616 11.616 0 006.29 1.84" />
                </svg>
              </a>
              <a href="#" className="text-gray-400 hover:text-white transition-colors">
                <span className="sr-only">Instagram</span>
                <svg className="h-6 w-6" fill="currentColor" viewBox="0 0 24 24">
                  <path fillRule="evenodd" d="M12.315 2c2.43 0 2.784.013 3.808.06 1.064.049 1.791.218 2.427.465a4.902 4.902 0 011.772 1.153 4.902 4.902 0 011.153 1.772c.247.636.416 1.363.465 2.427.048 1.067.06 1.407.06 4.123v.08c0 2.643-.012 2.987-.06 4.043-.049 1.064-.218 1.791-.465 2.427a4.902 4.902 0 01-1.153 1.772 4.902 4.902 0 01-1.772 1.153c-.636.247-1.363.416-2.427.465-1.067.048-1.407.06-4.123.06h-.08c-2.643 0-2.987-.012-4.043-.06-1.064-.049-1.791-.218-2.427-.465a4.902 4.902 0 01-1.772-1.153 4.902 4.902 0 01-1.153-1.772c-.247-.636-.416-1.363-.465-2.427-.047-1.024-.06-1.379-.06-3.808v-.63c0-2.43.013-2.784.06-3.808.049-1.064.218-1.791.465-2.427a4.902 4.902 0 011.153-1.772A4.902 4.902 0 015.45 2.525c.636-.247 1.363-.416 2.427-.465C8.901 2.013 9.256 2 11.685 2h.63zm-.081 1.802h-.468c-2.456 0-2.784.011-3.807.058-.975.045-1.504.207-1.857.344-.467.182-.8.398-1.15.748-.35.35-.566.683-.748 1.15-.137.353-.3.882-.344 1.857-.047 1.023-.058 1.351-.058 3.807v.468c0 2.456.011 2.784.058 3.807.045.975.207 1.504.344 1.857.182.466.399.8.748 1.15.35.35.683.566 1.15.748.353.137.882.3 1.857.344 1.054.048 1.37.058 4.041.058h.08c2.597 0 2.917-.01 3.96-.058.976-.045 1.505-.207 1.858-.344.466-.182.8-.398 1.15-.748.35-.35.566-.683.748-1.15.137-.353.3-.882.344-1.857.048-1.055.058-1.37.058-4.041v-.08c0-2.597-.01-2.917-.058-3.96-.045-.976-.207-1.505-.344-1.858a3.097 3.097 0 00-.748-1.15 3.098 3.098 0 00-1.15-.748c-.353-.137-.882-.3-1.857-.344-1.023-.047-1.351-.058-3.807-.058zM12 6.865a5.135 5.135 0 110 10.27 5.135 5.135 0 010-10.27zm0 1.802a3.333 3.333 0 100 6.666 3.333 3.333 0 000-6.666zm5.338-3.205a1.2 1.2 0 110 2.4 1.2 1.2 0 010-2.4z" clipRule="evenodd" />
                </svg>
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-white font-semibold mb-4">Quick Links</h3>
            <ul className="space-y-2">
              <li>
                <Link to={ROUTES.PRODUCTS} className="text-sm hover:text-white transition-colors">
                  Products
                </Link>
              </li>
              <li>
                <Link to={ROUTES.ORDERS} className="text-sm hover:text-white transition-colors">
                  Orders
                </Link>
              </li>
              <li>
                <Link to="/about" className="text-sm hover:text-white transition-colors">
                  About Us
                </Link>
              </li>
              <li>
                <Link to="/contact" className="text-sm hover:text-white transition-colors">
                  Contact
                </Link>
              </li>
            </ul>
          </div>

          {/* Contact Info */}
          <div>
            <h3 className="text-white font-semibold mb-4">Contact Us</h3>
            <ul className="space-y-3">
              <li className="flex items-start space-x-2">
                <MapPin className="h-5 w-5 text-primary-400 flex-shrink-0 mt-0.5" />
                <span className="text-sm">123 Store Street, Sydney NSW 2000</span>
              </li>
              <li className="flex items-center space-x-2">
                <Phone className="h-5 w-5 text-primary-400 flex-shrink-0" />
                <span className="text-sm">+61 2 1234 5678</span>
              </li>
              <li className="flex items-center space-x-2">
                <Mail className="h-5 w-5 text-primary-400 flex-shrink-0" />
                <span className="text-sm">support@store.com</span>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="border-t border-gray-800 mt-8 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <p className="text-sm text-gray-400">
              &copy; {currentYear} Store. All rights reserved.
            </p>
            <div className="flex space-x-6 mt-4 md:mt-0">
              <Link to="/privacy" className="text-sm text-gray-400 hover:text-white transition-colors">
                Privacy Policy
              </Link>
              <Link to="/terms" className="text-sm text-gray-400 hover:text-white transition-colors">
                Terms of Service
              </Link>
              <Link to="/cookies" className="text-sm text-gray-400 hover:text-white transition-colors">
                Cookie Policy
              </Link>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};
```

**src/components/layout/Footer/index.ts:**
```typescript
export { Footer } from './Footer';
```

**Acceptance Criteria:**
- [ ] Footer component created
- [ ] Links work correctly
- [ ] Responsive design implemented
- [ ] Social media icons display
- [ ] Contact information shown

**Estimated Time:** 30 minutes

---

### Task 1.3: Create MainLayout Component

**Status:** ⬜ Not Started
**Depends On:** Tasks 1.1, 1.2

**Description:**
Create wrapper layout component that includes header and footer.

**Files to Create:**

**src/components/layout/MainLayout/MainLayout.tsx:**
```typescript
import React from 'react';
import { Header } from '../Header';
import { Footer } from '../Footer';

interface MainLayoutProps {
  children: React.ReactNode;
}

export const MainLayout: React.FC<MainLayoutProps> = ({ children }) => {
  return (
    <div className="flex flex-col min-h-screen">
      <Header />
      <main className="flex-grow bg-gray-50">
        {children}
      </main>
      <Footer />
    </div>
  );
};
```

**src/components/layout/MainLayout/index.ts:**
```typescript
export { MainLayout } from './MainLayout';
```

**src/components/layout/index.ts:**
```typescript
export { Header } from './Header';
export { Footer } from './Footer';
export { MainLayout } from './MainLayout';
```

**Acceptance Criteria:**
- [ ] MainLayout component created
- [ ] Header and Footer integrated
- [ ] Flex layout ensures footer stays at bottom
- [ ] Children render in main section

**Estimated Time:** 15 minutes

---

## Section 2: Authentication Pages

**Estimated Time:** 4-5 hours
**Dependencies:** Section 1 complete

### Task 2.1: Install Form Dependencies

**Status:** ⬜ Not Started
**Depends On:** Phase 1 complete

**Description:**
Install react-hook-form and zod for form handling and validation.

**Commands to Run:**
```bash
npm install react-hook-form zod @hookform/resolvers
```

**Acceptance Criteria:**
- [ ] Dependencies installed successfully
- [ ] No installation errors
- [ ] Package.json updated

**Estimated Time:** 5 minutes

---

### Task 2.2: Create Form Validation Schemas

**Status:** ⬜ Not Started
**Depends On:** Task 2.1

**Description:**
Create Zod schemas for form validation.

**Files to Create:**

**src/schemas/auth.schema.ts:**
```typescript
import { z } from 'zod';

export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(8, 'Password must be at least 8 characters'),
});

export const registerSchema = z.object({
  fullName: z
    .string()
    .min(1, 'Full name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters'),
  username: z
    .string()
    .min(1, 'Username is required')
    .min(3, 'Username must be at least 3 characters')
    .max(50, 'Username must be less than 50 characters')
    .regex(/^[a-zA-Z0-9_]+$/, 'Username can only contain letters, numbers, and underscores'),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number'),
  confirmPassword: z
    .string()
    .min(1, 'Please confirm your password'),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword'],
});

export const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
});

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;
```

**Acceptance Criteria:**
- [ ] Validation schemas created
- [ ] All fields have appropriate validation
- [ ] TypeScript types exported
- [ ] Password confirmation works

**Estimated Time:** 25 minutes

---

### Task 2.3: Create Login Page

**Status:** ⬜ Not Started
**Depends On:** Tasks 2.1, 2.2

**Description:**
Create login page with form validation and error handling.

**Files to Create:**

**src/pages/Login.tsx:**
```typescript
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { loginSchema, type LoginFormData } from '@/schemas/auth.schema';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ROUTES } from '@/config/routes';
import { ShoppingCart } from 'react-icons/lu';

const Login: React.FC = () => {
  const [error, setError] = useState<string>('');
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const from = location.state?.from?.pathname || ROUTES.HOME;

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormData) => {
    try {
      setError('');
      await login(data);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="max-w-md w-full" padding="lg">
        {/* Header */}
        <div className="text-center mb-8">
          <Link to={ROUTES.HOME} className="inline-flex items-center space-x-2 mb-4">
            <ShoppingCart className="h-10 w-10 text-primary-600" />
            <span className="text-2xl font-bold text-gray-900">Store</span>
          </Link>
          <h2 className="text-3xl font-bold text-gray-900">Welcome back</h2>
          <p className="mt-2 text-sm text-gray-600">
            Sign in to your account to continue
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {/* Login Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Email"
            type="email"
            placeholder="Enter your email"
            error={errors.email?.message}
            {...register('email')}
            fullWidth
          />

          <Input
            label="Password"
            type="password"
            placeholder="Enter your password"
            error={errors.password?.message}
            {...register('password')}
            fullWidth
          />

          <div className="flex items-center justify-between">
            <div className="flex items-center">
              <input
                id="remember-me"
                name="remember-me"
                type="checkbox"
                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-700">
                Remember me
              </label>
            </div>

            <Link
              to={ROUTES.FORGOT_PASSWORD}
              className="text-sm text-primary-600 hover:text-primary-700 font-medium"
            >
              Forgot password?
            </Link>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            isLoading={isSubmitting}
          >
            Sign in
          </Button>
        </form>

        {/* Sign Up Link */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Don't have an account?{' '}
            <Link
              to={ROUTES.REGISTER}
              className="font-medium text-primary-600 hover:text-primary-700"
            >
              Sign up
            </Link>
          </p>
        </div>
      </Card>
    </div>
  );
};

export default Login;
```

**Acceptance Criteria:**
- [ ] Login page created with form
- [ ] Form validation works
- [ ] Error messages display
- [ ] Loading state shows during submission
- [ ] Redirects after successful login
- [ ] Links to register and forgot password work

**Estimated Time:** 60 minutes

---

### Task 2.4: Create Register Page

**Status:** ⬜ Not Started
**Depends On:** Tasks 2.1, 2.2

**Description:**
Create registration page with password strength indicator.

**Files to Create:**

**src/components/auth/PasswordStrength/PasswordStrength.tsx:**
```typescript
import React from 'react';

interface PasswordStrengthProps {
  password: string;
}

export const PasswordStrength: React.FC<PasswordStrengthProps> = ({ password }) => {
  const calculateStrength = (pwd: string): { score: number; label: string; color: string } => {
    let score = 0;

    if (pwd.length >= 8) score++;
    if (pwd.length >= 12) score++;
    if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^a-zA-Z0-9]/.test(pwd)) score++;

    if (score <= 2) return { score, label: 'Weak', color: 'bg-red-500' };
    if (score === 3) return { score, label: 'Fair', color: 'bg-yellow-500' };
    if (score === 4) return { score, label: 'Good', color: 'bg-blue-500' };
    return { score, label: 'Strong', color: 'bg-green-500' };
  };

  if (!password) return null;

  const strength = calculateStrength(password);
  const widthPercentage = (strength.score / 5) * 100;

  return (
    <div className="mt-2">
      <div className="flex items-center justify-between mb-1">
        <span className="text-xs text-gray-600">Password strength:</span>
        <span className={`text-xs font-semibold ${strength.score <= 2 ? 'text-red-600' : strength.score === 3 ? 'text-yellow-600' : strength.score === 4 ? 'text-blue-600' : 'text-green-600'}`}>
          {strength.label}
        </span>
      </div>
      <div className="h-2 bg-gray-200 rounded-full overflow-hidden">
        <div
          className={`h-full ${strength.color} transition-all duration-300`}
          style={{ width: `${widthPercentage}%` }}
        />
      </div>
    </div>
  );
};
```

**src/components/auth/PasswordStrength/index.ts:**
```typescript
export { PasswordStrength } from './PasswordStrength';
```

**src/pages/Register.tsx:**
```typescript
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/context/AuthContext';
import { registerSchema, type RegisterFormData } from '@/schemas/auth.schema';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { PasswordStrength } from '@/components/auth/PasswordStrength';
import { ROUTES } from '@/config/routes';
import { ShoppingCart } from 'react-icons/lu';

const Register: React.FC = () => {
  const [error, setError] = useState<string>('');
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
  });

  const password = watch('password');

  const onSubmit = async (data: RegisterFormData) => {
    try {
      setError('');
      const { confirmPassword, ...registerData } = data;
      await registerUser(registerData);
      navigate(ROUTES.HOME);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Registration failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="max-w-md w-full" padding="lg">
        {/* Header */}
        <div className="text-center mb-8">
          <Link to={ROUTES.HOME} className="inline-flex items-center space-x-2 mb-4">
            <ShoppingCart className="h-10 w-10 text-primary-600" />
            <span className="text-2xl font-bold text-gray-900">Store</span>
          </Link>
          <h2 className="text-3xl font-bold text-gray-900">Create account</h2>
          <p className="mt-2 text-sm text-gray-600">
            Join us and start shopping today
          </p>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {/* Register Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <Input
            label="Full Name"
            type="text"
            placeholder="Enter your full name"
            error={errors.fullName?.message}
            {...register('fullName')}
            fullWidth
          />

          <Input
            label="Username"
            type="text"
            placeholder="Choose a username"
            error={errors.username?.message}
            {...register('username')}
            fullWidth
          />

          <Input
            label="Email"
            type="email"
            placeholder="Enter your email"
            error={errors.email?.message}
            {...register('email')}
            fullWidth
          />

          <div>
            <Input
              label="Password"
              type="password"
              placeholder="Create a password"
              error={errors.password?.message}
              {...register('password')}
              fullWidth
            />
            <PasswordStrength password={password || ''} />
          </div>

          <Input
            label="Confirm Password"
            type="password"
            placeholder="Confirm your password"
            error={errors.confirmPassword?.message}
            {...register('confirmPassword')}
            fullWidth
          />

          <div className="flex items-start">
            <input
              id="terms"
              name="terms"
              type="checkbox"
              required
              className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded mt-1"
            />
            <label htmlFor="terms" className="ml-2 block text-sm text-gray-700">
              I agree to the{' '}
              <Link to="/terms" className="text-primary-600 hover:text-primary-700">
                Terms of Service
              </Link>{' '}
              and{' '}
              <Link to="/privacy" className="text-primary-600 hover:text-primary-700">
                Privacy Policy
              </Link>
            </label>
          </div>

          <Button
            type="submit"
            variant="primary"
            size="lg"
            fullWidth
            isLoading={isSubmitting}
          >
            Create account
          </Button>
        </form>

        {/* Sign In Link */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Already have an account?{' '}
            <Link
              to={ROUTES.LOGIN}
              className="font-medium text-primary-600 hover:text-primary-700"
            >
              Sign in
            </Link>
          </p>
        </div>
      </Card>
    </div>
  );
};

export default Register;
```

**Acceptance Criteria:**
- [ ] Register page created with form
- [ ] All form fields validated
- [ ] Password strength indicator works
- [ ] Password confirmation validation works
- [ ] Error messages display
- [ ] Terms checkbox required
- [ ] Redirects after successful registration

**Estimated Time:** 75 minutes

---

### Task 2.5: Create Forgot Password Page

**Status:** ⬜ Not Started
**Depends On:** Tasks 2.1, 2.2

**Description:**
Create forgot password page for password reset requests.

**Files to Create:**

**src/pages/ForgotPassword.tsx:**
```typescript
import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Link } from 'react-router-dom';
import { forgotPasswordSchema, type ForgotPasswordFormData } from '@/schemas/auth.schema';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { ROUTES } from '@/config/routes';
import { ShoppingCart, Mail } from 'react-icons/lu';

const ForgotPassword: React.FC = () => {
  const [error, setError] = useState<string>('');
  const [success, setSuccess] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
  });

  const onSubmit = async (data: ForgotPasswordFormData) => {
    try {
      setError('');
      // TODO: Implement forgot password API call
      console.log('Forgot password request:', data);

      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      setSuccess(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Request failed');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <Card className="max-w-md w-full" padding="lg">
        {/* Header */}
        <div className="text-center mb-8">
          <Link to={ROUTES.HOME} className="inline-flex items-center space-x-2 mb-4">
            <ShoppingCart className="h-10 w-10 text-primary-600" />
            <span className="text-2xl font-bold text-gray-900">Store</span>
          </Link>
          <h2 className="text-3xl font-bold text-gray-900">Reset password</h2>
          <p className="mt-2 text-sm text-gray-600">
            Enter your email and we'll send you a reset link
          </p>
        </div>

        {success ? (
          <div className="text-center">
            <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-100 mb-4">
              <Mail className="h-8 w-8 text-green-600" />
            </div>
            <h3 className="text-lg font-semibold text-gray-900 mb-2">Check your email</h3>
            <p className="text-sm text-gray-600 mb-6">
              We've sent password reset instructions to your email address.
            </p>
            <Link to={ROUTES.LOGIN}>
              <Button variant="primary" fullWidth>
                Back to login
              </Button>
            </Link>
          </div>
        ) : (
          <>
            {/* Error Message */}
            {error && (
              <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                <p className="text-sm text-red-600">{error}</p>
              </div>
            )}

            {/* Forgot Password Form */}
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <Input
                label="Email"
                type="email"
                placeholder="Enter your email"
                error={errors.email?.message}
                {...register('email')}
                fullWidth
              />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                isLoading={isSubmitting}
              >
                Send reset link
              </Button>
            </form>

            {/* Back to Login Link */}
            <div className="mt-6 text-center">
              <Link
                to={ROUTES.LOGIN}
                className="text-sm text-primary-600 hover:text-primary-700 font-medium"
              >
                Back to login
              </Link>
            </div>
          </>
        )}
      </Card>
    </div>
  );
};

export default ForgotPassword;
```

**Acceptance Criteria:**
- [ ] Forgot password page created
- [ ] Email validation works
- [ ] Success state displays
- [ ] Error handling works
- [ ] Links to login page

**Estimated Time:** 40 minutes

---

## Section 3: Navigation System

**Estimated Time:** 2-3 hours
**Dependencies:** Sections 1 and 2 complete

### Task 3.1: Update App.tsx with New Routes

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Update App.tsx to include new routes with MainLayout.

**File to Modify:**

**src/App.tsx:**
```typescript
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { AuthProvider } from '@/context/AuthContext';
import { MainLayout } from '@/components/layout';
import { ProtectedRoute } from '@/components/common/ProtectedRoute';
import { ROUTES } from '@/config/routes';
import './App.css';

// Pages
import Home from '@/pages/Home';
import About from '@/pages/About';
import Contact from '@/pages/Contact';
import Login from '@/pages/Login';
import Register from '@/pages/Register';
import ForgotPassword from '@/pages/ForgotPassword';
import NotFound from '@/pages/NotFound';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Public Routes with Layout */}
          <Route
            path={ROUTES.HOME}
            element={
              <MainLayout>
                <Home />
              </MainLayout>
            }
          />
          <Route
            path="/about"
            element={
              <MainLayout>
                <About />
              </MainLayout>
            }
          />
          <Route
            path="/contact"
            element={
              <MainLayout>
                <Contact />
              </MainLayout>
            }
          />

          {/* Auth Routes (No Layout) */}
          <Route path={ROUTES.LOGIN} element={<Login />} />
          <Route path={ROUTES.REGISTER} element={<Register />} />
          <Route path={ROUTES.FORGOT_PASSWORD} element={<ForgotPassword />} />

          {/* Protected Routes with Layout (Placeholder for Phase 3+) */}
          <Route
            path={ROUTES.PRODUCTS}
            element={
              <MainLayout>
                <div className="p-8 text-center">Products Page (Coming in Phase 3)</div>
              </MainLayout>
            }
          />
          <Route
            path={ROUTES.ORDERS}
            element={
              <ProtectedRoute>
                <MainLayout>
                  <div className="p-8 text-center">Orders Page (Coming in Phase 5)</div>
                </MainLayout>
              </ProtectedRoute>
            }
          />
          <Route
            path={ROUTES.PROFILE}
            element={
              <ProtectedRoute>
                <MainLayout>
                  <div className="p-8 text-center">Profile Page (Coming in Phase 6)</div>
                </MainLayout>
              </ProtectedRoute>
            }
          />

          {/* 404 Not Found */}
          <Route
            path="*"
            element={
              <MainLayout>
                <NotFound />
              </MainLayout>
            }
          />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
```

**Acceptance Criteria:**
- [ ] All routes configured
- [ ] MainLayout applied to appropriate routes
- [ ] Auth routes exclude layout
- [ ] ProtectedRoute wrapper works
- [ ] 404 page has layout

**Estimated Time:** 30 minutes

---

### Task 3.2: Create Breadcrumb Component

**Status:** ⬜ Not Started
**Depends On:** Task 3.1

**Description:**
Create breadcrumb navigation component.

**Files to Create:**

**src/components/common/Breadcrumb/Breadcrumb.tsx:**
```typescript
import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ChevronRight, Home } from 'react-icons/lu';
import { ROUTES } from '@/config/routes';

interface BreadcrumbItem {
  label: string;
  path: string;
}

export const Breadcrumb: React.FC = () => {
  const location = useLocation();

  const pathnames = location.pathname.split('/').filter((x) => x);

  const breadcrumbNameMap: Record<string, string> = {
    'products': 'Products',
    'cart': 'Shopping Cart',
    'checkout': 'Checkout',
    'orders': 'Orders',
    'profile': 'Profile',
    'settings': 'Settings',
    'about': 'About',
    'contact': 'Contact',
  };

  if (pathnames.length === 0) {
    return null;
  }

  const breadcrumbs: BreadcrumbItem[] = [
    { label: 'Home', path: ROUTES.HOME },
  ];

  let currentPath = '';
  pathnames.forEach((segment) => {
    currentPath += `/${segment}`;
    breadcrumbs.push({
      label: breadcrumbNameMap[segment] || segment,
      path: currentPath,
    });
  });

  return (
    <nav className="flex items-center space-x-2 text-sm text-gray-600 mb-4">
      {breadcrumbs.map((breadcrumb, index) => {
        const isLast = index === breadcrumbs.length - 1;

        return (
          <React.Fragment key={breadcrumb.path}>
            {index === 0 ? (
              <Link
                to={breadcrumb.path}
                className="flex items-center hover:text-primary-600 transition-colors"
              >
                <Home className="h-4 w-4" />
              </Link>
            ) : (
              <>
                <ChevronRight className="h-4 w-4 text-gray-400" />
                {isLast ? (
                  <span className="font-medium text-gray-900">{breadcrumb.label}</span>
                ) : (
                  <Link
                    to={breadcrumb.path}
                    className="hover:text-primary-600 transition-colors"
                  >
                    {breadcrumb.label}
                  </Link>
                )}
              </>
            )}
          </React.Fragment>
        );
      })}
    </nav>
  );
};
```

**src/components/common/Breadcrumb/index.ts:**
```typescript
export { Breadcrumb } from './Breadcrumb';
```

**Acceptance Criteria:**
- [ ] Breadcrumb component created
- [ ] Path segments displayed correctly
- [ ] Links work for all except current page
- [ ] Home icon displayed
- [ ] Responsive design

**Estimated Time:** 30 minutes

---

### Task 3.3: Create Loading Component for Route Transitions

**Status:** ⬜ Not Started
**Depends On:** Phase 1 complete

**Description:**
Create page loading component for route transitions.

**Files to Create:**

**src/components/common/PageLoader/PageLoader.tsx:**
```typescript
import React from 'react';
import { Spinner } from '@/components/ui/Spinner';

export const PageLoader: React.FC = () => {
  return (
    <div className="fixed inset-0 bg-white bg-opacity-90 z-50 flex items-center justify-center">
      <div className="text-center">
        <Spinner size="lg" />
        <p className="mt-4 text-gray-600">Loading...</p>
      </div>
    </div>
  );
};
```

**src/components/common/PageLoader/index.ts:**
```typescript
export { PageLoader } from './PageLoader';
```

**Acceptance Criteria:**
- [ ] PageLoader component created
- [ ] Full-screen overlay
- [ ] Centered spinner and text
- [ ] Semi-transparent background

**Estimated Time:** 15 minutes

---

## Testing & Validation

### Task 4.1: Manual Testing Checklist

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Manually verify all Phase 2 functionality.

**Testing Steps:**

1. **Layout Components:**
   - [ ] Header displays correctly on all pages
   - [ ] Mobile menu works on small screens
   - [ ] User menu dropdown toggles correctly
   - [ ] Cart icon shows (when authenticated)
   - [ ] Footer displays with all links
   - [ ] Layout maintains correct structure (header, content, footer)

2. **Authentication Pages:**
   - [ ] Login page displays and form works
   - [ ] Form validation shows errors
   - [ ] Can submit login form
   - [ ] Register page displays with all fields
   - [ ] Password strength indicator works
   - [ ] Password confirmation validation works
   - [ ] Forgot password page works
   - [ ] Success state shows after submission

3. **Navigation:**
   - [ ] All routes accessible
   - [ ] Protected routes redirect to login
   - [ ] Breadcrumb displays on appropriate pages
   - [ ] Navigation links highlight current page
   - [ ] Login/logout flow works
   - [ ] Redirect to intended page after login

4. **Responsive Design:**
   - [ ] Test on mobile (< 640px)
   - [ ] Test on tablet (768px - 1024px)
   - [ ] Test on desktop (> 1024px)
   - [ ] Mobile menu works correctly
   - [ ] All pages are responsive

5. **Cross-Browser:**
   - [ ] Test on Chrome
   - [ ] Test on Firefox
   - [ ] Test on Safari
   - [ ] Test on Edge

**Estimated Time:** 60 minutes

---

### Task 4.2: Run Validation Checks

**Status:** ⬜ Not Started
**Depends On:** Task 4.1

**Description:**
Run TypeScript and ESLint checks to ensure code quality.

**Commands to Run:**
```bash
npm run type-check
npm run lint
npm run build
```

**Acceptance Criteria:**
- [ ] No TypeScript errors
- [ ] No ESLint errors
- [ ] Build succeeds
- [ ] All imports resolve correctly

**Estimated Time:** 15 minutes

---

### Task 4.3: Create Phase 2 Completion Report

**Status:** ⬜ Not Started
**Depends On:** Tasks 4.1, 4.2

**Description:**
Document Phase 2 completion and any issues.

**Create file:** `tasks/frontend/phase-2-completion-report.md`

**Contents:**
- Date completed
- All tasks completed (checkboxes)
- Any deviations from plan
- Issues encountered and solutions
- Screenshots of key pages
- Recommendations for Phase 3
- Known issues or technical debt

**Acceptance Criteria:**
- [ ] Report created
- [ ] All tasks documented
- [ ] Screenshots included
- [ ] Ready to begin Phase 3

**Estimated Time:** 30 minutes

---

## Summary

### Total Tasks: 15
### Estimated Total Time: 10-12 hours
### Priority: HIGH
### Prerequisites: Phase 1 must be completed first

### Deliverables Checklist:

**Layout Components:**
- [ ] Header with navigation and user menu
- [ ] Footer with links and contact info
- [ ] MainLayout wrapper component
- [ ] Responsive mobile menu

**Authentication Pages:**
- [ ] Login page with form validation
- [ ] Register page with password strength
- [ ] Forgot password page
- [ ] Form validation schemas (Zod)

**Navigation System:**
- [ ] Updated App.tsx with all routes
- [ ] Protected routes configured
- [ ] Breadcrumb component
- [ ] Page loading component

**Additional:**
- [ ] Form dependencies installed (react-hook-form, zod)
- [ ] All pages responsive
- [ ] TypeScript validation passing
- [ ] ESLint validation passing

---

## Next Steps

After completing Phase 2:
1. Review this document and mark all tasks complete
2. Create Phase 2 completion report with screenshots
3. Test all authentication flows
4. Verify responsive design on multiple devices
5. Proceed to `phase-3-product-catalog.md`
6. Begin implementing product listing and detail pages

---

**Document Created:** October 2025
**Phase Status:** Not Started
**Target Completion:** 3-4 days from start
**Dependencies:** Phase 1 must be complete
