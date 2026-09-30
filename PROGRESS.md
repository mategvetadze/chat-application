# Progress

Update this file at the end of each session.

## Current stage

**Stage 3 done. Stage 4 rooms REST done. Next: messages (then membership).**

JWT + roles + room ownership proven. Do **not** start WebSockets until REST messages exist.

## Stages

| Stage | Topic | Status |
|-------|--------|--------|
| 0 | Foundations — skeleton, Postgres, `/health` | Done |
| 1 | User model & registration (no auth) | Done |
| 2 | Authentication — Spring Security + JWT | Done |
| 3 | Authorization — roles, `@PreAuthorize`, ownership | Done |
| 4 | Chat domain REST — rooms, messages, membership | In progress (rooms done) |
| 5 | Real-time — WebSocket/STOMP | Not started |
| 6 | Presence — online status, typing | Not started |
| 7 | Hardening — rate limit, sanitization, Testcontainers | Not started |
| 8 | Deployment — Docker, env-based config | Not started |

## Stage 0 (done)

- Docker Postgres 16.4 (`docker-compose.yaml`), port 5432, env from `.env`.
- **No Docker volume** — `docker compose down` wipes the DB; ids start at 1 again. `docker compose stop` keeps data. Volume is a later optional fix.
- `application.properties` uses `${POSTGRES_USER}`, `${POSTGRES_PASSWORD}`, `${POSTGRES_DB}`.
- `DemoApplication` with `@SpringBootApplication`.
- Throwaway `HealthCheck` entity + repository + `GET /health` proved DB connectivity, then those classes were **deleted**. `/health` is still in `permitAll` but has no controller (anonymous hit is **403**). Restore or drop the matcher later.
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
- `JwtService.createToken(username, role)` / `extractUsername` / `extractRole` (jjwt 0.12: `subject`, `claim("role")`, `signWith`, `verifyWith`).
- `JwtAuthFilter`: if `Authorization` starts with `Bearer `, parse token, set `UsernamePasswordAuthenticationToken` with `ROLE_` + role (blank → `"USER"`).
- `GET /me` returns `{"username": …}` from `Authentication.getName()`. Not in `permitAll`.

### Proven with curl

- Register 201, login 200 with `token`.
- `/me` without token → 403.
- `/me` with the token from login → **200** `{"username":"mate6"}`.

## Stage 3 (done)

Authorization = “are you allowed?”, not “who are you?” (that’s JWT).

- `User.role` column, default `"USER"`.
- JWT carries `role`; filter sets `SimpleGrantedAuthority("ROLE_" + role)`.
- `@EnableMethodSecurity` + `GET /admin/ping` with `@PreAuthorize("hasRole('ADMIN')")`.
- Proven earlier: user with `role = ADMIN` → **200** `{"ok":"admin"}`.
- **Ownership:** `DELETE /rooms/{id}` — owner (`createdBy.username == auth.getName()`) or `ROLE_ADMIN`. Else **403**. Missing room **404**.

Authority string is `"ROLE_ADMIN"` (underscore). `"ROLE ADMIN"` would never match.

## Stage 4 (in progress — rooms done)

- `Room` entity: `name`, `@ManyToOne User createdBy` (`created_by_id`). Table `rooms`.
- `RoomRepository extends JpaRepository<Room, Long>`.
- `RoomController`: never return the entity as JSON (would leak `User.password`). Use `toMap` → `id`, `name`, `createdBy` username.
- `POST /rooms` — `createdBy` from JWT via `findByUsername(auth.getName())`, not from JSON. Body `{"name":"general"}`. **201**.
- `GET /rooms` — list of maps. Auth required.
- `DELETE /rooms/{id}` — ownership as above. Owner **204**.

### Proven with curl (2026-08-18)

- `POST /rooms` as alice → **201** `{id, name, createdBy}`.
- `GET /rooms` → **200** list; without token → **403**.
- `DELETE` as non-owner → **403** `{"error":"forbidden"}`.
- `DELETE` missing id → **404** `{"error":"not found"}`.
- `DELETE` as owner → **204**, then list is `[]`.

Still to do in Stage 4: **messages**, then **membership**.

## What to do next (in order)

1. Commit rooms work (`Room`, `RoomRepository`, `RoomController`, `PROGRESS.md`). Do **not** commit `.env` or tokens.
2. **Stage 4 — messages:** `Message` entity (`body`, `room`, `author`, timestamp), REST create/list (only in a room; author from JWT).
3. Stage 4 — membership (who is in a room) if you want private rooms before realtime.
4. Optional polish: 401 vs 403 for anonymous; `@Valid`; drop unused imports in `RoomController`; restore or remove `/health`.
5. Stage 5 — WebSocket/STOMP (not before REST messages exist).
6. Stage 6 — presence / typing.
7. Stage 7 — rate limit, sanitization, Testcontainers.
8. Stage 8 — Docker for the app, volumes for Postgres, env-based config.

## Leftovers (anytime)

- Optional: `@Valid` / `@NotBlank` / `@Email` (Validation starter already in `pom.xml`).
- Optional: Docker volume for Postgres so data survives `compose down`.
- Duplicate imports in `SecurityConfig` (harmless; can clean up).
- Unused imports in `RoomController` (`PasswordEncoder`, `PreAuthorize`).
- `GET /health` matcher left in `SecurityConfig` after HealthCheck classes were deleted.
- Commit rooms + Stage 3/4 progress; do **not** commit `.env`.

## How to run

```bash
cd ~/chat-application
docker compose up -d
set -a && source .env && set +a
./mvnw spring-boot:run
```

| Method | Path | Auth |
|--------|------|------|
| GET | `/health` | leftover matcher; controller deleted → 403 |
| POST | `/register` | public, JSON `{username, email, password}` |
| POST | `/login` | public, JSON `{username, password}` → token |
| GET | `/me` | `Authorization: Bearer <token>` |
| GET | `/admin/ping` | JWT + `ROLE_ADMIN` |
| POST | `/rooms` | JWT, JSON `{name}` → 201 `{id, name, createdBy}` |
| GET | `/rooms` | JWT |
| DELETE | `/rooms/{id}` | JWT; owner or admin → 204; else 403; missing 404 |

## Decisions

- Secrets stay in `.env` (gitignored). Placeholders: `POSTGRES_*`, `JWT_SECRET`.
- `ddl-auto=update` and `show-sql=true` for development.
- JWT instead of server sessions (`STATELESS`); CSRF disabled for a JSON API.
- HealthCheck classes were throwaway and have been deleted; `/health` route leftover.
- Password hashing is bcrypt. Users registered **before** hashing (if any still exist) cannot login.

## Repo

https://github.com/mategvetadze/chat-application
