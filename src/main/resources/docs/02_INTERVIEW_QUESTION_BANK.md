---
doc_id: interview-question-bank
title: Interview Question Bank (with Answer Skeletons)
tags: [interview, questions, dsa, system-design, backend, behavioral, security, data]
last_updated: 2026-01-08
---

# Interview Question Bank (with Answer Skeletons)

Use this as a retrieval source to generate practice drills, mock interviews, and targeted prep plans.

## How to drill effectively
- Timebox: 25–40 minutes per question.
- Always produce: **final answer**, **complexity**, **tests**, **edge cases**.
- After solving: do a 5-minute “teach-back” explanation.

---

# Section A — DSA / Coding (high-frequency)

## A1. Hash map patterns
### Question
Given an array, find pairs/groups meeting a condition (two-sum, k-sum, anagram grouping).

### What interviewers look for
- Correct use of dictionary/map
- Handling duplicates and edge cases
- Complexity awareness

### Answer skeleton
1) Use a map to store counts or seen values  
2) Iterate once; compute complement/feature key  
3) Validate and return result

### Common pitfalls
- Off-by-one
- Mutating map incorrectly
- Forgetting duplicates

## A2. Two pointers
### Question
Sorted array: find pair, remove duplicates, reverse string, partition.

### Skeleton
- Initialize `l=0, r=n-1`
- Move pointers based on invariant
- Prove termination + correctness

## A3. Sliding window
### Question
Longest substring with constraint, max sum subarray of size k, etc.

### Skeleton
- Expand right pointer
- While constraint violated, shrink left
- Track best result

## A4. Stack / monotonic stack
### Question
Valid parentheses, next greater element, daily temperatures.

### Skeleton
- Stack stores indices or partial states
- While stack violates monotonicity, pop + resolve

## A5. BFS / DFS
### Question
Graph traversal, shortest path unweighted, islands/grid.

### Skeleton
- Represent adjacency
- Visited set
- BFS for shortest unweighted; DFS for components

## A6. Binary search
### Question
Find boundary, first/last occurrence, search rotated array.

### Skeleton
- Define predicate `P(mid)`
- Maintain invariant: left false, right true (or vice versa)
- Return boundary index

---

# Section B — Backend Engineering

## B1. Design a REST API for X
### Prompts
- Design endpoints for tasks/orders/messages
- Pagination, filtering, sorting
- Errors and versioning

### Signals
- Clear resource naming
- Consistent status codes
- Practical pagination (cursor or offset)
- Idempotency where needed

### Answer skeleton
1) Requirements (MVP + non-goals)  
2) Resources & endpoints  
3) Data model  
4) Auth & permissions  
5) Error model  
6) Observability

## B2. Auth: JWT vs sessions
### What to cover
- Threats: token theft, replay, CSRF, XSS
- Revocation strategies
- Cookie flags, rotation

## B3. Database indexing
### Question
Why is this query slow? How to fix?

### Skeleton
1) Identify access pattern  
2) Add index on selective columns  
3) Consider composite index order  
4) Validate with query plan  
5) Tradeoffs: write cost, storage

## B4. Transactions and consistency
### Prompts
- Avoid double charge
- Inventory reservation
- Concurrent updates

### Signals
- Understand atomicity
- Use unique constraints
- Idempotency keys
- Isolation level awareness

## B5. Caching strategy
### Cover
- What to cache (read-heavy, stable)
- TTL vs invalidation
- Stampede protection
- Consistency tradeoffs

## B6. Rate limiting
### Cover
- Token bucket / leaky bucket
- Per-user vs per-IP
- Handling burst traffic
- Metrics + alerts

---

# Section C — System Design (high frequency)

## C1. URL shortener
### Key topics
- API design
- unique ID generation
- storage schema
- read/write scaling
- abuse prevention

## C2. Rate limiter service
### Key topics
- centralized vs distributed
- consistency model
- storage: Redis-like counters
- latency and fallback behavior

## C3. Feed / timeline
### Key topics
- fan-out on write vs read
- ranking pipeline
- caching
- pagination correctness

## C4. Chat service
### Key topics
- WebSocket vs long polling
- message ordering
- storage + delivery receipts
- offline handling

### System design answer template
1) Requirements + constraints  
2) High-level architecture  
3) Data model  
4) APIs  
5) Scaling strategy  
6) Reliability + failure modes  
7) Observability  
8) Tradeoffs

---

# Section D — Behavioral (use STAR/SAO)

## D1. Tell me about yourself
### Structure (60–90 sec)
- Present: role focus + strengths
- Past: 1–2 key experiences
- Future: what you want next and why this company

## D2. Conflict with a teammate
### Signals
- maturity, accountability, communication, outcomes

### STAR skeleton
- Situation: context + stakes  
- Task: your responsibility  
- Action: what you did (communication, alignment, compromise)  
- Result: measurable outcome + learning  

## D3. Failure / mistake
### Signals
- ownership, postmortem mindset, prevention measures

## D4. Ambiguity
### Signals
- scoping, asking good questions, risk management

---

# Section E — Security / AppSec (if relevant)

## E1. OWASP Top 10 walkthrough
### Prompt
Given a web app, identify top risks and mitigations.

### Signals
- threat model thinking
- practical mitigations (validation, authZ, CSRF, logging)
- secure defaults

## E2. Secrets management
### Cover
- avoid committing secrets
- rotation
- least privilege
- environment separation

## E3. Logging and incident response readiness
### Cover
- what to log (security events)
- redaction
- correlation IDs
- evidence collection

---

# Section F — Data / Analytics (if relevant)

## F1. SQL screen
### Prompts
- cohort retention
- conversion funnel
- revenue by segment
- window functions

## F2. Metrics design
### Signals
- avoid vanity metrics
- clear definitions
- guardrail metrics
- segmentation

## F3. Experimentation
### Cover
- hypothesis, power, duration
- SRM checks
- interpreting results responsibly
