---
doc_id: rag-pack-index
title: RAG Knowledge Pack Index
tags: [rag, index, navigation]
last_updated: 2026-01-08
---

# RAG Knowledge Pack

This folder contains structured markdown notes designed for retrieval-augmented generation (RAG).

## Files
- **01_PROGRAMMING_STUDY_ROUTE.md** — multi-track programming study routes (backend / data / security), with milestones and checklists.
- **02_INTERVIEW_QUESTION_BANK.md** — categorized interview question bank with expected signals, pitfalls, and answer skeletons.
- **03_JOB_SEEKING_PLAYBOOK.md** — end-to-end job seeking guide: positioning → resume/portfolio → networking → interviews → offer.
- **04_PROJECT_LEARNING_ADVICE.md** — project-based learning playbook: MVP → iterations → repo structure → evaluation → portfolio packaging.
- **05_TEMPLATES_AND_CHECKLISTS.md** — ready-to-copy templates: README, resume bullets, outreach, interview prep, project checklists.

## Suggested RAG chunking
- Chunk by `##` and `###` headings.
- Prefer retrieving sections that contain:
  - explicit checklists
  - question-answer formats
  - step-by-step plans
  - templates/snippets

## Retrieval tips
If a user asks:
- “How do I learn X?” → retrieve **01** and **04**.
- “Interview questions for Y?” → retrieve **02**.
- “Help me find a job” → retrieve **03** and **05**.
