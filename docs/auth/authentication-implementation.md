# Authentication Implementation

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/auth/email-verifications` | Send six-digit email verification code |
| `POST` | `/api/auth/email-verifications/confirm` | Confirm code |
| `POST` | `/api/auth/sign-up` | Create verified email account and return tokens |
| `POST` | `/api/auth/login` | Email/password login |
| `POST` | `/api/auth/refresh` | Rotate refresh token |
| `POST` | `/api/auth/logout` | Revoke current refresh-token session |
| `GET` | `/oauth2/authorization/google` | Start Google OAuth2 login |

Google redirect URI for local development is `http://localhost:8080/login/oauth2/code/google`. Register it in Google Cloud Console.

## Token and Redis Design

- Access JWT TTL: 30 minutes; it carries the user ID and `type=access`.
- Refresh JWT TTL: 14 days; it carries a unique session ID (`sid`) and `type=refresh`.
- Redis stores only SHA-256 hashes: `auth:refresh:{userId}:{sessionId}`.
- A per-user Redis set tracks session IDs: `auth:refresh:user:{userId}`.
- Email code key: `auth:email:code:{email}`, TTL 5 minutes; request cooldown key has a 1-minute TTL.

## Theft Mitigation

1. Refresh tokens are never persisted in plaintext.
2. Refresh uses one-time rotation: successful use deletes the old session and issues a new session/token pair.
3. A missing or mismatched stored hash means a revoked or reused token. The server revokes every refresh session for that user and returns an error, forcing a new login.
4. Passwords are BCrypt-hashed.
5. Email signup requires a Redis-backed confirmation before the account is created.

## Local Secrets

SMTP credentials, Google client credentials, and `JWT_SECRET` are loaded only from the root `.env`, which is ignored by Git. `MAIL_USERNAME` must be the Gmail address that owns the supplied app password. Do not add real values to this document, `application.yml`, logs, or commits.

## Frontend Integration

Send the access token as `Authorization: Bearer <token>`. Keep a refresh token in a secure storage strategy appropriate to the web client; request `POST /api/auth/refresh` before retrying an expired access token. Do not put tokens in URLs or logs.
