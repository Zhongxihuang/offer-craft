<div align="center">

# AI Career Decision & Interview Prep Agent

### Workflow-first career decision agent for JD fit, Top 3 gaps, and interview prep

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1.0-2B6CB0)](https://docs.langchain4j.dev/)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)

[Quick Local Demo](#quick-local-demo) | [Core Workflow](#core-workflow) | [API](#api) | [Security Defaults](#security-defaults) | [Portfolio Materials](#portfolio-materials)

</div>

## What Problem This Solves

很多候选人看到 AI 产品、平台产品或 GenAI 相关岗位时，真正卡住的不是“问 AI 一个问题”，而是下面这条决策链：

1. 这份 JD 到底在招什么能力？
2. 我的简历证据够不够支撑这些要求？
3. 我该不该投，还是应该先补表达或先准备？
4. 最影响结果的 Top 3 gaps 是什么？
5. 接下来 3-7 天应该具体做什么？

这个项目不是泛聊天助手，而是把 **JD 解析 -> 候选人分析 -> Gap 排序 -> 面试准备 -> 结果追问/更新** 做成一个可复用的 workflow。

## Why This Is An Agent, Not Just A Chatbot

The application uses typed, staged workflow agents:

1. `JD Parsing Agent` extracts role signals, must-haves, keywords, and interview themes.
2. `Candidate Analysis Agent` structures strengths, evidence, and missing signals from a resume or profile.
3. `Gap Analysis Agent` ranks the gaps by hiring impact, evidence weakness, and interview risk.
4. `Interview Prep Agent` converts findings into resume framing, mock questions, and a 3-7 day action plan.
5. `Support Chat` links back to the saved `workflowId`, so follow-up questions continue from the generated artifact instead of starting from zero.

## Core Workflow

```text
JD parsing -> candidate analysis -> gap analysis -> interview prep generation -> workflow-linked follow-up
```

The main output bundle includes:

- apply verdict: apply now, reframe first, prep first, or redirect effort
- JD must-haves and interview focus areas
- candidate strengths, evidence, and missing signals
- Top 3 gaps that most affect the outcome
- why each gap matters, evidence weakness, and prep action
- confidence summary and clarification questions
- 3-7 day preparation checklist
- optional company research suggestions when provider tools are available

## Quick Local Demo

The default local profile is demo-friendly. You can run the full workflow without provider keys.

### Backend

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn.cmd "-Dmaven.repo.local=.m2\repository" test
mvn.cmd spring-boot:run
```

The backend runs at `http://localhost:8081/api` and uses:

- `career.ai.mode=demo`
- H2 file database under `./data`
- local upload storage under `./data/uploads`
- no external model, RAG, or search key requirement

### Frontend

```powershell
cd frontend
npm.cmd install
npm.cmd run dev
```

Open `http://localhost:5173`.

Recommended live demo:

1. Open [docs/demo/ai-pm-canonical/job-description.md](docs/demo/ai-pm-canonical/job-description.md).
2. Open [docs/demo/ai-pm-canonical/candidate-resume.md](docs/demo/ai-pm-canonical/candidate-resume.md).
3. Paste both into the workflow form and run analysis.
4. Review apply verdict, confidence, Top 3 gaps, and 3-7 day action plan.
5. Click support chat and ask: `把 Top 3 gaps 转成 7 天准备计划`.

Static portfolio artifact:

- [workflow-request.json](docs/demo/ai-pm-canonical/workflow-request.json)
- [workflow-response.sample.json](docs/demo/ai-pm-canonical/workflow-response.sample.json)
- [support-chat-followups.md](docs/demo/ai-pm-canonical/support-chat-followups.md)
- [demo-script.md](docs/demo/ai-pm-canonical/demo-script.md)

Live model/provider results may vary; `workflow-response.sample.json` is the canonical portfolio artifact.

## Windows JDK 21 Notes

If the Maven wrapper is unavailable or broken on Windows, use `mvn.cmd` directly.

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
mvn.cmd -version
mvn.cmd compile
mvn.cmd "-Dmaven.repo.local=.m2\repository" test
```

Using `-Dmaven.repo.local=.m2\repository` keeps dependencies inside the project workspace and avoids user-level Maven repository permission issues.

## Provider Mode

Provider mode enables real model and optional search/RAG capabilities.

```powershell
$env:CAREER_AI_MODE="provider"
$env:DASHSCOPE_API_KEY="your_dashscope_key"
$env:BIGMODEL_API_KEY="optional_search_key"
$env:CAREER_WORKFLOW_ACCESS_REQUIRE_TOKEN="true"
$env:CAREER_WORKFLOW_READ_TOKEN="a_restore_token"
mvn.cmd spring-boot:run
```

Provider mode requires `DASHSCOPE_API_KEY`. Search/RAG/MCP are enhancement layers: when unavailable, the workflow should still return a useful result with visible fallback notes.

## Security Defaults

This is still a portfolio MVP, but the launch-readiness pass adds production-adjacent guardrails:

- CORS origins are configured through `career.web.allowed-origins`.
- CORS methods are limited to the currently used API methods: `GET`, `POST`, and `OPTIONS`.
- H2 console is disabled by default, including local demo mode. If you need it for debugging, set `SPRING_H2_CONSOLE_ENABLED=true` locally only.
- `./data`, `target`, `.m2`, `frontend/dist`, and `frontend/node_modules` are ignored.
- Uploads are limited to PDF, TXT, and MD.
- Uploaded file names reject control characters and overly long names.
- Extracted document text is capped by `CAREER_WORKFLOW_MAX_EXTRACTED_TEXT_CHARS` to reduce accidental oversized payloads.
- Text extraction rejects image-only PDFs because OCR is out of scope for v1.
- Uploaded file paths are normalized and must stay inside `./data/uploads/{workflowId}`.
- Restore endpoints can require `X-Workflow-Access-Token` when `CAREER_WORKFLOW_ACCESS_REQUIRE_TOKEN=true`.
- When workflow token protection is enabled, startup fails if `CAREER_WORKFLOW_READ_TOKEN` is missing.
- MCP request/response logging is disabled by default to avoid leaking provider keys, prompts, JD text, or resume text.

## Known Limitations

- No account system or multi-user tenant model yet.
- Restore is latest-workflow based on localStorage plus persisted backend result.
- Uploaded PDFs must contain extractable text; OCR is not supported.
- Provider quality depends on available model/search credentials.
- Support chat history is session memory, not a persisted conversation store.
- This is not an auto-apply tool; it stops at decision support and interview preparation.

## API

Base path: `http://localhost:8081/api`

### `POST /career/workflow/analyze`

Runs the JSON text workflow.

```json
{
  "locale": "zh-CN",
  "targetRole": "AI 产品经理",
  "targetLevel": "实习生",
  "companyName": "美团",
  "jobDescription": "粘贴 JD...",
  "candidateProfile": "粘贴简历或候选人背景...",
  "focusAreas": ["面试策略", "技术理解"],
  "includeCompanyResearch": true
}
```

### `POST /career/workflow/analyze-upload`

Runs the same workflow with multipart intake. Each slot must provide pasted text or a file; when both are provided, the file wins.

Supported files: `.pdf`, `.txt`, `.md`.

### `POST /career/workflow/compare`

Compares 2-5 target roles against the same candidate profile and returns a ranked shortlist.

### `GET /career/workflow/{workflowId}`

Restores a persisted workflow result. In token-protected mode, include:

```http
X-Workflow-Access-Token: your_restore_token
```

### `GET /api/ai/chat`

Streaming support chat. When `workflowId` is passed, the answer is grounded in the saved workflow artifact.

## Error Envelope

Errors use a stable JSON envelope:

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

Frontend localized handling covers `400`, `403`, `404`, `413`, `415`, `429`, `500`, and network errors.

## Architecture

```text
Vue 3 Frontend
  -> Workflow intake / result workspace / support chat
  -> Axios JSON + multipart API calls
  -> EventSource streaming chat

Spring Boot Backend
  -> CareerWorkflowController
  -> CareerWorkflowApplicationService
  -> CareerWorkflowOrchestrator
  -> JD / Candidate / Gap / Interview Prep AI services
  -> WorkflowSessionStore
  -> H2 persistence + local upload storage
```

Main packages:

- `src/main/java/com/workspace/codeforgeai/career/api`: request/response DTOs and REST controller
- `src/main/java/com/workspace/codeforgeai/career/workflow`: orchestration, persistence, upload handling, confidence annotation
- `src/main/java/com/workspace/codeforgeai/career/demo`: local deterministic demo-mode agents
- `src/main/java/com/workspace/codeforgeai/ai`: chat, RAG, MCP, tools, model configuration
- `frontend/src/components`: workflow intake, result view, support chat, header
- `frontend/src/api`: workflow and chat API helpers

## Portfolio Materials

These docs are written for AI PM / product manager internship interviews:

- [中文 PRD](docs/product/chinese-prd.md)
- [Outcome eval cases](docs/product/eval-cases.md)
- [与直接问 ChatGPT 的对比](docs/product/chatgpt-comparison.md)
- [2 分钟面试讲述](docs/product/interview-talk-track.md)
- [Launch checklist](docs/product/launch-checklist.md)

Suggested resume framing:

> 设计并实现 workflow-first AI Career Agent，将 JD 解析、候选人证据分析、Top 3 gap 排序和 3-7 天面试准备串成闭环，支持文件上传、H2 持久化、中英文切换、workflow-linked support chat 和 demo/provider 双运行模式。

## Verification

```powershell
git diff --check
mvn.cmd compile
mvn.cmd "-Dmaven.repo.local=.m2\repository" test
cd frontend
npm.cmd run build
```

Manual launch checklist:

1. Local demo starts without provider keys.
2. Text-only workflow returns decision summary, confidence, Top 3 gaps, and prep plan.
3. Upload workflow works for TXT/MD/PDF with extractable text.
4. Restore works after backend restart.
5. Token-protected restore returns `403` without `X-Workflow-Access-Token`.
6. Support chat follows up with workflow context.
7. Chinese/English switch updates UI immediately.

## License

This project is open sourced under the [MIT License](LICENSE).
