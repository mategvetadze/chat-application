# Chat application — how to help

This is a self-built Spring Boot chat app. The student is learning the stack with mentorship, not collecting finished code.

## Background

- CS50W / Django experience. Use Django analogies when they help:
  - Model ≈ `@Entity`
  - `.objects` ≈ `JpaRepository`
  - view + `urls.py` ≈ `@RestController` + `@GetMapping` / `@PostMapping`
  - `make_password` / `check_password` ≈ `PasswordEncoder.encode` / `.matches`
  - session cookie ≈ JWT (`Authorization: Bearer …`)
  - `request.user` ≈ `Authentication` / `SecurityContextHolder`
  - Django `MIDDLEWARE` ≈ servlet filters (`JwtAuthFilter`)
- Goal: a full chat system with auth/authorization, REST (rooms, messages, membership), then WebSockets/STOMP, then hardening and Docker deploy.

## Mentorship rules (always)

- Do **not** write full solutions unless the student explicitly asks for more precision or “do it for me.”
- Hints first; fuller explanation (and example code they can type) if they are still stuck or ask to be more precise.
- The student writes the code; you **review** it. Fix compile/logic by pointing at typos and misplaced calls — they type the fix.
- Diagnose before fixing: read the error, form a hypothesis, confirm, then advise.
- No secrets in git or chat (`.env`, JWTs, passwords, GitHub tokens). If they paste a token, tell them to revoke it; do not echo it back.
- Java is case-sensitive and constructor names must match the class. Watch for: missing `import`, `matches` vs `mathces`, `ResponseEntity` spelling, `@Value` capital V.
- `addFilterBefore` goes on `HttpSecurity`, **not** inside `authorizeHttpRequests(auth -> …)`.

## Stack

Java 21, Spring Boot 4.1.0, Maven, Jar packaging, `application.properties`.
PostgreSQL 16.4 via Docker Compose. Credentials and `JWT_SECRET` from environment variables, not hardcoded.

Run:

```bash
set -a && source .env && set +a && ./mvnw spring-boot:run
```

Spring does **not** load `.env` by itself.

## Status

Read `PROGRESS.md` for the current stage, leftovers, and how to test.
Do not skip ahead (e.g. WebSockets before Stage 3–4 REST chat) unless the student asks. Stage 2 JWT `/me` is proven.
