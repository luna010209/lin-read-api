# Auth & profile

Authentication is handled by **lin-auth-api**. Lin-read validates JWTs locally and calls auth when user metadata is needed.

## Services

| Service | URL (local) | Role |
|---------|-------------|------|
| lin-auth-api | `http://127.0.0.1:8080` | Login, signup, profile, token issue |
| lin-read-api | `http://127.0.0.1:8082` | Business APIs, JWT validation, `GET /me` |

Config (`application.yaml`):

```yaml
auth:
  api:
    base-url: http://127.0.0.1:8080
```

## Client flow

1. `POST http://127.0.0.1:8080/auth/login` → `accessToken`
2. Store token in the client
3. Call lin-read with `Authorization: Bearer {accessToken}`
4. `GET http://127.0.0.1:8082/me` → profile from auth-api

Lin-read does **not** read the auth database. User ids in product tables should reference lin-auth users only.

## JWT validation (lin-read-api)

- Tokens are issued by **lin-auth-api**; both services must share the same `jwt.secret-key` (see `application.yaml` / `.env.local.properties`).
- **Access tokens must include `userId`.**
- Only **access tokens** are accepted (`typ: refresh` is rejected).
- Only `ROLE_*` application roles are mapped to lin-read `Role`. Other JWT authorities are ignored.

### Symptom: `GET /me` → 401 after login

| Check | Action |
|-------|--------|
| Old token in client | Clear stored tokens and log in again |
| JWT secret mismatch | Set the same `JWT_SECRET_KEY` in lin-auth and lin-read `.env.local.properties`. Default: `local-admin-authenticate-secret-key`. Restart both APIs and log in again. |
| Missing `userId` in token | JWT payload must contain `"userId": <number>` |

## lin-read — `GET /me` (authenticated)

Returns the current user profile from lin-auth-api (no local subscription merge).

Profile updates still use lin-auth-api (`PUT /auth`, etc.).
