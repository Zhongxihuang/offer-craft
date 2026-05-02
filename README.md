<div align="center">

# OfferCraft

**Turn a job description and a resume into an actionable career decision and interview preparation plan.**

OfferCraft helps candidates decide whether a role is worth applying to, identify the gaps that matter most, and turn those gaps into short-term preparation actions.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1.0-2B6CB0)](https://docs.langchain4j.dev/)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[中文文档](README-zh-CN.md) | [Demo](#demo) | [Local Run](#local-run) | [API](#api)

</div>

---

## What is OfferCraft?

For many candidates, the hard part is not getting a generic piece of advice. The harder question is whether a role is worth the effort, which gaps matter most, and what to prepare in the next few days.

OfferCraft organizes that task into a staged workflow. It first understands the role, then analyzes candidate evidence, ranks the most important gaps, generates an apply recommendation, and supports follow-up refinement.

The current demo and evaluation cases focus on AI product, product management, AI application, and technical roles because these roles require a visible mix of business judgment, technical fluency, project evidence, and interview storytelling. The workflow itself can accept any job description and candidate profile.

## Core Outputs

- **Apply recommendation**: apply now, apply after reframing, prepare first, or redirect effort.
- **Role requirements**: role direction, must-have requirements, keywords, and interview focus areas.
- **Candidate evidence**: strengths, transferable experience, missing signals, and risk areas.
- **Three key gaps**: the most important gaps, why they matter, and how to prepare for them.
- **Confidence notes**: which conclusions are supported by direct evidence and which need more information.
- **Preparation plan**: short-term actions that can be completed within 3-7 days.

## Main Capabilities

### 1. Role Understanding

Extracts role positioning, hard requirements, nice-to-have signals, keywords, and interview focus areas from a job description.

### 2. Candidate Analysis

Identifies evidence, transferable strengths, project experience, and missing signals from a resume or candidate profile.

### 3. Gap Ranking

Ranks the three most important gaps by hiring impact, evidence weakness, and interview risk.

### 4. Interview Preparation

Generates short-term preparation actions, resume framing advice, behavioral interview prompts, and risk-focused practice areas.

### 5. Follow-up and Refinement

Users can ask follow-up questions based on a saved analysis, add new evidence, and generate updated versions.

### 6. Upload and Restore

Supports pasted text and PDF/TXT/Markdown uploads. Workflow results are stored in a local database and can be restored after backend restart.

## Product Flow

```text
1. Paste or upload a job description
2. Paste or upload a resume / candidate profile
3. Add target role, level, company, and focus areas
4. Run the analysis
5. Review apply recommendation, key gaps, confidence, and prep plan
6. Ask follow-up questions or refine the analysis with more information
```

## Design Principles

- **Workflow first**: generate a structured analysis before follow-up conversation.
- **Evidence first**: expose supporting evidence, missing signals, and confidence notes.
- **Action first**: turn gaps into concrete preparation tasks.
- **Local first**: run the core demo without external model keys.
- **Extensible**: search, retrieval, and tool calling are enhancements, not blockers for the core workflow.

<a id="demo"></a>

## Demo

The repository includes a reproducible demo scenario for quickly testing the full workflow.

**Canonical scenario**: Senior AI Product Manager for an enterprise GenAI product.

This scenario tests requirement extraction, candidate evidence analysis, platform thinking, governance awareness, and interview preparation quality. The candidate has strong product and analytics experience, but visible gaps in LLM platform depth, enterprise AI governance, and technical storytelling.

### Demo Assets

| File | Purpose |
|---|---|
| [job-description.md](docs/demo/ai-pm-canonical/job-description.md) | Sample job description |
| [candidate-resume.md](docs/demo/ai-pm-canonical/candidate-resume.md) | Sample candidate profile |
| [workflow-request.json](docs/demo/ai-pm-canonical/workflow-request.json) | API request example |
| [workflow-response.sample.json](docs/demo/ai-pm-canonical/workflow-response.sample.json) | Curated sample response |
| [support-chat-followups.md](docs/demo/ai-pm-canonical/support-chat-followups.md) | Follow-up examples |
| [demo-script.md](docs/demo/ai-pm-canonical/demo-script.md) | Two-minute demo script |

### Screenshots

- [Intake page](docs/demo/ai-pm-canonical/screenshots/01-intake.png)
- [Workflow result](docs/demo/ai-pm-canonical/screenshots/02-workflow-result.png)
- [Follow-up chat](docs/demo/ai-pm-canonical/screenshots/03-support-chat.png)

The current version is designed for local demonstration. A public hosted demo is not available yet.

<a id="local-run"></a>

## Local Run

### Requirements

- JDK 21
- Maven 3.9+
- Node.js 18+
- npm

### Start the Backend

The default local mode does not require external model keys.

Windows PowerShell:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"

mvn.cmd "-Dmaven.repo.local=.m2\repository" test
mvn.cmd spring-boot:run
```

macOS / Linux:

```bash
mvn test
mvn spring-boot:run
```

Backend URL:

```text
http://localhost:8081/api
```

### Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

```text
http://localhost:5173
```

### Try the Demo

1. Open the frontend.
2. Use the sample job description and candidate profile.
3. Run the analysis.
4. Review the apply recommendation, key gaps, confidence notes, and preparation plan.
5. Continue with follow-up questions such as a 7-day preparation plan, resume framing, or mock interview prompts.

## Configuration

### Local Demo Mode

The local profile uses demo mode by default. It does not require external provider keys and can run the main workflow immediately.

### Provider Mode

To connect to a real model provider:

| Variable | Required | Description |
|---|---|---|
| `CAREER_AI_MODE` | optional | `demo` or `provider` |
| `DASHSCOPE_API_KEY` | required in provider mode | DashScope / Qwen model key |
| `BIGMODEL_API_KEY` | optional | Optional key for web search enhancement |

Windows PowerShell:

```powershell
$env:CAREER_AI_MODE="provider"
$env:DASHSCOPE_API_KEY="your_dashscope_key"
$env:BIGMODEL_API_KEY="optional_search_key"
mvn.cmd spring-boot:run
```

### Runtime and Security Settings

| Variable | Default | Description |
|---|---|---|
| `CAREER_WEB_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Allowed frontend origins |
| `CAREER_WORKFLOW_ACCESS_REQUIRE_TOKEN` | local: `false`, base config: `true` | Protect restore, refine, and version endpoints |
| `CAREER_WORKFLOW_READ_TOKEN` | empty | Token used by `X-Workflow-Access-Token` |
| `CAREER_WORKFLOW_MAX_EXTRACTED_TEXT_CHARS` | `200000` | Maximum extracted text length per document |
| `SPRING_H2_CONSOLE_ENABLED` | `false` | H2 console switch |

<a id="api"></a>

## API

Base URL:

```text
http://localhost:8081/api
```

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/career/workflow/analyze` | Analyze pasted job description and resume/profile |
| `POST` | `/career/workflow/analyze-upload` | Analyze uploaded PDF/TXT/Markdown inputs |
| `POST` | `/career/workflow/compare` | Compare 2-5 target roles |
| `GET` | `/career/workflow/{workflowId}` | Restore a saved workflow |
| `GET` | `/career/workflow/{workflowId}/versions` | List workflow versions |
| `POST` | `/career/workflow/{workflowId}/refine` | Refine an existing workflow |
| `GET` | `/ai/chat` | Streaming follow-up chat |

### Text Analysis Example

```json
{
  "locale": "en",
  "targetRole": "AI Product Manager",
  "targetLevel": "Intern",
  "companyName": "Example Company",
  "jobDescription": "Paste job description here...",
  "candidateProfile": "Paste resume or candidate profile here...",
  "focusAreas": ["interview strategy", "technical fluency"],
  "includeCompanyResearch": true
}
```

### Upload Analysis Example

```powershell
curl.exe -X POST "http://localhost:8081/api/career/workflow/analyze-upload" `
  -F "locale=en" `
  -F "targetRole=AI Product Manager" `
  -F "jobDescriptionText=Paste job description here" `
  -F "candidateProfileFile=@docs/demo/ai-pm-canonical/candidate-resume.md;type=text/markdown"
```

### Follow-up Example

```text
GET /api/ai/chat?memoryId=demo-1&workflowId={workflowId}&locale=en&message=Turn the most important gaps into a 7-day prep plan
```

### Error Envelope

```json
{
  "timestamp": "2026-04-30T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed.",
  "path": "/api/career/workflow/analyze",
  "details": [
    {
      "field": "jobDescription",
      "message": "jobDescription is required."
    }
  ]
}
```

## Tech Stack

| Layer | Stack |
|---|---|
| Frontend | Vue 3, Vite, vue-i18n, Axios, EventSource |
| Backend | Java 21, Spring Boot 3.5, Spring MVC, Spring Data JPA |
| AI Orchestration | LangChain4j typed AI services, staged workflow orchestration |
| Model Runtime | Local deterministic demo mode, DashScope / Qwen provider mode |
| Retrieval and Tools | Optional RAG, MCP web search, tool calling |
| Persistence | H2 file database, local upload storage |
| Validation | Jakarta Validation, unified API error envelope |
| Testing | JUnit 5, WebMvcTest, Spring Boot integration tests, demo outcome eval |

## Architecture

```text
Vue frontend
  -> Spring Boot REST API
    -> Career workflow application service
      -> Job description parsing
      -> Candidate analysis
      -> Gap analysis
      -> Interview preparation generation
      -> Confidence and evidence annotation
    -> H2 workflow persistence
    -> Local upload storage
    -> Optional retrieval / search / tool capabilities
```

The workflow is split into stages with clear responsibilities. This makes the output easier to inspect, test, update, and explain than a single free-form response.

## Project Structure

```text
.
|-- frontend/
|   |-- src/api/                 # Frontend API clients
|   |-- src/components/          # Form, result view, follow-up panel
|   `-- src/i18n/                # Chinese / English UI copy
|-- src/main/java/com/workspace/codeforgeai/
|   |-- ai/                      # Chat, retrieval, tools, model config
|   |-- career/api/              # Career workflow controller and DTOs
|   |-- career/demo/             # Local demo-mode implementations
|   |-- career/jd/               # Job description parsing
|   |-- career/candidate/        # Candidate analysis
|   |-- career/gap/              # Gap analysis
|   |-- career/interview/        # Interview preparation generation
|   |-- career/workflow/         # Orchestration, persistence, upload, confidence
|   `-- common/                  # Error envelope and localization support
|-- src/main/resources/
|   |-- application.yml
|   |-- application-local.yml
|   |-- messages_en.properties
|   `-- messages_zh_CN.properties
|-- docs/demo/ai-pm-canonical/   # Demo inputs, sample output, screenshots
|-- docs/product/                # Product notes, eval cases, demo talk track
|-- scripts/                     # Local smoke scripts
|-- pom.xml
`-- README.md
```

## Testing

Backend:

```bash
mvn test
```

Frontend:

```bash
cd frontend
npm run build
```

The backend test suite covers controllers, demo-mode integration, upload and persistence, security boundaries, and provider-mode tests that run only when credentials are available.

## Security and Limitations

Implemented safeguards:

- CORS origin allowlist.
- H2 console disabled by default.
- Upload whitelist for PDF/TXT/Markdown.
- Upload path normalization and path traversal checks.
- Extracted text length limits.
- Optional token protection for restore, refine, and version endpoints.
- Provider request/response logging disabled by default to avoid leaking prompts or keys.

Current limitations:

- No account system or multi-tenant ownership model yet.
- H2 and local file storage are intended for local demo and single-node use, not public production.
- Uploaded files are not virus-scanned or automatically redacted for personal information.
- Follow-up chat history is not persisted as a full conversation store.
- Search, retrieval, and tool calling are enhancement capabilities. If unavailable, the core workflow degrades instead of blocking the analysis.

## Roadmap

- Candidate workspace with saved analyses and role comparison history.
- Stronger follow-up loop where new evidence creates a versioned analysis update.
- Production-ready storage with PostgreSQL and object storage.
- Authentication, workflow ownership, file retention, and deletion controls.
- Larger bilingual evaluation set for output quality and hallucination resistance.

## Related Docs

- [Chinese PRD](docs/product/chinese-prd.md)
- [Outcome eval cases](docs/product/eval-cases.md)
- [Demo talk track](docs/product/interview-talk-track.md)
- [Launch checklist](docs/product/launch-checklist.md)

## License

OfferCraft is open sourced under the [MIT License](LICENSE).
