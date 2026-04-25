# EAMS API Gateway

API Gateway for EAMS — JWT validation, RBAC, rate limiting, and reverse proxy to Spring Boot backend.

## Architecture

```
Client → [NestJS Gateway :3000] → [Spring Boot Backend :8080]
            ├─ JWT validation
            ├─ RBAC enforcement
            ├─ Rate limiting (100 req/min)
            └─ Header injection (X-User-Id, X-Institution-Id)
```

## Setup

### Prerequisites
- Node.js 18+
- npm or yarn

### Installation

```bash
npm install
```

### Environment Variables

Create a `.env` file based on `.env.example`:

```bash
PORT=3000
NODE_ENV=development
JWT_SECRET=your-secret-key-change-in-production
BACKEND_URL=http://localhost:8080
THROTTLE_TTL=60000
THROTTLE_LIMIT=100
```

## Development

### Build

```bash
npm run build
```

### Start (Development with Watch)

```bash
npm run start:dev
```

The gateway will be available at `http://localhost:3000`

### Start (Debug Mode)

```bash
npm run start:debug
```

### Start (Production)

```bash
npm run start:prod
```

## Testing

### Run Tests

```bash
npm test
```

### Watch Mode

```bash
npm test:watch
```

### Coverage Report

```bash
npm run test:cov
```

**Coverage Threshold**: 95% across lines, branches, functions, and statements.

## API Structure

### Public Routes (No Authentication Required)
- `POST /auth/login` — User login
- `POST /auth/mfa/verify` — MFA verification
- `POST /auth/refresh` — Refresh JWT token
- `POST /auth/logout` — User logout
- `GET /health` — Health check

### Protected Routes

All routes under `/api/**` require a valid JWT token and are subject to RBAC.

#### RBAC Rules

| Endpoint | GUARDIAN | TEACHER | ADMIN | SUPERADMIN |
|---|---|---|---|---|
| `POST /enrollments` | ✓ | - | ✓ | ✓ |
| `POST /attendance/sessions` | - | ✓ | ✓ | ✓ |
| `POST /activities` | - | ✓ | ✓ | ✓ |
| `PATCH /activities/:id/status` | - | - | ✓ | ✓ |
| `POST /institutions` | - | - | - | ✓ |

## Key Features

### JWT Validation
- Validates HMAC-SHA JWT tokens
- Rejects MFA-pending tokens
- Extracts user identity and role

### Rate Limiting
- 100 requests per 60 seconds per IP/user
- Throttled at the gateway level

### RBAC (Role-Based Access Control)
- Four roles: GUARDIAN, TEACHER, ADMIN, SUPERADMIN
- SUPERADMIN bypasses all role checks
- Decorators: `@Public()` for unauthenticated routes, `@Roles(...)` for role-restricted

### Reverse Proxy
- Forwards all authenticated requests to Spring Boot backend
- Injects headers: `X-User-Id`, `X-Institution-Id`
- Normalizes backend errors to standard format

### Security
- Helmet.js for HTTP headers
- CORS configuration
- Bearer token authentication via Passport.js

## Project Structure

```
src/
├── main.ts                              # Entry point
├── app.module.ts                        # Root module
├── health.controller.ts                 # Health check
├── auth/
│   ├── auth.module.ts
│   ├── auth.controller.ts               # Login, MFA, refresh, logout
│   ├── jwt.strategy.ts                  # Passport JWT strategy
│   ├── jwt-auth.guard.ts                # JWT validation guard
│   ├── public.decorator.ts              # @Public() decorator
│   ├── jwt-auth.guard.spec.ts
│   └── (other test files)
├── rbac/
│   ├── rbac.module.ts
│   ├── roles.decorator.ts               # @Roles() decorator
│   ├── roles.guard.ts                   # RBAC enforcement
│   └── roles.guard.spec.ts
├── proxy/
│   ├── proxy.module.ts
│   ├── proxy.service.ts                 # Request forwarding logic
│   ├── proxy.controller.ts              # Reverse proxy endpoint
│   ├── proxy.service.spec.ts
│   └── proxy.controller.spec.ts
└── common/
    └── interceptors/
        ├── error-normalizer.interceptor.ts  # Error standardization
        └── error-normalizer.interceptor.spec.ts
```

## Testing Strategy

### Unit Tests

- **JWT Auth Guard**: Valid tokens, expired tokens, missing tokens, MFA-pending
- **Roles Guard**: All RBAC combinations, forbidden scenarios
- **Proxy Service**: Header injection, backend errors, different HTTP methods
- **Error Interceptor**: Error normalization for 4xx and 5xx responses

### Running Tests

```bash
# All tests
npm test

# Specific file
npm test -- proxy.service.spec.ts

# Watch mode
npm test:watch

# Coverage
npm run test:cov
```

## Error Handling

The gateway normalizes backend errors into a standard format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human-readable message"
}
```

### Status Code Mapping
- `400` → Bad Request
- `404` → Not Found
- `403` → Forbidden
- `409` → Conflict
- `5xx` → Internal Server Error

## Performance & Scalability

- Stateless design (no sessions, no database)
- Horizontal scalability (can run multiple instances)
- Rate limiting at gateway level
- Caching headers from backend respected
- Connection pooling via Axios

## Troubleshooting

### Gateway won't start
- Check `JWT_SECRET` is set
- Verify `BACKEND_URL` is reachable
- Check port 3000 is available

### Tests fail with coverage threshold
- Run `npm run test:cov` to see which files lack coverage
- Ensure all guards and interceptors are tested

### Requests timeout
- Increase `THROTTLE_TTL` if rate limiting is too aggressive
- Check backend connectivity

## Contributing

- Follow NestJS conventions
- Write tests for new features
- Maintain ≥95% code coverage
- Use TypeScript with strict mode
