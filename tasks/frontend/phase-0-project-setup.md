# Phase 0: Project Setup - Implementation Tasks
## Online Store Application - Frontend

**Phase Duration:** 1-2 days
**Priority:** CRITICAL
**Status:** Not Started
**Last Updated:** October 2025

---

## Overview

This document contains the initial setup tasks required before starting actual feature development. Phase 0 focuses on establishing the development environment, installing dependencies, and creating the basic project structure.

### Phase 0 Goals
- ✅ Install and configure all required dependencies
- ✅ Set up development tooling (Tailwind CSS, TypeScript, etc.)
- ✅ Create core project folder structure
- ✅ Configure TypeScript path aliases
- ✅ Create base type definitions

### Why Phase 0?

Phase 0 is the foundation that everything else builds upon. Completing this phase ensures:
- All team members have the same development environment
- Dependencies are properly configured before writing code
- Project structure is in place for organized development
- TypeScript configuration is correct from the start

---

## Table of Contents

1. [Section 1: Dependencies Installation](#section-1-dependencies-installation)
2. [Section 2: Basic Project Structure](#section-2-basic-project-structure)
3. [Verification & Next Steps](#verification--next-steps)

---

## Section 1: Dependencies Installation

**Estimated Time:** 2-3 hours
**Dependencies:** None (starting fresh)

### Task 0.1: Install Tailwind CSS

**Status:** ⬜ Not Started

**Description:**
Install and configure Tailwind CSS for styling the application.

**Commands:**
```bash
cd frontend
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

**Files to Modify:**
- `tailwind.config.js` - Configure content paths, theme, plugins
- `postcss.config.js` - Configure PostCSS plugins

**Acceptance Criteria:**
- [ ] Tailwind CSS installed successfully
- [ ] `tailwind.config.js` created with proper content paths
- [ ] PostCSS configured correctly
- [ ] Test by using Tailwind classes in a component

**Estimated Time:** 30 minutes

---

### Task 0.2: Configure Tailwind Content Paths

**Status:** ⬜ Not Started
**Depends On:** Task 0.1

**Description:**
Configure Tailwind to scan the correct files for classes.

**Configuration:**
```javascript
// tailwind.config.js
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
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
      },
    },
  },
  plugins: [],
}
```

**Acceptance Criteria:**
- [ ] Content paths include all component files
- [ ] Tailwind processes classes correctly
- [ ] No unused CSS in production build
- [ ] Primary color theme configured

**Estimated Time:** 15 minutes

---

### Task 0.3: Set Up Tailwind Base Styles

**Status:** ⬜ Not Started
**Depends On:** Task 0.2

**Description:**
Import Tailwind directives in main CSS file.

**File to Create/Modify:**

**src/index.css:**
```css
@tailwind base;
@tailwind components;
@tailwind utilities;

/* Custom base styles */
@layer base {
  html {
    @apply antialiased;
  }

  body {
    @apply bg-gray-50 text-gray-900;
  }

  * {
    @apply border-gray-200;
  }
}

/* Custom component styles */
@layer components {
  .btn {
    @apply px-4 py-2 rounded-lg font-medium transition-colors;
  }
}

/* Custom utility styles */
@layer utilities {
  .text-balance {
    text-wrap: balance;
  }
}
```

**Acceptance Criteria:**
- [ ] Tailwind directives imported
- [ ] Base styles applied globally
- [ ] Typography looks clean and readable
- [ ] Styles imported in main.tsx

**Estimated Time:** 15 minutes

---

### Task 0.4: Install UI Component Library (react-icons for icons)

**Status:** ⬜ Not Started

**Description:**
Install icon library for UI components.

**Commands:**
```bash
npm install react-icons
```

**Optional (if you want to use shadcn/ui):**
```bash
# Initialize shadcn/ui
npx shadcn-ui@latest init
```

**Acceptance Criteria:**
- [ ] Icon library installed
- [ ] Can import and use icons in components
- [ ] (Optional) shadcn/ui initialized and configured

**Estimated Time:** 20 minutes

---

### Task 0.5: Install Form Libraries

**Status:** ⬜ Not Started

**Description:**
Install React Hook Form and Zod for form handling and validation.

**Commands:**
```bash
npm install react-hook-form zod @hookform/resolvers
```

**Acceptance Criteria:**
- [ ] react-hook-form installed
- [ ] zod installed for schema validation
- [ ] @hookform/resolvers installed for zod integration

**Estimated Time:** 10 minutes

---

### Task 0.6: Install HTTP Client (Axios)

**Status:** ⬜ Not Started

**Description:**
Install Axios for making HTTP requests to the backend API.

**Commands:**
```bash
npm install axios
```

**Acceptance Criteria:**
- [ ] Axios installed successfully
- [ ] Can import axios in files

**Estimated Time:** 5 minutes

---

### Task 0.7: Install State Management (Zustand)

**Status:** ⬜ Not Started

**Description:**
Install Zustand for lightweight state management.

**Commands:**
```bash
npm install zustand
```

**Optional (if using React Query for server state):**
```bash
npm install @tanstack/react-query
```

**Acceptance Criteria:**
- [ ] Zustand installed
- [ ] (Optional) React Query installed for server state
- [ ] Can create stores

**Estimated Time:** 10 minutes

---

### Task 0.8: Install Date Utilities

**Status:** ⬜ Not Started

**Description:**
Install date-fns for date formatting and manipulation.

**Commands:**
```bash
npm install date-fns
```

**Acceptance Criteria:**
- [ ] date-fns installed
- [ ] Can import and use date functions

**Estimated Time:** 5 minutes

---

### Task 0.9: Install Development Tools

**Status:** ⬜ Not Started

**Description:**
Install development and testing tools.

**Commands:**
```bash
# Testing libraries
npm install -D vitest @testing-library/react @testing-library/jest-dom @testing-library/user-event

# Type definitions
npm install -D @types/node
```

**Acceptance Criteria:**
- [ ] Vitest installed for testing
- [ ] React Testing Library installed
- [ ] Type definitions for Node.js available

**Estimated Time:** 15 minutes

---

### Task 0.10: Configure Environment Variables

**Status:** ⬜ Not Started

**Description:**
Set up environment variable files for different environments.

**Files to Create:**

**frontend/.env.example:**
```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=15000

# App Configuration
VITE_APP_NAME=Online Store
VITE_APP_VERSION=1.0.0
```

**frontend/.env.local:**
```bash
# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api
VITE_API_TIMEOUT=15000

# App Configuration
VITE_APP_NAME=Online Store
VITE_APP_VERSION=1.0.0
```

**Update .gitignore:**
```bash
# Environment variables
.env.local
.env.*.local
```

**Acceptance Criteria:**
- [ ] `.env.example` created with all required variables
- [ ] `.env.local` created with development values
- [ ] `.env.local` added to `.gitignore`
- [ ] Can access env vars with `import.meta.env.VITE_*`

**Estimated Time:** 10 minutes

---

### Task 0.11: Update package.json Scripts

**Status:** ⬜ Not Started

**Description:**
Add useful npm scripts for development workflow.

**File to Modify:**

**frontend/package.json:**
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "eslint .",
    "lint:fix": "eslint . --fix",
    "format": "prettier --write \"src/**/*.{ts,tsx,css,md}\"",
    "format:check": "prettier --check \"src/**/*.{ts,tsx,css,md}\"",
    "type-check": "tsc --noEmit",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage"
  }
}
```

**Acceptance Criteria:**
- [ ] All scripts added to package.json
- [ ] Scripts run without errors
- [ ] Team members can use standard commands

**Estimated Time:** 10 minutes

---

### Task 0.12: Verify All Dependencies

**Status:** ⬜ Not Started
**Depends On:** Tasks 0.1-0.11

**Description:**
Verify all dependencies are installed correctly.

**Commands:**
```bash
cd frontend
npm install
npm run type-check
npm run lint
```

**Acceptance Criteria:**
- [ ] `npm install` completes without errors
- [ ] No dependency conflicts
- [ ] Type checking passes
- [ ] Linting passes (or only expected warnings)
- [ ] Dev server starts with `npm run dev`

**Estimated Time:** 15 minutes

---

## Section 2: Basic Project Structure

**Estimated Time:** 1-2 hours
**Dependencies:** Section 1 complete

### Task 0.13: Create Core Directory Structure

**Status:** ⬜ Not Started
**Depends On:** Task 0.12

**Description:**
Create the main folder structure for the application.

**Commands:**
```bash
cd frontend/src

# Create main directories
mkdir -p api/services
mkdir -p assets/images assets/icons assets/logos
mkdir -p components/ui components/layout components/features components/common
mkdir -p pages
mkdir -p hooks
mkdir -p context
mkdir -p types
mkdir -p utils
mkdir -p styles
mkdir -p config
mkdir -p lib
```

**Directory Structure to Create:**
```
src/
├── api/
│   └── services/
├── assets/
│   ├── images/
│   ├── icons/
│   └── logos/
├── components/
│   ├── ui/
│   ├── layout/
│   ├── features/
│   └── common/
├── pages/
├── hooks/
├── context/
├── types/
├── utils/
├── styles/
├── config/
└── lib/
```

**Create .gitkeep files:**
```bash
# Keep empty directories in git
find src -type d -empty -exec touch {}/.gitkeep \;
```

**Acceptance Criteria:**
- [ ] All directories created
- [ ] Directory structure matches architecture plan
- [ ] Directories visible in IDE
- [ ] .gitkeep files in empty directories

**Estimated Time:** 10 minutes

---

### Task 0.14: Configure TypeScript Path Aliases

**Status:** ⬜ Not Started
**Depends On:** Task 0.13

**Description:**
Set up path aliases for cleaner imports.

**Files to Modify:**

**frontend/tsconfig.json:**
```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,

    /* Bundler mode */
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",

    /* Linting */
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,

    /* Path aliases */
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"],
      "@/components/*": ["./src/components/*"],
      "@/pages/*": ["./src/pages/*"],
      "@/hooks/*": ["./src/hooks/*"],
      "@/utils/*": ["./src/utils/*"],
      "@/types/*": ["./src/types/*"],
      "@/api/*": ["./src/api/*"],
      "@/config/*": ["./src/config/*"],
      "@/lib/*": ["./src/lib/*"],
      "@/context/*": ["./src/context/*"],
      "@/assets/*": ["./src/assets/*"],
      "@/styles/*": ["./src/styles/*"]
    }
  },
  "include": ["src"]
}
```

**frontend/vite.config.ts:**
```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
      '@/components': path.resolve(__dirname, './src/components'),
      '@/pages': path.resolve(__dirname, './src/pages'),
      '@/hooks': path.resolve(__dirname, './src/hooks'),
      '@/utils': path.resolve(__dirname, './src/utils'),
      '@/types': path.resolve(__dirname, './src/types'),
      '@/api': path.resolve(__dirname, './src/api'),
      '@/config': path.resolve(__dirname, './src/config'),
      '@/lib': path.resolve(__dirname, './src/lib'),
      '@/context': path.resolve(__dirname, './src/context'),
      '@/assets': path.resolve(__dirname, './src/assets'),
      '@/styles': path.resolve(__dirname, './src/styles'),
    },
  },
  server: {
    port: 3000,
  },
})
```

**Acceptance Criteria:**
- [ ] Path aliases configured in tsconfig.json
- [ ] Vite resolve aliases configured
- [ ] Can import using `@/` prefix
- [ ] IDE autocomplete works with aliases
- [ ] No TypeScript errors

**Estimated Time:** 20 minutes

---

### Task 0.15: Create TypeScript Type Definitions

**Status:** ⬜ Not Started
**Depends On:** Task 0.13

**Description:**
Create base TypeScript type definition files.

**Files to Create:**

**src/types/common.types.ts:**
```typescript
// Common types used across the application

export interface PageInfo {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: PageInfo;
}

export interface ApiResponse<T> {
  data: T;
  message: string;
  timestamp: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, string>;
}

export type LoadingState = 'idle' | 'loading' | 'success' | 'error';

export interface SelectOption {
  value: string;
  label: string;
}
```

**src/types/user.types.ts:**
```typescript
export interface User {
  id: string;
  username: string;
  email: string;
  fullName: string;
  phone?: string;
  avatar?: string;
  createdAt: string;
  updatedAt: string;
}

export interface Address {
  id?: string;
  fullName: string;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phone: string;
}

export interface RegisterData {
  username: string;
  email: string;
  password: string;
  fullName: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}
```

**src/types/api.types.ts:**
```typescript
import type { ApiError, ApiResponse, PaginatedResponse } from './common.types';

export type { ApiError, ApiResponse, PaginatedResponse };

export interface RequestConfig {
  headers?: Record<string, string>;
  params?: Record<string, any>;
  timeout?: number;
}
```

**src/types/index.ts:**
```typescript
// Re-export all types for easier importing
export * from './common.types';
export * from './user.types';
export * from './api.types';
```

**Acceptance Criteria:**
- [ ] All base type files created
- [ ] Types properly exported
- [ ] No TypeScript errors
- [ ] Can import and use types in components
- [ ] Index file exports all types

**Estimated Time:** 30 minutes

---

## Verification & Next Steps

### Task 0.16: Verify Phase 0 Completion

**Status:** ⬜ Not Started
**Depends On:** All previous tasks

**Description:**
Verify that Phase 0 is complete and the project is ready for Phase 1.

**Verification Checklist:**

**1. Dependencies:**
- [ ] Run `npm install` - no errors
- [ ] Run `npm run dev` - dev server starts on port 3000
- [ ] Run `npm run type-check` - no TypeScript errors
- [ ] Run `npm run lint` - no critical errors
- [ ] Open http://localhost:3000 - app loads

**2. Project Structure:**
- [ ] All directories created
- [ ] Can navigate to each directory
- [ ] .gitkeep files in place

**3. TypeScript:**
- [ ] Path aliases work (try importing with @/)
- [ ] Type files have no errors
- [ ] IDE provides autocomplete for aliases

**4. Environment:**
- [ ] .env.local exists and has values
- [ ] .env.example exists
- [ ] .env.local in .gitignore

**5. Tailwind:**
- [ ] Tailwind classes work in components
- [ ] No CSS errors
- [ ] Styles applied correctly

**Commands to Run:**
```bash
cd frontend

# Install and verify
npm install
npm run type-check
npm run lint

# Start dev server
npm run dev

# In browser: http://localhost:3000
# Should see React app running
```

**Acceptance Criteria:**
- [ ] All verification steps pass
- [ ] No errors in console
- [ ] Dev server runs smoothly
- [ ] Ready to start Phase 1

**Estimated Time:** 20 minutes

---

### Task 0.17: Create Phase 0 Completion Checklist

**Status:** ⬜ Not Started
**Depends On:** Task 0.16

**Description:**
Document Phase 0 completion for team records.

**Create file:** `tasks/frontend/completion-reports/phase-0-report.md`

**Template:**
```markdown
# Phase 0 Completion Report

**Date Completed:** [Date]
**Completed By:** [Name]
**Time Taken:** [Hours]

## Tasks Completed

### Section 1: Dependencies Installation
- [x] Task 0.1: Install Tailwind CSS
- [x] Task 0.2: Configure Tailwind Content Paths
- [x] Task 0.3: Set Up Tailwind Base Styles
- [x] Task 0.4: Install UI Component Library
- [x] Task 0.5: Install Form Libraries
- [x] Task 0.6: Install HTTP Client
- [x] Task 0.7: Install State Management
- [x] Task 0.8: Install Date Utilities
- [x] Task 0.9: Install Development Tools
- [x] Task 0.10: Configure Environment Variables
- [x] Task 0.11: Update package.json Scripts
- [x] Task 0.12: Verify All Dependencies

### Section 2: Basic Project Structure
- [x] Task 0.13: Create Core Directory Structure
- [x] Task 0.14: Configure TypeScript Path Aliases
- [x] Task 0.15: Create TypeScript Type Definitions

### Verification
- [x] Task 0.16: Verify Phase 0 Completion

## Issues Encountered

[List any issues and how they were resolved]

## Deviations from Plan

[List any deviations from the original plan]

## Screenshots

[Optional: Add screenshots of dev server running, etc.]

## Notes for Phase 1

[Any notes or recommendations for the next phase]

## Recommendations

[Any improvements or suggestions]

---

**Status:** ✅ Phase 0 Complete
**Ready for Phase 1:** Yes/No
```

**Acceptance Criteria:**
- [ ] Report created in completion-reports directory
- [ ] All tasks documented
- [ ] Issues and solutions noted
- [ ] Ready to proceed to Phase 1

**Estimated Time:** 15 minutes

---

## Summary

### Total Tasks: 17
### Estimated Total Time: 4-6 hours
### Priority: CRITICAL

### Phase 0 Deliverables:

**Dependencies Installed:**
- [ ] Tailwind CSS configured
- [ ] All required npm packages installed
- [ ] Development tools ready (Vitest, ESLint, etc.)
- [ ] Environment variables configured

**Project Structure:**
- [ ] All core directories created
- [ ] TypeScript path aliases configured
- [ ] Base type definitions created
- [ ] .gitkeep files in empty directories

**Development Environment:**
- [ ] Dev server runs without errors
- [ ] TypeScript compilation works
- [ ] Linting configured
- [ ] Ready to write code

---

## Next Steps

After completing Phase 0:
1. ✅ Mark all tasks complete in this document
2. ✅ Create completion report
3. ✅ Commit initial setup to git
4. ➡️ Proceed to `phase-1-foundation.md`
5. ➡️ Begin implementing utility functions and base components

---

## Git Commit Suggestion

After completing Phase 0, commit your work:

```bash
git add .
git commit -m "feat: complete Phase 0 - project setup

- Install and configure Tailwind CSS
- Install all required dependencies
- Create project directory structure
- Configure TypeScript path aliases
- Set up base type definitions
- Configure development environment

Phase 0 complete. Ready for Phase 1 development."
```

---

**Document Created:** October 2025
**Phase Status:** Not Started
**Target Completion:** 1-2 days from start
**Next Phase:** phase-1-foundation.md
