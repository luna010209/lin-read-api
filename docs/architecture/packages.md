# Package structure

Feature-first layout under `io.lin.read.feature.*`, aligned with lin-langa-api.

## Top-level

| Package | Purpose |
|---------|---------|
| `io.lin.read.config` | Spring, security, JPA, auth client, Swagger |
| `io.lin.read.exception` | Shared exceptions |
| `io.lin.read.common.audit` | JPA audit base classes |
| `io.lin.read.common.dto` | Shared API types (`ApiResponse`) |
| `io.lin.read.common.security` | `CurrentUserContext` |
| `io.lin.read.security` | `AuthenticatedUser`, `Role` |
| `io.lin.read.utils` | Shared utilities |
| `io.lin.read.aiGenerate` | Gemini text / image / audio clients |
| `io.lin.read.prompt` | AI prompt templates (Read-specific) |

## Feature module layout

Each feature under `io.lin.read.feature.<name>`:

```
feature/<name>/
  <Name>Controller.java
  dto/
  entity/
  enums/
  service/
  repo/
```

## Current features

| Feature | Package | Notes |
|---------|---------|-------|
| User / profile | `io.lin.read.feature.user` | JWT validation; profile via lin-auth-api (`GET /me`) |
