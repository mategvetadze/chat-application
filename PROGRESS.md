# Progress

Update this file at the end of each session.

## Current stage

**Stage 2 done. Next: Stage 3 — Authorization (roles, `@PreAuthorize`, resource ownership).**

JWT round-trip proven: `GET /me` with a token from `/login` returns **200** `{"username":"mate6"}`. Without a token: **403** (anonymous access-denied; 401 polish is optional).

## Stages

| Stage | Topic | Status |
|-------|--------|--------|
| 0 | Foundations — skeleton, Postgres, `/health` | Done |
| 1 | User model & registration (no auth) | Done |
| 2 | Authentication — Spring Security + JWT | Done |
| 3 | Authorization — roles, `@PreAuthorize`, ownership | Not started |
| 4 | Chat domain REST — rooms, messages, membership | Not started |
| 5 | Real-time — WebSocket/STOMP | Not started |
| 6 | Presence — online status, typing | Not started |
| 7 | Hardening — rate limit, sanitization, Testcontainers | Not started |
| 8 | Deployment — Docker, env-based config | Not started |

## Stage 0 (done)

- Docker Postgres 16.4 (`docker-compose.yaml`), port 5432, env from `.env`.
- **No Docker volume** — `docker compose down` wipes the DB; ids start at 1 again. `docker compose stop` keeps data. Volume is a later optional fix.
- `application.properties` uses `${POSTGRES_USER}`, `${POSTGRES_PASSWORD}`, `${POSTGRES_DB}`.
- `DemoApplication` with `@SpringBootApplication`.
- Throwaway `HealthCheck` entity + repository + `GET /health` calls `count()` and returns `OK, rows: …`.
- Native Postgres on the host was disabled so port 5432 is free for Docker.

## Stage 1 (done)

- `User` entity, `@Table(name = "users")` (Postgres reserves `user`).
- Unique `username` and `email`; `getId()` exists.
- `UserRepository`: `existsByUsername`, `existsByEmail`, later `findByUsername`.
- `POST /register` — 201, body is `Map` of `id`, `username`, `email` (no password; do **not** `setPassword(null)` on a managed entity).
- 409 if username or email taken.

## Stage 2 (done)

Dependencies: `spring-boot-starter-security`, jjwt `0.12.6` (`jjwt-api`, `jjwt-impl`, `jjwt-jackson`).

Config:

- `jwt.secret=${JWT_SECRET}` and `jwt.expiration-ms=3600000` in `application.properties`.
- `JWT_SECRET` in `.env` (gitignored, ≥ 32 chars).
- `SecurityConfig`: CSRF off (JSON API); `STATELESS` sessions; `permitAll` for `/health`, `/register`, `/login`; everything else authenticated; `JwtAuthFilter` **before** `UsernamePasswordAuthenticationFilter` (must be chained on `http`, not inside `authorizeHttpRequests`).
- `PasswordEncoder` bean = `BCryptPasswordEncoder`.
- Ignore log line `Using generated security password` — Spring’s in-memory user, not the `users` table.

Behavior:

- Register hashes password with `passwordEncoder.encode` **before** `save`.
- `POST /login`: `findByUsername` + `passwordEncoder.matches(raw, hash)` → 200 `{id, username, token}` or 401.
- `JwtService.createToken(username)` / `extractUsername(token)` (jjwt 0.12: `subject`, `signWith`, `verifyWith`, `getSubject()`).
- `JwtAuthFilter`: if `Authorization` starts with `Bearer `, parse token, set `UsernamePasswordAuthenticationToken` on `SecurityContextHolder`.
- `GET /me` returns `{"username": …}` from `Authentication.getName()`. Not in `permitAll`.

### Proven with curl

- Register 201, login 200 with `token`.
- `/me` without token → 403.
- `/me` with the token from login → **200** `{"username":"mate6"}`.

## What to do next (in order)

1. Commit Stage 2 (`AGENTS.md`, `PROGRESS.md`, auth sources). Do **not** commit `.env` or tokens.
2. Optional polish: exception handling so anonymous `/me` is 401 not 403; `@Valid` on register/login; replace default in-memory UserDetailsService so the generated-password warning goes away.
3. **Stage 3 — Authorization:** roles, `@PreAuthorize`, resource ownership (“is this *your* room?”). Not “who are you?” (that’s JWT) — “are you allowed?”
4. Stage 4 — REST rooms, messages, membership.
5. Stage 5 — WebSocket/STOMP (not before REST chat exists).
6. Stage 6 — presence / typing.
7. Stage 7 — rate limit, sanitization, Testcontainers.
8. Stage 8 — Docker for the app, volumes for Postgres, env-based config.

## Leftovers (anytime)

- Optional: `@Valid` / `@NotBlank` / `@Email` (Validation starter already in `pom.xml`).
- Optional: Docker volume for Postgres so data survives `compose down`.
- Duplicate imports in `SecurityConfig` (harmless; can clean up).
- Commit Stage 2; do **not** commit `.env`.

## How to run

```bash
cd ~/chat-application
docker compose up -d
set -a && source .env && set +a
./mvnw spring-boot:run
```

| Method | Path | Auth |
|--------|------|------|
| GET | `/health` | public |
| POST | `/register` | public, JSON `{username, email, password}` |
| POST | `/login` | public, JSON `{username, password}` → token |
| GET | `/me` | `Authorization: Bearer <token>` |

## Decisions

- Secrets stay in `.env` (gitignored). Placeholders: `POSTGRES_*`, `JWT_SECRET`.
- `ddl-auto=update` and `show-sql=true` for development.
- JWT instead of server sessions (`STATELESS`); CSRF disabled for a JSON API.
- HealthCheck classes stay until later; they are throwaway.
- Password hashing is bcrypt. Users registered **before** hashing (if any still exist) cannot login.

## Repo

https://github.com/mategvetadze/chat-application
