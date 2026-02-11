<div align="center">

# Code Forge AI

### AI-Powered Software Engineering Coach

An intelligent conversational platform that combines LLM reasoning, retrieval-augmented generation, and real-time tool execution to accelerate developer growth — from first commit to signed offer letter.

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1.0-2B6CB0)](https://docs.langchain4j.dev/)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](#license)

[Getting Started](#-getting-started) · [Architecture](#-architecture) · [API Reference](#-api-reference) · [Contributing](#-contributing)

</div>

---

## Why Code Forge AI?

Most AI coding assistants stop at autocomplete. **Code Forge AI** goes further — it acts as an **engineering mentor** that understands your career context:

- **Write & debug code** with structured problem-solving and test-driven guidance
- **Plan your learning path** with milestone-driven roadmaps tailored to your target role
- **Prepare for interviews** with real questions pulled from live sources, not static datasets
- **Navigate your job search** end-to-end: résumé, portfolio, networking, negotiation

All of this is backed by a curated knowledge base, real-time web search, and purpose-built tooling — not just a raw LLM prompt.

---

## ✨ Features

| | Feature | Details |
|---|---------|---------|
| ⚡ | **Streaming Responses** | Token-by-token delivery via SSE for a natural, real-time conversation experience |
| 📚 | **RAG-Enhanced Answers** | Domain-specific knowledge base (study routes, interview banks, career playbooks) automatically retrieved and injected into context |
| 🔌 | **MCP Protocol Integration** | Live web search via Model Context Protocol — the AI can access up-to-date information beyond its training cutoff |
| 🛠️ | **Extensible Tool System** | Built-in interview question scraper; easily add new tools via LangChain4j's `@Tool` annotation |
| 🛡️ | **Input Guardrails** | Pluggable safety layer that intercepts and blocks unsafe or abusive input before it reaches the model |
| 🧠 | **Session-Isolated Memory** | Each conversation maintains independent context history with a configurable sliding window |
| 📊 | **Observability** | Full request/response/error lifecycle logging via `ChatModelListener` for debugging and monitoring |
| 📱 | **Responsive UI** | Clean, mobile-friendly chat interface built with Vue 3 Composition API |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                      Client  (Vue 3 + Vite)                  │
│                          :5173                               │
│              Proxy  /api  ──────────────►  Backend            │
└──────────────────────────┬───────────────────────────────────┘
                           │  Server-Sent Events
┌──────────────────────────▼───────────────────────────────────┐
│                   Application  (Spring Boot)                  │
│                          :8081/api                            │
│                                                               │
│   ┌─────────────┐    ┌──────────────────────────────────┐    │
│   │ AiController │───►│      CodeForgeAiService          │    │
│   │  GET /ai/chat│    │  (LangChain4j AI Services)       │    │
│   └─────────────┘    │                                    │    │
│                       │  ┌────────────┐  ┌─────────────┐ │    │
│                       │  │ Guardrails │  │ Chat Memory │ │    │
│                       │  └────────────┘  └─────────────┘ │    │
│                       │  ┌────────────┐  ┌─────────────┐ │    │
│                       │  │    RAG     │  │    Tools    │ │    │
│                       │  │ Retriever  │  │ + MCP Client│ │    │
│                       │  └────────────┘  └─────────────┘ │    │
│                       └──────────┬───────────────────────┘    │
│                                  │                             │
│   ┌──────────────────────────────▼──────────────────────────┐│
│   │              LLM Provider  (DashScope)                   ││
│   │     Qwen-Max (Chat)  ·  Qwen-Max (Stream)  ·  Embedding ││
│   └──────────────────────────────────────────────────────────┘│
└───────────────────────────────────────────────────────────────┘
          │                                       │
          ▼                                       ▼
   ┌─────────────┐                      ┌──────────────────┐
   │  RAG Docs   │                      │  External APIs   │
   │  (Markdown) │                      │  · Zhipu Search  │
   └─────────────┘                      │  · mianshiya.com │
                                        └──────────────────┘
```

---

## 🔧 Tech Stack

### Backend

| Component | Technology | Version |
|-----------|-----------|---------|
| Runtime | Java (OpenJDK) | 21 LTS |
| Framework | Spring Boot | 3.5.9 |
| Reactive Streaming | Spring WebFlux | 6.x |
| AI Orchestration | LangChain4j | 1.1.0 |
| LLM Provider | LangChain4j DashScope (Qwen) | 1.1.0-beta7 |
| Tool Protocol | LangChain4j MCP | 1.1.0-beta7 |
| Web Scraping | Jsoup | 1.20.1 |
| Code Generation | Lombok | latest |

### Frontend

| Component | Technology | Version |
|-----------|-----------|---------|
| Framework | Vue.js (Composition API) | 3.4+ |
| Build Tooling | Vite | 5.x |
| HTTP Client | Axios | 1.6+ |
| Streaming | EventSource (native) | — |

### AI & Data

| Model | Provider | Role |
|-------|----------|------|
| Qwen-Max | Alibaba DashScope | Primary chat & streaming model |
| text-embedding-v4 | Alibaba DashScope | Document embedding for RAG |
| Web Search | Zhipu AI (BigModel) | Real-time internet search via MCP |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Minimum Version |
|-------------|----------------|
| JDK | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| npm | 9+ |

### 1. Clone & Navigate

```bash
git clone https://github.com/your-org/code-forge-ai.git
cd code-forge-ai
```

### 2. Configure Credentials

Create or edit `src/main/resources/application-local.yml`:

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: ${DASHSCOPE_API_KEY}
      embedding-model:
        api-key: ${DASHSCOPE_API_KEY}
      streaming-chat-model:
        api-key: ${DASHSCOPE_API_KEY}

bigmodel:
  api-key: ${BIGMODEL_API_KEY}
```

<details>
<summary><strong>Where to get API keys</strong></summary>

| Provider | Sign-Up URL | Key Type |
|----------|-------------|----------|
| Alibaba DashScope | [dashscope.aliyun.com](https://dashscope.aliyun.com/) | `DASHSCOPE_API_KEY` |
| Zhipu AI (BigModel) | [open.bigmodel.cn](https://open.bigmodel.cn/) | `BIGMODEL_API_KEY` |

</details>

### 3. Launch Backend

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The API will be available at `http://localhost:8081/api`.

### 4. Launch Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI will be available at `http://localhost:5173`. API requests are automatically proxied to the backend.

### 5. Open & Chat

Navigate to **http://localhost:5173** — you're ready to go.

---

## 📡 API Reference

### `GET /api/ai/chat`

Initiates a streaming chat session.

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `memoryId` | `int` | Yes | Unique session identifier for conversation memory isolation |
| `message` | `string` | Yes | The user's input message |

**Response:** `text/event-stream`

```text
data: Based on your question about
data:  Spring Boot configuration,
data:  here are the key steps...
```

**cURL Example:**

```bash
curl -N "http://localhost:8081/api/ai/chat?memoryId=1&message=How+do+I+prepare+for+a+backend+interview"
```

---

## 🧩 Internal Design

### Service Composition

`CodeForgeAiServiceFactory` assembles the AI service pipeline using LangChain4j's declarative `AiServices.builder()`:

```
Input → Guardrails → Memory Lookup → RAG Retrieval → LLM Call (+ Tools/MCP) → Stream Output
```

| Layer | Component | Responsibility |
|-------|-----------|----------------|
| Safety | `SafeInputGuardrail` | Blocks requests containing prohibited terms |
| Memory | `ChatMemoryProvider` | Maintains per-session sliding window (10 messages) |
| Knowledge | `ContentRetriever` | Embeds & retrieves from local Markdown knowledge base |
| Tools | `InterviewQuestionTool` | Scrapes real-time interview questions on demand |
| Search | `McpToolProvider` | Delegates web search to Zhipu via MCP protocol |
| Observability | `ChatModelListener` | Logs full request/response/error lifecycle |

### RAG Pipeline

1. **Ingest** — Load Markdown docs from `src/main/resources/docs/`
2. **Split** — Paragraph-level chunking (1,000 chars, 200-char overlap)
3. **Transform** — Prepend filename metadata to each segment for context
4. **Embed** — Vectorize with DashScope `text-embedding-v4`
5. **Store** — In-memory embedding store (swappable to persistent stores)
6. **Retrieve** — Cosine similarity search at query time, top-k injection into prompt

### Knowledge Base Contents

| Document | Coverage |
|----------|----------|
| Programming Study Routes | Multi-track roadmaps (backend, data, security) with milestones |
| Interview Question Bank | Categorized questions with expected signals and answer frameworks |
| Job-Seeking Playbook | End-to-end guide: positioning → résumé → networking → offers |
| Project Learning Advice | MVP-to-portfolio methodology with evaluation criteria |
| Templates & Checklists | Ready-to-use README, résumé, outreach, and prep templates |

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DASHSCOPE_API_KEY` | Alibaba Cloud DashScope API key for Qwen models | ✅ |
| `BIGMODEL_API_KEY` | Zhipu AI API key for web search MCP | ✅ |

> Values in `application-local.yml` take precedence over environment variables.

### Application Properties

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8081` | Backend HTTP port |
| `server.servlet.context-path` | `/api` | API base path |
| `langchain4j.community.dashscope.chat-model.model-name` | `qwen-max` | Primary LLM model |

---

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Windows
mvnw.cmd test
```

---

## 📦 Deployment

### Backend

```bash
./mvnw clean package -DskipTests
java -jar target/code-forge-ai-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
# Output: frontend/dist/
```

Serve the `dist/` directory with any static file server (Nginx, Caddy, etc.) and proxy `/api` to the backend.

---

## 📂 Project Structure

```
code-forge-ai/
├── pom.xml
├── src/main/java/com/workspace/codeforgeai/
│   ├── CodeForgeAiApplication.java          # Application entry point
│   ├── controller/
│   │   └── AiController.java               # SSE streaming endpoint
│   └── ai/
│       ├── CodeForgeAi.java                 # Low-level chat wrapper
│       ├── CodeForgeAiService.java          # AI Service contract
│       ├── CodeForgeAiServiceFactory.java   # Service assembly & wiring
│       ├── config/CorsConfig.java           # CORS policy
│       ├── guardrail/SafeInputGuardrail.java
│       ├── listener/ChatModelListenerConfig.java
│       ├── mcp/McpConfig.java               # Zhipu MCP transport
│       ├── model/QwenChatModelConfig.java   # Model bean definition
│       ├── rag/RagConfig.java               # RAG pipeline setup
│       └── tools/InterviewQuestionTool.java # @Tool implementation
├── src/main/resources/
│   ├── application.yml                      # Shared configuration
│   ├── application-local.yml                # Local overrides (git-ignored)
│   ├── system-prompt.txt                    # System prompt template
│   └── docs/                               # RAG knowledge base
├── src/test/java/                           # Test suite
└── frontend/                               # Vue 3 SPA
    ├── vite.config.js                       # Dev server & proxy
    └── src/
        ├── api/                             # HTTP & SSE client layer
        └── components/                      # UI components
```

---

## 🗺️ Roadmap

| Quarter | Milestone |
|---------|-----------|
| **Next** | Multi-user authentication · Persistent chat history (PostgreSQL) |
| **v1.1** | Multi-model support (OpenAI, Anthropic) · File upload & code review |
| **v1.2** | Docker Compose deployment · Helm chart for Kubernetes |
| **v2.0** | Admin dashboard · Knowledge base CMS · Analytics & usage metrics |

---

## 🤝 Contributing

Contributions are welcome! Please read the following before submitting a PR:

1. Fork the repository and create a feature branch from `main`
2. Follow existing code style and naming conventions
3. Include tests for any new functionality
4. Update documentation as needed
5. Open a Pull Request with a clear description of the changes

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**[⬆ Back to Top](#code-forge-ai)**

Built with [LangChain4j](https://docs.langchain4j.dev/) · [Spring Boot](https://spring.io/projects/spring-boot) · [Vue.js](https://vuejs.org/)

</div>
