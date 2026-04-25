<div align="center">

# AI Career Decision & Interview Prep Agent

### Workflow-First Job-Fit Analysis and Interview Preparation

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1.0-2B6CB0)](https://docs.langchain4j.dev/)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)

[Getting Started](#getting-started) | [Quick Demo](#quick-demo) | [Core Workflow](#core-workflow) | [API](#api) | [Architecture](#architecture)

</div>

## User Problem

English:

Many AI career tools answer questions well but fail to guide a candidate through a concrete decision workflow:

1. Read the job description
2. Extract the real must-haves
3. Compare them against the candidate profile
4. Identify the gaps that actually affect hiring confidence
5. Turn those gaps into interview preparation and next actions

This project is designed around that workflow instead of stopping at chat.

中文：

很多 AI 求职工具虽然能回答问题，但并不能真正带着候选人走完一条清晰的决策工作流：

1. 读取岗位描述
2. 提炼真正影响招聘判断的核心要求
3. 对照候选人背景做证据分析
4. 找出会影响录用信心的关键差距
5. 把这些差距转成可执行的面试准备与下一步动作

这个项目的核心不是“陪聊”，而是把这条工作流真正做出来。

## Why This Is An Agent, Not Just A Chatbot

The application uses a staged agentic workflow with typed outputs:

1. `JD Parsing Agent` extracts role signals, must-haves, and likely interview themes
2. `Candidate Analysis Agent` structures strengths, evidence, and missing signals from a resume or profile
3. `Gap Analysis Agent` compares the candidate against the role and decides where confidence breaks down
4. `Interview Prep Agent` converts those findings into targeted prep, positioning advice, and next steps

The older streaming chat layer still exists, but it is now positioned as a support capability for follow-up questions rather than the main product.

## Core Workflow

`JD parsing -> candidate analysis -> gap analysis -> interview prep generation`

Example output bundle:

- structured job requirements
- candidate-fit summary
- prioritized gaps and risks
- interview preparation plan
- company research suggestions
- practical next steps

## Key Capabilities

- Java 21 + Spring Boot backend
- LangChain4j AI services for typed workflow stages
- RAG over local career and interview preparation documents
- MCP web search integration for fresh context
- tool-based interview question retrieval
- streaming support chat with session-isolated memory
- workflow-linked support chat that reuses the saved `workflowId` artifact bundle for follow-up explanations and prep
- upload intake for JD and candidate documents (`.pdf`, `.txt`, `.md`)
- H2-backed workflow persistence plus local file storage for uploaded source documents

## Quick Demo

Canonical scenario: `Senior AI Product Manager for an enterprise GenAI product`

The app now supports one-click language switching between `English` and `中文`. Existing saved workflow artifacts keep their original `contentLocale`; rerun the analysis to regenerate the artifact in the current language, while Support Chat replies in the active UI language.

项目现在支持 `English / 中文` 一键切换。已经生成并保存的 workflow artifact 会保留原始 `contentLocale`；如果你希望结果内容也切换语言，需要重新运行分析。Support Chat 会按照当前界面语言继续回复。

This repo includes a portfolio-ready demo pack under [`docs/demo/ai-pm-canonical`](docs/demo/ai-pm-canonical/).

### Live App Demo

1. Open the canonical [job description](docs/demo/ai-pm-canonical/job-description.md) and [candidate profile](docs/demo/ai-pm-canonical/candidate-resume.md).
2. Run the backend and frontend locally.
3. Paste the sample inputs into the workflow intake form and submit.
4. Review the structured workflow result.
5. Enter Support Chat and ask a follow-up grounded in the saved `workflowId`.

Minimal request example:

```bash
curl -X POST http://localhost:8081/api/career/workflow/analyze \
  -H "Content-Type: application/json" \
  -d @docs/demo/ai-pm-canonical/workflow-request.json
```

### Static Portfolio Demo

- Canonical request: [workflow-request.json](docs/demo/ai-pm-canonical/workflow-request.json)
- Canonical response artifact: [workflow-response.sample.json](docs/demo/ai-pm-canonical/workflow-response.sample.json)
- Workflow-linked chat examples: [support-chat-followups.md](docs/demo/ai-pm-canonical/support-chat-followups.md)
- Demo narration: [demo-script.md](docs/demo/ai-pm-canonical/demo-script.md)
- Screenshots:
  - [01-intake.png](docs/demo/ai-pm-canonical/screenshots/01-intake.png)
  - [02-workflow-result.png](docs/demo/ai-pm-canonical/screenshots/02-workflow-result.png)
  - [03-support-chat.png](docs/demo/ai-pm-canonical/screenshots/03-support-chat.png)

Live model results may vary, but [`workflow-response.sample.json`](docs/demo/ai-pm-canonical/workflow-response.sample.json) is the canonical portfolio artifact for this scenario.

## Architecture

### Main Modules

- `src/main/java/com/workspace/codeforgeai/ai`
  Shared infrastructure: models, RAG, MCP, tools, guardrails, and support chat
- `src/main/java/com/workspace/codeforgeai/ai/demo`
  Local deterministic fallback chat service for demo mode
- `src/main/java/com/workspace/codeforgeai/career/api`
  Workflow request/response DTOs and REST controller
- `src/main/java/com/workspace/codeforgeai/career/demo`
  Local deterministic fallback workflow stages for demo mode
- `src/main/java/com/workspace/codeforgeai/career/workflow`
  Workflow orchestrator, decision summary, and session store
- `src/main/java/com/workspace/codeforgeai/career/jd`
  JD parsing agent contract and output model
- `src/main/java/com/workspace/codeforgeai/career/candidate`
  Candidate analysis agent contract and output model
- `src/main/java/com/workspace/codeforgeai/career/gap`
  Gap analysis agent contract and output model
- `src/main/java/com/workspace/codeforgeai/career/interview`
  Interview prep agent contract and output model

### Request Flow

```text
POST /api/career/workflow/analyze
POST /api/career/workflow/analyze-upload
    -> CareerWorkflowController
    -> CareerWorkflowApplicationService
        -> CareerDocumentUploadService (multipart path only)
        -> CareerWorkflowOrchestrator
        -> JdParsingAiService
        -> CandidateAnalysisAiService
        -> GapAnalysisAiService
        -> InterviewPrepAiService
    -> WorkflowSessionStore
    -> Structured workflow response
```

### Support Capabilities

The project keeps the original strengths and repurposes them as support layers:

- `GET /api/ai/chat` for streaming follow-up chat
- RAG for interview and job-search playbooks
- MCP search for fresh company and market context
- tool calling for interview question retrieval
- multi-session memory for support conversations

The support chat now forms a closed loop with the main workflow: after a workflow run, the frontend carries the saved `workflowId` into the streaming chat endpoint so follow-up answers are grounded in the stored JD analysis, candidate analysis, gap analysis, and interview prep artifacts.

## API

### `POST /api/career/workflow/analyze`

Runs the workflow-first career agent with pasted JD and candidate text.

Example request:

```json
{
  "locale": "en",
  "targetRole": "Senior Backend Engineer",
  "targetLevel": "Senior",
  "companyName": "ExampleCorp",
  "jobDescription": "Paste the JD here...",
  "candidateProfile": "Paste the resume or candidate summary here...",
  "focusAreas": ["system design", "behavioral"],
  "includeCompanyResearch": true
}
```

Example response shape:

```json
{
  "workflowId": "uuid",
  "contentLocale": "en",
  "decisionSummary": {
    "fitLevel": "COMPETITIVE_WITH_GAPS",
    "recommendedPositioning": "Senior Backend Engineer"
  },
  "jdAnalysis": {},
  "candidateAnalysis": {},
  "gapAnalysis": {},
  "interviewPrep": {}
}
```

Standard validation error example:

```json
{
  "timestamp": "2026-04-11T12:00:00Z",
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

### `GET /api/career/workflow/{workflowId}`

Fetches the persisted artifact bundle for a previously executed workflow. This survives backend restarts because workflow results are stored in H2 and backed by a local upload directory.

If the workflow does not exist, the API returns a unified `404` error envelope:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Workflow not found."
}
```

### `GET /api/ai/chat`

Streaming support chat endpoint. This remains useful for follow-up questions, brainstorming, and ad hoc coaching after the structured workflow is complete.

Query parameters:

- `memoryId` required
- `message` required
- `workflowId` optional but recommended after a workflow run
- `locale` optional; defaults from `Accept-Language`

When `workflowId` is provided, the backend injects the saved workflow artifact bundle into the support chat context so the response can explain the fit call, elaborate on gaps, and suggest targeted next steps without restarting the analysis flow.

### `POST /api/career/workflow/analyze-upload`

Runs the same workflow with `multipart/form-data` so the user can upload a JD or candidate file instead of pasting text.

Supported file types:

- `.pdf`
- `.txt`
- `.md`

Upload behavior:

- each document slot can use pasted text or an uploaded file
- if both are provided for the same slot, the uploaded file wins
- text-based PDFs are supported in v1
- image-only PDFs are rejected because OCR is not included in this version

## Getting Started

### Prerequisites

- JDK 21+
- Maven 3.9+
- Node.js 18+
- npm 9+

### Runtime Modes

The backend now supports two explicit modes:

- `demo`  
  Local deterministic mode. No external keys are required, and the main workflow plus workflow-linked support chat still run end-to-end.
- `provider`  
  Uses DashScope/Qwen, optional MCP search, and optional RAG retrieval enhancements. Missing RAG or MCP no longer blocks startup, but `DASHSCOPE_API_KEY` is still required in this mode.

`local` profile defaults to demo mode through [`application-local.yml`](src/main/resources/application-local.yml).

### Run Backend In Local Demo Mode

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

This is the recommended default for portfolio demos. It starts with no provider keys and still supports:

- `POST /api/career/workflow/analyze`
- `POST /api/career/workflow/analyze-upload`
- `GET /api/career/workflow/{workflowId}`
- `GET /api/ai/chat`

### Run Backend In Provider Mode

macOS / Linux:

```bash
export CAREER_AI_MODE=provider
export DASHSCOPE_API_KEY=your_real_dashscope_key
export BIGMODEL_API_KEY=your_optional_bigmodel_key
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
$env:CAREER_AI_MODE="provider"
$env:DASHSCOPE_API_KEY="your_real_dashscope_key"
$env:BIGMODEL_API_KEY="your_optional_bigmodel_key"
.\mvnw.cmd spring-boot:run
```

In provider mode:

- missing `DASHSCOPE_API_KEY` fails fast with a clear startup error
- missing `BIGMODEL_API_KEY` disables MCP web search only
- retrieval initialization failures degrade to no-op retrieval instead of killing the app

If Maven is still using Java 17 on Windows, point it to JDK 21 before running:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="C:\Program Files\Java\jdk-21\bin;$env:Path"
.\mvnw.cmd test
```

Backend URL: `http://localhost:8081/api`

### Local Persistence

- workflow database: `./data/codeforge-ai-db*`
- uploaded source files: `./data/uploads/{workflowId}/`

Current persistence scope:

- workflow inputs
- extracted document text
- uploaded file metadata
- structured workflow response

Current v1 limitations:

- no OCR for scanned PDFs
- no saved analyses list UI yet
- frontend restores the latest saved `workflowId` only
- demo mode uses deterministic Java fallback logic for explainable local runs rather than live model output

### Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend URL: `http://localhost:5173`

### Language Switching

- Default locale:
  - browser language starting with `zh` -> `zh-CN`
  - everything else -> `en`
- The selected locale is stored in browser local storage as `careerAgent_locale`
- The header provides a one-click `中文 | EN` switch
- UI text switches immediately
- Saved workflow results keep their original language and show a notice if the UI language no longer matches
- New workflow analyses and new Support Chat replies use the current locale

## Knowledge Base

The local RAG corpus already supports the repositioned workflow:

- interview question bank
- job-seeking playbook
- project and portfolio advice
- templates and checklists

This lets the workflow stay grounded in reusable domain knowledge without changing the core stack.

## Why This Is Portfolio-Friendly

- The system has a clear product narrative
- The workflow is easy to explain in an AI PM interview
- The architecture cleanly separates orchestration, prompts, tools, retrieval, and support chat
- The implementation stays in Java and reuses Spring Boot + LangChain4j instead of hiding the logic in a larger rewrite

## Future Extensibility

- saved analyses list and history view
- OCR for scanned resume and JD PDFs
- role-specific scoring rubrics
- company-specific research packets
- mock interview mode grounded in the saved workflow artifact bundle

## Testing

```bash
./mvnw test
```

Windows PowerShell:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="C:\Program Files\Java\jdk-21\bin;$env:Path"
.\mvnw.cmd test
```

Frontend build check:

```bash
cd frontend
npm run build
```

Local demo smoke:

```powershell
.\scripts\demo-smoke.ps1
```

The smoke script runs backend tests, builds the frontend, starts the backend in demo mode, posts the canonical workflow request, verifies workflow restore, and checks workflow-linked support chat.

API-key-dependent provider tests are still gated behind environment variables, while the demo-mode integration suite runs without external credentials.

Common failure cases:

- Missing `jobDescription` or `candidateProfile` -> `400` with field-level validation details
- Missing upload sources for JD or candidate profile -> `400` with field-level validation details
- Unsupported upload type or image-only PDF -> `400` with a unified error envelope
- Malformed JSON request body -> `400` with a unified error envelope
- Unknown `workflowId` -> `404` with a unified error envelope
