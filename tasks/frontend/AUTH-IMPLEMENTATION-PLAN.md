# Frontend Authentication Implementation Plan - React Query Approach

## Overview
Complete TDD implementation plan for integrating frontend with backend JWT authentication using React Query for optimal state management.

## Architecture Decision

**Chosen Approach**: React Query + Lightweight Context

**Benefits**:
- Built-in loading/error/success states
- Automatic cache management
- Request deduplication
- Better separation: server state (React Query) vs client state (tokens)
- Superior developer experience with DevTools

## Implementation Tasks

### Phase 1: Foundation (Tasks 01-05)
✅ **Task 01**: Update auth types to match backend API
✅ **Task 02**: Update API endpoints configuration  
✅ **Task 03**: Implement auth service with TDD
✅ **Task 04**: Enhance axios interceptor for JWT refresh
✅ **Task 05**: Implement token storage utilities with tests

### Phase 2: React Query Integration (Tasks 06-07)
✅ **Task 06**: AuthContext with React Query setup
- Lightweight context for client state (tokens, user)
- QueryProvider with devtools
- Computed isAuthenticated from token

✅ **Task 07**: Auth mutation hooks
- useLoginMutation
- useRegisterMutation  
- useLogoutMutation
- Built-in states and callbacks

### Phase 3: UI Integration (Tasks 08-10)
✅ **Task 08**: Login page with mutations
- Use mutation for login with automatic states
- Form validation with Zod
- Auto-navigation on success

✅ **Task 09**: Register page with mutations
- Auto-login support (backend returns tokens)
- Password confirmation validation
- Mutation-based error handling

✅ **Task 10**: Logout implementation
- Mutation with graceful error handling
- Clears local state even on API failure
- Auto-navigation to login

### Phase 4: Protection & Testing (Tasks 11-12)
✅ **Task 11**: Protected routes component
- Authentication guard
- Role-based access control
- Redirect to login

✅ **Task 12**: E2E authentication flow testing
- Complete user journey tests
- Token persistence validation
- React Query integration tests

## Technology Stack

```json
{
  "state-management": "@tanstack/react-query",
  "forms": "react-hook-form",
  "validation": "zod",
  "routing": "react-router-dom",
  "http-client": "axios",
  "testing": "vitest + @testing-library/react"
}
```

## File Structure

```
src/
├── api/
│   └── services/
│       ├── auth.service.ts          # Task 03
│       └── __tests__/
│           └── auth.service.test.ts
├── config/
│   ├── api.config.ts                # Task 02
│   └── __tests__/
│       └── api.config.test.ts
├── context/
│   ├── AuthContext.tsx              # Task 06
│   └── __tests__/
│       └── AuthContext.test.tsx
├── hooks/
│   ├── useAuthMutations.ts          # Task 07
│   └── __tests__/
│       └── useAuthMutations.test.tsx
├── lib/
│   ├── axios.ts                     # Task 04
│   └── __tests__/
│       └── axios.test.ts
├── pages/
│   ├── Login.tsx                    # Task 08
│   ├── Register.tsx                 # Task 09
│   └── __tests__/
│       ├── Login.test.tsx
│       └── Register.test.tsx
├── providers/
│   └── QueryProvider.tsx            # Task 06
├── components/
│   └── auth/
│       └── ProtectedRoute.tsx       # Task 11
├── types/
│   ├── auth.types.ts                # Task 01
│   └── __tests__/
│       └── auth.types.test.ts
├── utils/
│   ├── storage.ts                   # Task 05
│   └── __tests__/
│       └── storage.test.ts
└── __tests__/
    └── e2e/
        └── auth-flow.test.tsx       # Task 12
```

## Backend API Contract

**Endpoints** (port 8081):
- `POST /api/auth/register` → `{ accessToken, refreshToken, user }`
- `POST /api/auth/login` → `{ accessToken, refreshToken, user }`
- `POST /api/auth/refresh` → `{ accessToken }`
- `POST /api/auth/logout` → `{ message }`

**Token Lifetimes**:
- Access Token: 1 hour
- Refresh Token: 7 days

**Security**:
- Access tokens in Authorization header
- Refresh tokens in request body
- Blacklist on logout (Redis)
- Auto-refresh on 401

## Implementation Workflow

### For Each Task:
1. **Write Tests First** (TDD)
2. **Implement Feature**
3. **Run Tests** (`npm test`)
4. **Type Check** (`npm run type-check`)
5. **Git Commit** (Conventional Commits)

### Example Workflow:
```bash
# Task 06 example
npm test src/context/__tests__/AuthContext.test.tsx --watch
# Write tests until failing
# Implement AuthContext.tsx
# Tests pass ✅
npm run type-check
git add src/context/
git commit -m "feat(auth): implement AuthContext with React Query integration"
```

## Testing Strategy

### Unit Tests (>90% coverage)
- Auth service
- Token storage
- Type definitions
- Axios interceptor

### Integration Tests (>85% coverage)  
- AuthContext
- Mutation hooks
- API integration

### Component Tests (>80% coverage)
- Login page
- Register page
- Protected routes

### E2E Tests
- Complete auth flows
- Token persistence
- Navigation

## Quality Gates

Before marking any task complete:
- [ ] All tests passing
- [ ] Type-check passing  
- [ ] Linting passing
- [ ] Code coverage meets target
- [ ] Git commit follows conventions

## Git Commit Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**: `feat`, `fix`, `test`, `refactor`, `docs`
**Scopes**: `auth`, `api`, `types`, `storage`, `config`

## Development Commands

```bash
# Development
npm run dev                    # Start dev server
npm test                       # Run all tests
npm test -- --watch           # Watch mode
npm test -- --coverage        # Coverage report
npm run type-check            # TypeScript validation
npm run lint                  # ESLint check

# Backend
cd ../store-backend
../gradlew bootRun            # Start on port 8081
```

## Success Criteria

✅ All 12 tasks completed
✅ >85% test coverage overall
✅ Backend integration working
✅ Token refresh automatic
✅ Logout blacklists tokens
✅ Protected routes working
✅ All tests passing
✅ Type-safe throughout
✅ React Query DevTools functional

## References

- **Backend Docs**: `docs/AUTHENTICATION_FLOW.md`
- **Project Guide**: `CLAUDE.md`
- **Backend Config**: `store-backend/src/main/resources/application-local.yml`
- **React Query**: https://tanstack.com/query/latest
- **Testing Library**: https://testing-library.com/

## Next Steps

1. Start with Task 01 (types)
2. Work sequentially through tasks
3. Follow TDD approach strictly
4. Commit after each completed task
5. Test integration with running backend
6. Celebrate when all done! 🎉
