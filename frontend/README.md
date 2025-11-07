# Frontend - Online Store Application

**COMP5348 – Enterprise-Scale Software Development**
**The University of Sydney**

A modern, responsive React single-page application (SPA) for the Online Store system, built with TypeScript, Vite, and Tailwind CSS.

---

## Table of Contents

1. [Overview](#overview)
2. [Tech Stack](#tech-stack)
3. [Quick Start](#quick-start)
4. [Environment Variables](#environment-variables)
5. [Available Scripts](#available-scripts)
6. [Project Structure](#project-structure)
7. [Development Workflow](#development-workflow)
8. [Key Features](#key-features)
9. [Documentation](#documentation)
10. [Code Quality](#code-quality)

---

## Overview

This frontend application provides a comprehensive e-commerce interface for customers to browse products, manage their shopping cart, place orders, and track order status. It communicates with a Spring Boot backend via RESTful APIs and implements modern React patterns for state management, routing, and component composition.

### Key Highlights

- **Framework**: React 19 with TypeScript for type safety
- **Build Tool**: Vite for fast development and optimized builds
- **Styling**: Tailwind CSS for utility-first styling
- **State Management**: Context API and Zustand for client state
- **Forms**: React Hook Form with Zod validation
- **Testing**: Vitest with React Testing Library
- **Code Quality**: ESLint, Prettier, and TypeScript strict mode

---

## Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Framework** | React | 19.1.1 |
| **Language** | TypeScript | 5.9.3 |
| **Build Tool** | Vite | 7.1.7 |
| **Routing** | React Router | 6.30.1 |
| **Styling** | Tailwind CSS | 3.4.1 |
| **State Management** | Zustand | 5.0.8 |
| **HTTP Client** | Axios | 1.12.2 |
| **Forms** | React Hook Form | 7.65.0 |
| **Validation** | Zod | 4.1.12 |
| **UI Components** | Radix UI | Various |
| **Testing** | Vitest | 3.2.4 |
| **Linting** | ESLint | 9.36.0 |
| **Formatting** | Prettier | 3.6.2 |

---

## Quick Start

### Prerequisites

- Node.js v22+ (we recommend using `nvm`)
- npm 10+ or pnpm 9+

### Installation

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Copy environment template
cp .env.example .env.local

# Edit .env.local with your configuration
# (Optional) Adjust API base URL if backend is on a different port

# Start development server
npm run dev
```

The application will be available at `http://localhost:3000`

---

## Environment Variables

Create a `.env.local` file in the frontend directory with the following variables:

```env
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=15000

# App Configuration
VITE_APP_NAME=Online Store
VITE_APP_VERSION=1.0.0

# Port Configuration
VITE_PORT=3000
```

### Variable Descriptions

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080/api` |
| `VITE_API_TIMEOUT` | API request timeout (ms) | `15000` |
| `VITE_APP_NAME` | Application name | `Online Store` |
| `VITE_APP_VERSION` | Application version | `1.0.0` |
| `VITE_PORT` | Development server port | `3000` |

**Note**: All environment variables must be prefixed with `VITE_` to be accessible in the application.

---

## Available Scripts

### Development

```bash
npm run dev
```
Starts the Vite development server with Hot Module Replacement (HMR) at `http://localhost:3000`

### Build

```bash
npm run build
```
Runs TypeScript type checking and builds the application for production. Output is in the `dist/` directory.

### Preview

```bash
npm run preview
```
Locally preview the production build before deploying.

### Linting

```bash
# Check for linting errors
npm run lint

# Auto-fix linting errors
npm run lint:fix
```

### Formatting

```bash
# Format code with Prettier
npm run format

# Check code formatting
npm run format:check
```

### Type Checking

```bash
npm run type-check
```
Runs TypeScript compiler in check mode without emitting files.

### Testing

```bash
# Run tests
npm run test

# Run tests with UI
npm run test:ui

# Generate coverage report
npm run test:coverage
```

---

## Project Structure

```
frontend/
├── public/                      # Static assets
│   └── vite.svg
│
├── src/
│   ├── api/                     # API integration layer
│   │   └── services/           # API service modules
│   │
│   ├── assets/                  # Images, icons, fonts
│   │   ├── images/
│   │   ├── icons/
│   │   └── logos/
│   │
│   ├── components/              # React components
│   │   ├── ui/                 # Base UI components (Button, Input, Card, etc.)
│   │   ├── layout/             # Layout components (Header, Footer, etc.)
│   │   ├── features/           # Feature-specific components
│   │   └── common/             # Common shared components
│   │
│   ├── pages/                   # Page components / Route containers
│   │   ├── Home.tsx
│   │   ├── About.tsx
│   │   ├── Contact.tsx
│   │   └── NotFound.tsx
│   │
│   ├── hooks/                   # Custom React hooks
│   │
│   ├── context/                 # React Context providers
│   │
│   ├── types/                   # TypeScript type definitions
│   │   ├── api.types.ts
│   │   ├── common.types.ts
│   │   ├── user.types.ts
│   │   └── index.ts
│   │
│   ├── utils/                   # Utility functions
│   │
│   ├── lib/                     # Third-party library configurations
│   │
│   ├── config/                  # App configuration files
│   │
│   ├── styles/                  # Global styles
│   │
│   ├── App.tsx                  # Root component
│   ├── main.tsx                 # Application entry point
│   └── index.css                # Global CSS
│
├── .env.example                 # Environment variables template
├── .env.local                   # Local environment variables (git-ignored)
├── .gitignore
├── eslint.config.js            # ESLint configuration
├── index.html                   # HTML template
├── package.json                 # Dependencies and scripts
├── postcss.config.js           # PostCSS configuration
├── tailwind.config.js          # Tailwind CSS configuration
├── tsconfig.json               # TypeScript configuration
├── tsconfig.app.json           # TypeScript app configuration
├── tsconfig.node.json          # TypeScript node configuration
├── vite.config.ts              # Vite configuration
└── README.md                    # This file
```

### Path Aliases

The project uses path aliases for cleaner imports:

```typescript
import { Button } from '@/components/ui/Button'
import { useAuth } from '@/hooks/useAuth'
import { User } from '@/types/user.types'
```

Available aliases:
- `@/` → `src/`
- `@/components` → `src/components`
- `@/pages` → `src/pages`
- `@/hooks` → `src/hooks`
- `@/utils` → `src/utils`
- `@/types` → `src/types`
- `@/api` → `src/api`
- `@/config` → `src/config`
- `@/lib` → `src/lib`
- `@/context` → `src/context`
- `@/assets` → `src/assets`
- `@/styles` → `src/styles`

---

## Development Workflow

### 1. Start Development Server

```bash
npm run dev
```

### 2. Create Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### 3. Develop Your Feature

- Follow the [Development Standards](../docs/frontend/DEVELOPMENT_STANDARDS.md)
- Use components from the [UI Design System](../docs/frontend/UI_DESIGN_SYSTEM.md)
- Integrate with backend using [API Integration Guide](../docs/frontend/API_INTEGRATION.md)
- Write tests for your components

### 4. Run Quality Checks

```bash
# Type checking
npm run type-check

# Linting
npm run lint

# Format code
npm run format

# Run tests
npm run test
```

### 5. Build for Production

```bash
npm run build
```

### 6. Submit Pull Request

- Ensure all checks pass
- Follow the PR template
- Request code review

---

## Key Features

### User Interface
- Responsive design (mobile, tablet, desktop)
- Accessible components (WCAG 2.1 Level AA)
- Dark mode support (planned)
- Smooth animations and transitions

### User Features
- User authentication (login, register)
- Product browsing with search and filters
- Shopping cart management
- Checkout flow
- Order history and tracking
- Order cancellation
- User profile management

### Technical Features
- Type-safe API integration
- Optimistic UI updates
- Error boundary for graceful error handling
- Loading states and skeletons
- Form validation with Zod schemas
- Protected routes
- JWT token management
- Persistent cart state

---

## Documentation

### Comprehensive Documentation

For detailed documentation, see the [`docs/frontend/`](../docs/frontend/) directory:

- **[README.md](../docs/frontend/README.md)** - Documentation index and quick reference
- **[ARCHITECTURE.md](../docs/frontend/ARCHITECTURE.md)** - Technical architecture and patterns
- **[UI_DESIGN_SYSTEM.md](../docs/frontend/UI_DESIGN_SYSTEM.md)** - Design system and component library
- **[API_INTEGRATION.md](../docs/frontend/API_INTEGRATION.md)** - API endpoints and integration guide
- **[DEVELOPMENT_STANDARDS.md](../docs/frontend/DEVELOPMENT_STANDARDS.md)** - Coding standards and best practices
- **[IMPLEMENTATION_PLAN.md](../docs/frontend/IMPLEMENTATION_PLAN.md)** - Implementation roadmap and timeline

### External Resources

- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Vite Documentation](https://vite.dev/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [React Router Documentation](https://reactrouter.com/)
- [Zustand Documentation](https://zustand-demo.pmnd.rs/)

---

## Code Quality

### Type Safety

This project uses TypeScript in strict mode. All code must be type-safe:

```typescript
// Good
const user: User = { id: '1', name: 'John', email: 'john@example.com' }

// Bad - will not compile
const user = { id: 1, name: 'John' } // Missing email field
```

### Linting

ESLint is configured with React and TypeScript rules. Run linting before committing:

```bash
npm run lint
```

### Formatting

Prettier is configured for consistent code formatting:

```bash
npm run format
```

### Testing

Write tests for components, hooks, and utilities:

```typescript
// Button.test.tsx
import { render, screen, fireEvent } from '@testing-library/react'
import { Button } from './Button'

describe('Button', () => {
  it('renders correctly', () => {
    render(<Button>Click me</Button>)
    expect(screen.getByText('Click me')).toBeInTheDocument()
  })

  it('calls onClick when clicked', () => {
    const handleClick = jest.fn()
    render(<Button onClick={handleClick}>Click me</Button>)
    fireEvent.click(screen.getByText('Click me'))
    expect(handleClick).toHaveBeenCalledTimes(1)
  })
})
```

---

## Troubleshooting

### Port Already in Use

If port 3000 is already in use, you can change it in `.env.local`:

```env
VITE_PORT=3001
```

### API Connection Issues

Ensure the backend is running on the correct port. Check `VITE_API_BASE_URL` in `.env.local`.

### TypeScript Errors

Run type checking to see all errors:

```bash
npm run type-check
```

### Build Errors

Clear the cache and rebuild:

```bash
rm -rf node_modules dist .vite
npm install
npm run build
```

---

## Contributing

1. Follow the [Development Standards](../docs/frontend/DEVELOPMENT_STANDARDS.md)
2. Write meaningful commit messages
3. Add tests for new features
4. Update documentation as needed
5. Submit pull requests for review

---

## License

This project is part of the COMP5348 course at The University of Sydney.

---

**Last Updated:** October 2025
**Maintained By:** Frontend Team
