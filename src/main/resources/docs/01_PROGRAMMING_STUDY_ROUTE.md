---
doc_id: programming-study-route
title: Programming Study Route (Project-Based)
tags: [learning, programming, roadmap, backend, data, security]
last_updated: 2026-01-08
---

# Programming Study Route (Project-Based)

This guide gives practical learning routes with **deliverables**. Pick one primary track, but keep the fundamentals.

## How to use this route
- Prefer **projects over tutorials**: every week ends with a repo artifact you can demo.
- Keep a **learning log**: what you learned, what broke, how you fixed it.
- Add **tests + CI** early; recruiters love predictable engineering.

---

## Track selector

### Track A: Backend Engineer (recommended default)
You like APIs, databases, auth, reliability, and shipping features.

### Track B: Data / Analytics Engineer
You like data pipelines, metrics, dashboards, experimentation, and SQL.

### Track C: Security / AppSec
You like threat modeling, secure coding, logging, and defense-in-depth.

---

# Universal foundation (Weeks 1–2)

## Week 1: Tooling + fundamentals
**Goal:** become productive fast in any repo.

### Deliverables
- A GitHub repo with:
  - `README.md` (goal, setup, usage)
  - basic unit tests
  - lint/format config
  - CI pipeline (run tests + lint)

### Checklist
- [ ] Git: branch, commit hygiene, PR flow
- [ ] Debugging: breakpoints, logs, repro steps
- [ ] Python or Java basics (choose one primary)
- [ ] Data structures: array/list, map/dict, set, stack/queue
- [ ] Complexity: Big-O + tradeoffs

### Practice tasks
- Implement 5 small functions with tests:
  - parse/validate input
  - frequency counter
  - two-sum variant
  - basic file IO
  - error handling patterns

## Week 2: HTTP + APIs basics
**Goal:** understand request/response, REST conventions, and auth basics.

### Deliverables
- A small API service with 3 endpoints (CRUD), plus tests.

### Checklist
- [ ] HTTP methods, status codes, idempotency
- [ ] JSON schemas (validation)
- [ ] Pagination and filtering
- [ ] Auth overview: API keys vs sessions vs JWT (conceptual)

---

# Track A: Backend Engineer (Weeks 3–12)

## Week 3: Database fundamentals (SQL)
**Goal:** be dangerous with schema + queries.

### Deliverables
- Design a simple schema (3–5 tables) + migrations
- 10 queries demonstrating joins, grouping, indexes

### Checklist
- [ ] normalization basics
- [ ] indexes: when and why
- [ ] transactions and isolation (conceptual)
- [ ] connection pooling concept

## Week 4: Backend project MVP (FastAPI / Spring Boot)
**Goal:** ship a usable service.

### Suggested MVP ideas
- Task manager API
- Personal finance tracker API
- Library / inventory system
- Content aggregator API

### Acceptance criteria
- [ ] CRUD endpoints
- [ ] validation + error responses
- [ ] persistence (Postgres or SQLite)
- [ ] basic tests (unit + integration)
- [ ] Docker run (optional but valuable)

## Week 5: Authentication + authorization
**Goal:** implement real access control.

### Deliverables
- Login + role-based permission checks (RBAC)
- Security notes in README

### Checklist
- [ ] password hashing (bcrypt/argon2)
- [ ] session/JWT basics
- [ ] authorization checks per endpoint
- [ ] threat model: what can go wrong?

## Week 6: Observability (logging, metrics, tracing)
**Goal:** make the system debuggable in production.

### Deliverables
- structured logs (JSON)
- request IDs, latency logging
- dashboard-ready metrics (even if local)

### Checklist
- [ ] define “golden signals” (latency, traffic, errors, saturation)
- [ ] log levels and sensitive-data redaction

## Week 7: Reliability patterns
**Goal:** learn real-world production patterns.

### Topics
- retries with backoff
- timeouts
- rate limiting
- circuit breaker (conceptual)

### Deliverables
- implement rate limiting + timeouts
- write failure-mode tests

## Week 8: Caching + queues (intro)
**Goal:** understand performance and async work.

### Deliverables
- add caching to one read endpoint
- add background job for a slow task

## Weeks 9–10: System design practice + refactor
**Goal:** explain your architecture and scale it logically.

### Deliverables
- architecture diagram in README
- “design doc” with tradeoffs

## Weeks 11–12: Portfolio polish + interview readiness
**Goal:** turn project into proof.

### Deliverables
- a 2-minute demo script
- “Impact” section (what it improves, measured results)
- final cleanup: docs, tests, CI, security checklist

---

# Track B: Data / Analytics Engineer (Weeks 3–12)

## Week 3: SQL mastery + data modeling
**Deliverables**
- star schema for an imaginary product (users, events, revenue)
- 15 analytical queries

## Week 4: ETL/ELT mini pipeline
**Deliverables**
- ingest CSV/JSON → clean → store → query
- data quality checks

## Week 5: Metrics and dashboards (lightweight)
**Deliverables**
- define north-star metric + 5 supporting metrics
- create a notebook/report that answers product questions

## Week 6: Experimentation basics
**Deliverables**
- A/B test checklist + analysis notebook
- pitfalls: SRM, peeking, multiple testing

## Weeks 7–8: Productionizing
**Deliverables**
- scheduled jobs
- monitoring for failures
- versioned datasets

## Weeks 9–12: Portfolio project + interview prep
**Deliverables**
- case-study style README
- “business story” + “engineering story”

---

# Track C: Security / AppSec (Weeks 3–12)

## Week 3: Secure coding fundamentals
**Deliverables**
- a checklist mapped to OWASP Top 10
- implement input validation + secure defaults

## Week 4: Threat modeling (STRIDE)
**Deliverables**
- DFD diagram
- STRIDE table with mitigations

## Week 5: Auth, secrets, and session security
**Deliverables**
- hardened auth flows
- secrets management notes

## Week 6: Logging, detection, and incident readiness
**Deliverables**
- security event logging
- incident “evidence bundle” script (optional)

## Week 7: Testing security
**Deliverables**
- SAST/linters in CI
- basic fuzzing/mutation tests (intro)

## Weeks 8–12: Secure service portfolio
**Deliverables**
- “security posture” section in README
- attack scenarios + mitigations + tests

---

## Minimal weekly cadence (copy/paste)
- Mon: learning + tiny exercise
- Tue: implement feature A
- Wed: implement feature B
- Thu: tests + refactor
- Fri: docs + demo + retrospective
- Weekend: optional stretch goal

## Proof-of-skill checklist (universal)
- [ ] runnable setup in 5 minutes
- [ ] tests and CI
- [ ] clear README with demo
- [ ] meaningful commit history
- [ ] a design doc or architecture notes
