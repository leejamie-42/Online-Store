# Frontend Documentation
## Online Store Application - COMP5348

Welcome to the frontend documentation for the Online Store application. This directory contains comprehensive guides for implementing and maintaining the React-based single-page application.

---

## Documentation Structure

### 📋 [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md)
**Start Here** - The main implementation roadmap

- Project overview and goals
- 7-phase implementation plan (28-39 days)
- Technical requirements and success criteria
- Risk assessment and mitigation strategies
- Timeline and priorities

**Best for:** Project managers, team leads, and developers starting the project

---

### 🏗️ [ARCHITECTURE.md](./ARCHITECTURE.md)
**Technical Architecture** - System design and component structure

- Complete project folder structure
- Component architecture and hierarchy
- State management strategy (Context API, Zustand, React Query)
- Routing architecture with protected routes
- Data flow patterns
- Key design patterns (Composition, Render Props, Custom Hooks)

**Best for:** Developers implementing features, architects, senior engineers

---

### 🎨 [UI_DESIGN_SYSTEM.md](./UI_DESIGN_SYSTEM.md)
**Design System** - Visual design and component specifications

- Color palette (primary, secondary, semantic colors)
- Typography system
- Spacing and layout guidelines
- Component library (buttons, inputs, cards, modals, etc.)
- Responsive design breakpoints
- Accessibility standards (WCAG 2.1 Level AA)
- Animation guidelines

**Best for:** Frontend developers, UI/UX designers, anyone building UI components

---

### 🔌 [API_INTEGRATION.md](./API_INTEGRATION.md)
**API Integration** - Backend communication guide

- Authentication flow and JWT management
- Complete API endpoint reference
  - Authentication endpoints
  - Product catalog endpoints
  - Shopping cart endpoints
  - Order management endpoints
  - User profile endpoints
- Request/response formats
- Error handling patterns
- Best practices and examples

**Best for:** Frontend developers integrating with backend, API consumers

---

### 📝 [DEVELOPMENT_STANDARDS.md](./DEVELOPMENT_STANDARDS.md)
**Coding Standards** - Best practices and conventions

- Code style and formatting (ESLint, Prettier)
- Naming conventions (files, variables, components)
- Component structure and organization
- TypeScript standards
- Testing guidelines
- Git workflow and commit messages
- Code review checklist
- Performance optimization techniques

**Best for:** All developers, code reviewers, new team members

---

## Quick Start Guide

### For New Team Members

1. Read [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - Understand the project scope
2. Review [ARCHITECTURE.md](./ARCHITECTURE.md) - Learn the technical architecture
3. Study [DEVELOPMENT_STANDARDS.md](./DEVELOPMENT_STANDARDS.md) - Follow coding conventions
4. Reference [UI_DESIGN_SYSTEM.md](./UI_DESIGN_SYSTEM.md) - Build consistent UI components
5. Use [API_INTEGRATION.md](./API_INTEGRATION.md) - Integrate with backend APIs

### For Implementing Features

1. Check [IMPLEMENTATION_PLAN.md](./IMPLEMENTATION_PLAN.md) - Find your feature phase
2. Review [ARCHITECTURE.md](./ARCHITECTURE.md) - Understand component structure
3. Follow [UI_DESIGN_SYSTEM.md](./UI_DESIGN_SYSTEM.md) - Use design system components
4. Reference [API_INTEGRATION.md](./API_INTEGRATION.md) - Call correct endpoints
5. Apply [DEVELOPMENT_STANDARDS.md](./DEVELOPMENT_STANDARDS.md) - Write quality code

---

## Tech Stack Summary

| Category | Technology |
|----------|------------|
| **Framework** | React 18 |
| **Language** | TypeScript |
| **Build Tool** | Vite |
| **Routing** | React Router v6 |
| **Styling** | Tailwind CSS |
| **State Management** | Context API / Zustand |
| **Data Fetching** | React Query / Axios |
| **Forms** | React Hook Form + Zod |
| **Testing** | Vitest + React Testing Library |
| **Linting** | ESLint |
| **Formatting** | Prettier |

---

## Key Features to Implement

### Phase 1: Foundation (HIGH Priority)
- ✅ Authentication system
- ✅ API integration layer
- ✅ Base component library
- ✅ Layout components

### Phase 2: Core Features (HIGH Priority)
- ✅ Product catalog with search/filter
- ✅ Product detail pages
- ✅ Shopping cart
- ✅ Checkout flow

### Phase 3: User Management (MEDIUM Priority)
- ✅ Order history
- ✅ Order tracking
- ✅ Order cancellation
- ✅ User profile

### Phase 4: Polish (LOW Priority)
- ✅ Performance optimization
- ✅ Accessibility improvements
- ✅ Testing coverage
- ✅ Animation and transitions

---

## Development Workflow

### 1. Setup Environment
```bash
cd frontend
npm install
cp .env.example .env
# Edit .env with your configuration
npm run dev
```

### 2. Create Feature Branch
```bash
git checkout -b feature/your-feature-name
```

### 3. Implement Feature
- Follow architecture guidelines
- Use design system components
- Write tests
- Follow coding standards

### 4. Submit Pull Request
- Fill out PR template
- Ensure all tests pass
- Request code review

### 5. Deploy
- Merge to main after approval
- CI/CD pipeline builds and deploys

---

## Project Structure Overview

```
frontend/
├── src/
│   ├── api/              # API integration
│   ├── components/       # Reusable components
│   │   ├── ui/          # Base UI components
│   │   ├── layout/      # Layout components
│   │   ├── features/    # Feature components
│   │   └── common/      # Common components
│   ├── pages/           # Page components
│   ├── hooks/           # Custom React hooks
│   ├── context/         # React Context providers
│   ├── types/           # TypeScript types
│   ├── utils/           # Utility functions
│   ├── styles/          # Global styles
│   └── config/          # Configuration
├── public/              # Static assets
└── docs/                # Documentation (you are here!)
```

---

## Common Tasks

### Adding a New Component
1. Create component directory in `src/components/`
2. Follow naming conventions from DEVELOPMENT_STANDARDS.md
3. Use design system styles from UI_DESIGN_SYSTEM.md
4. Write tests
5. Export from index.ts

### Integrating a New API Endpoint
1. Add endpoint to `src/api/endpoints.ts`
2. Create service method in appropriate service file
3. Define TypeScript types
4. Handle errors appropriately
5. See API_INTEGRATION.md for examples

### Creating a New Page
1. Create page component in `src/pages/`
2. Add route in `src/router.tsx`
3. Add to route constants in `src/config/routes.ts`
4. Use MainLayout or create custom layout
5. Implement with existing components

---

## Resources

### Internal
- [Backend Repository](../../warehouse/) - Spring Boot backend
- [Main README](../../README.md) - Project overview

### External
- [React Documentation](https://react.dev/)
- [TypeScript Documentation](https://www.typescriptlang.org/docs/)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [React Router Documentation](https://reactrouter.com/)
- [React Query Documentation](https://tanstack.com/query/latest)

---

## Getting Help

### Questions?
- Check the relevant documentation file first
- Ask in team chat/Slack
- Create an issue in the repository

### Found an Issue?
- Check if it's already reported
- Create a new issue with details
- Tag with appropriate labels

### Want to Contribute?
- Follow the Git workflow in DEVELOPMENT_STANDARDS.md
- Ensure your code follows all standards
- Submit a pull request with description

---

## Change Log

| Date | Version | Changes |
|------|---------|---------|
| 2025-10-15 | 1.0 | Initial documentation created |

---

## Maintainers

- **Project Lead:** [Name]
- **Frontend Lead:** [Name]
- **UI/UX Designer:** [Name]

---

**Last Updated:** October 2025
**Status:** Active Development
**License:** [Your License]
