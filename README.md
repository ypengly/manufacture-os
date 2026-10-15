# ManufactureOS

A manufacturing execution and management system: bills of material and MRP, concurrency-safe inventory, production orders, machine and worker scheduling, quality control, purchasing, notifications, dashboards, CSV reports, and an audit trail.

- **Backend:** Java 21, Spring Boot 3.3.5, Spring Data JPA, Spring Security (JWT), Flyway, PostgreSQL 16, springdoc OpenAPI, JUnit 5 + Testcontainers
- **Frontend:** React 18, TypeScript, Vite, Tailwind, React Router, Axios, Recharts, React Hook Form + Zod
- **Infra:** Docker Compose, all configuration via environment variables

> **Verification status — read this first.** The frontend was installed and built (`npm run build`, TypeScript strict) in the authoring environment. The backend was **not compiled or tested** there, because Maven Central was unreachable (HTTP 403 from the sandbox proxy). The Java code and tests were reviewed by hand only. Run `cd backend && mvn clean verify` (needs Docker for the Testcontainers integration test) before relying on it, and expect to fix a few compile-level issues.

## Quick start (Docker)

```bash
cp .env.example .env
# set DATABASE_PASSWORD and JWT_SECRET (>= 32 chars, e.g. `openssl rand -base64 48`)
# for a local demo with sample users, also set SEED_ENABLED=true
docker compose up --build
```

- UI: http://localhost:3000 (nginx serves the SPA and proxies `/api` to the backend)
- API: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html · Health: `/actuator/health`

### Demo data (dev only)

With `SEED_ENABLED=true` (or the `dev` Spring profile) the app creates one user per role — usernames `admin`, `production_manager`, `supervisor`, `quality_inspector`, `inventory_manager`, `worker` — all with the **publicly documented password `Password123!`**, plus a sample factory, product, BOM, machines, workers and opening stock. **Never enable seeding outside local demos.** It is off by default and off in `application.yml`; with it off, no users exist and you must create the first admin yourself (e.g. an `INSERT` or a one-off bootstrap).

## Local development

```bash
# database
docker run -d --name mos-pg -e POSTGRES_DB=manufactureos -e POSTGRES_USER=manufactureos \
  -e POSTGRES_PASSWORD=manufactureos -p 5432:5432 postgres:16-alpine

# backend (dev profile: seed data + dev JWT secret)
cd backend && SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run

# frontend (proxies /api to http://localhost:8080; override with VITE_API_PROXY_TARGET)
cd frontend && npm install && npm run dev      # http://localhost:5173
```

### Configuration

| Variable | Purpose | Default |
|---|---|---|
| `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` | PostgreSQL | localhost / manufactureos |
| `JWT_SECRET` | HS256 signing key, ≥ 32 bytes; app refuses to start otherwise | none in prod |
| `CORS_ALLOWED_ORIGINS` | Comma-separated origins | `http://localhost:5173` |
| `SPRING_PROFILES_ACTIVE` | `prod` (default) or `dev` | `prod` |
| `MANUFACTUREOS_SEED_ENABLED` | Demo data | `false` |
| `manufactureos.scrap.approval-threshold` | Scrap quantity needing manager approval | `50` |

## Roles

`ADMIN`, `PRODUCTION_MANAGER`, `SUPERVISOR`, `QUALITY_INSPECTOR`, `INVENTORY_MANAGER`, `WORKER`. Authorities are the bare role names; endpoints use `@PreAuthorize`. The UI hides what a role cannot use, but the server is the enforcement point.

## Architecture

Layered: `controller → service → repository`, DTOs at the edge, constructor injection throughout, entities never returned directly.

- **Auth:** short-lived HS256 access JWT + opaque, hashed, rotating refresh tokens (revoked on logout). Password reset issues a token; **delivery is not implemented — the token is only written to the server log**, so wire in email before real use.
- **Errors:** every failure returns `{timestamp,status,error,message,path}`. No stack traces or database details leak. Conflicts carry stable error codes.
- **Migrations:** a single Flyway script, `V1__schema.sql`; Hibernate runs in `validate` mode.

### Concurrency and integrity

- Inventory, machines, production outputs and purchase orders are mutated under `SELECT … FOR UPDATE` (`PESSIMISTIC_WRITE`), so two simultaneous reservations cannot oversell stock. `MaterialReservationConcurrencyIT` races reservations of 70 and 50 against 100 units and asserts exactly one succeeds.
- Optimistic `@Version` on contended aggregates; idempotency keys on production reporting so a retried request cannot double-count output.
- PostgreSQL exclusion (GIST) constraints back the overlap checks for machine tasks and worker assignments, so the guarantee holds even if application checks are bypassed.
- The audit log and inventory-transaction ledger are append-only, enforced by triggers.
- Notifications are de-duplicated with a unique partial index on `dedupe_key`.

### Domain flow

1. **BOM** versions go DRAFT → PENDING_APPROVAL → ACTIVE (approving archives the previous ACTIVE version).
2. **Production order** goes through plan → submit → approve → reserve materials (optionally partial) → ready → start/pause/resume → report output → complete. MRP shows required vs available vs shortage per material.
3. **Quarantine-first finished goods:** reported output lands in a QUARANTINED batch. Finalizing a quality inspection moves only the *passed* quantity to FINISHED_GOOD; the failed quantity stays quarantined until reworked or scrapped.
4. **Scrap** of at least the configured threshold requires ADMIN or PRODUCTION_MANAGER.
5. **Purchasing:** PO draft → submitted → approved → (partially) received; receiving goes through the same inventory receive path as manual receipts.
6. **Scheduler** (`OperationsScheduler`) raises low-stock, maintenance-due and production-delay notifications; rates are configurable via `manufactureos.scheduler.*-rate-ms`.

## API overview

All under `/api`; full contract in Swagger UI.

`/auth` · `/users` · `/factories`, `/departments`, `/production-lines`, `/workstations`, `/storage-locations`, `/shifts`, `/suppliers`, `/products`, `/materials` · `/boms` · `/inventory` · `/production-orders` · `/machines`, `/machine-tasks`, `/maintenance` · `/workers`, `/worker-skills`, `/worker-assignments` · `/quality/{inspections,defects,rework,scrap}`, `/quality/criteria` · `/purchase-orders` · `/notifications` · `/dashboard/{role}` · `/reports/{production,production/output,machines,quality,defects,workers}` (CSV)

## Tests

```bash
cd backend && mvn clean verify
```

Unit tests: MRP calculation, JWT issuing/validation, quality finalization. Integration test: concurrent material reservation against real PostgreSQL via Testcontainers (requires Docker).

## Known limitations

- Backend never compiled or run by the author (see top).
- Quality screens take an **output ID** typed by hand; there is no output picker yet.
- Password-reset delivery is a log line.
- No first-admin bootstrap outside seed mode.
- The frontend bundle is a single ~760 kB chunk (no route-level code splitting).
- Single-factory assumptions in a few UI selectors; multi-tenant isolation is not implemented.

## Repository layout

```
manufacture-os/
├── backend/            Spring Boot service (+ Dockerfile)
├── frontend/           React SPA (+ Dockerfile, nginx.conf)
├── docker-compose.yml
├── .env.example
└── README.md
```
