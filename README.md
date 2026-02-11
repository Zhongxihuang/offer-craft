# 🛠️ Code Forge AI

<p align="center">
  <strong>AI-Powered Programming Mentor & Career Development Assistant</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen?logo=springboot" alt="Spring Boot 3.5.9"/>
  <img src="https://img.shields.io/badge/LangChain4j-1.1.0-blue" alt="LangChain4j"/>
  <img src="https://img.shields.io/badge/Vue-3.4-42b883?logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Vite-5.x-646CFF?logo=vite" alt="Vite 5"/>
</p>

---

## 📖 About

**Code Forge AI** is a full-stack AI chat application built with LangChain4j + Spring Boot + Vue 3, designed for **developers** and **job seekers**:

- 🧑‍💻 **Programming Coaching** — Code writing, debugging, and technical solution design
- 🗺️ **Learning Roadmaps** — Multi-track study paths (backend, data, security, etc.)
- 💼 **End-to-End Job Search Guidance** — Resume optimization, portfolio packaging, interview prep, offer negotiation
- 🎯 **Real-Time Interview Question Search** — Integrated web scraping tool to fetch frequently asked interview questions
- 🌐 **Web Search** — Live internet search via MCP protocol with Zhipu BigModel Web Search

---

## ✨ Key Features

| Feature | Description |
|---------|-------------|
| **Streaming Chat (SSE)** | Real-time typewriter-effect responses via Server-Sent Events |
| **RAG Knowledge Augmentation** | Loads local knowledge base documents (study routes, interview Q&A, job-seeking guides) to enhance answer quality |
| **MCP Tool Invocation** | Connects to Zhipu web search via Model Context Protocol for real-time information |
| **Custom Tools** | Built-in interview question scraper that fetches relevant questions from mianshiya.com |
| **Input Safety Guardrails** | InputGuardrail-based sensitive word filtering to ensure safe conversations |
| **Chat Memory** | Multi-turn conversation context memory, isolated by `memoryId` |
| **Chat Model Listener** | Full request/response/error logging pipeline for easy debugging |
| **Responsive Frontend** | Vue 3 + Composition API, optimized for both desktop and mobile |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (Vue 3 + Vite)               │
│         Port 5173  →  Proxy /api → Backend              │
└────────────────────────┬────────────────────────────────┘
                         │ SSE (Server-Sent Events)
┌────────────────────────▼────────────────────────────────┐
│               Backend (Spring Boot 3.5.9)                │
│                     Port 8081                            │
│  ┌──────────────────────────────────────────────────┐   │
│  │              AiController (REST API)              │   │
│  │                GET /api/ai/chat                   │   │
│  └──────────────────────┬───────────────────────────┘   │
│                         │                                │
│  ┌──────────────────────▼───────────────────────────┐   │
│  │          CodeForgeAiService (AI Services)         │   │
│  │  ┌─────────┐ ┌──────┐ ┌──────┐ ┌─────────────┐  │   │
│  │  │Guardrail│ │ RAG  │ │Tools │ │  MCP Client  │  │   │
│  │  └─────────┘ └──────┘ └──────┘ └─────────────┘  │   │
│  └──────────────────────┬───────────────────────────┘   │
│                         │                                │
│  ┌──────────────────────▼───────────────────────────┐   │
│  │         Qwen Model (DashScope API)                │   │
│  │      Chat Model  /  Streaming  /  Embedding       │   │
│  └──────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

---

## 📂 Project Structure

```
code-forge-ai/
├── pom.xml                          # Maven configuration
├── src/main/
│   ├── java/com/workspace/codeforgeai/
│   │   ├── CodeForgeAiApplication.java       # Spring Boot entry point
│   │   ├── controller/
│   │   │   └── AiController.java             # REST endpoint (SSE streaming)
│   │   └── ai/
│   │       ├── CodeForgeAi.java              # Basic chat service
│   │       ├── CodeForgeAiService.java        # AI Service interface
│   │       ├── CodeForgeAiServiceFactory.java # AI Service factory & assembly
│   │       ├── config/
│   │       │   └── CorsConfig.java           # CORS configuration
│   │       ├── guardrail/
│   │       │   └── SafeInputGuardrail.java   # Input safety guardrail
│   │       ├── listener/
│   │       │   └── ChatModelListenerConfig.java  # Model call listener
│   │       ├── mcp/
│   │       │   └── McpConfig.java            # MCP protocol config (web search)
│   │       ├── model/
│   │       │   └── QwenChatModelConfig.java   # Qwen model configuration
│   │       ├── rag/
│   │       │   └── RagConfig.java            # RAG knowledge base config
│   │       └── tools/
│   │           └── InterviewQuestionTool.java # Interview question search tool
│   └── resources/
│       ├── application.yml                   # Main configuration
│       ├── application-local.yml             # Local dev configuration
│       ├── system-prompt.txt                 # AI system prompt
│       └── docs/                             # RAG knowledge base documents
│           ├── 00_INDEX.md                   # Document index
│           ├── 01_PROGRAMMING_STUDY_ROUTE.md # Programming study routes
│           ├── 02_INTERVIEW_QUESTION_BANK.md # Interview question bank
│           ├── 03_JOB_SEEKING_PLAYBOOK.md    # Job-seeking playbook
│           ├── 04_PROJECT_LEARNING_ADVICE.md # Project-based learning advice
│           └── 05_TEMPLATES_AND_CHECKLISTS.md# Templates & checklists
├── frontend/                        # Frontend project
│   ├── package.json
│   ├── vite.config.js               # Vite config & API proxy
│   ├── index.html
│   └── src/
│       ├── App.vue                  # Root component
│       ├── main.js                  # Entry file
│       ├── api/                     # API request layer
│       │   ├── index.js             # Axios instance
│       │   └── chat.js              # Chat API (SSE)
│       ├── assets/styles/
│       │   └── main.css             # Global styles
│       └── components/
│           ├── ChatHeader.vue       # Chat header
│           ├── ChatInput.vue        # Message input box
│           ├── ChatMessage.vue      # Message bubble
│           └── WelcomeScreen.vue    # Welcome screen
└── src/test/                        # Unit tests
    └── java/com/workspace/codeforgeai/
        ├── CodeForgeAiApplicationTests.java
        └── ai/CodeForgeAiServiceTest.java
```

---

## 🔧 Tech Stack

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Runtime |
| Spring Boot | 3.5.9 | Web framework |
| Spring WebFlux | — | Reactive streams (SSE) |
| LangChain4j | 1.1.0 | AI application framework |
| LangChain4j DashScope | 1.1.0-beta7 | Qwen model integration |
| LangChain4j MCP | 1.1.0-beta7 | MCP protocol support |
| Jsoup | 1.20.1 | Web scraping (interview tool) |
| Lombok | — | Boilerplate reduction |

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| Vue | 3.4+ | UI framework |
| Vite | 5.x | Build tool |
| Axios | 1.6+ | HTTP client |
| EventSource | Native | SSE streaming |

### AI Models

| Model | Provider | Purpose |
|-------|----------|---------|
| qwen-max | Alibaba DashScope | Primary chat model |
| text-embedding-v4 | Alibaba DashScope | Text embedding (RAG) |
| BigModel Web Search | Zhipu AI | MCP web search |

---

## 🚀 Getting Started

### Prerequisites

- **JDK** 21+
- **Maven** 3.9+
- **Node.js** 18+
- **npm** 9+

### 1. Clone the Repository

```bash
git clone <repository-url>
cd code-forge-ai
```

### 2. Configure API Keys

Edit `src/main/resources/application-local.yml` and fill in your API keys:

```yaml
langchain4j:
  community:
    dashscope:
      chat-model:
        api-key: 'your-dashscope-api-key'
      embedding-model:
        api-key: 'your-dashscope-api-key'
      streaming-chat-model:
        api-key: 'your-dashscope-api-key'

bigmodel:
  api-key: 'your-bigmodel-api-key'
```

> **How to obtain API Keys:**
> - **DashScope (Qwen):** Sign up at [Alibaba DashScope](https://dashscope.aliyun.com/) and create an API Key
> - **BigModel (Zhipu):** Sign up at [Zhipu AI Open Platform](https://open.bigmodel.cn/) and create an API Key

### 3. Start the Backend

```bash
./mvnw spring-boot:run
# On Windows:
mvnw.cmd spring-boot:run
```

The backend will run at `http://localhost:8081` with API base path `/api`.

### 4. Start the Frontend

```bash
cd frontend
npm install
npm run dev
```

The dev server will run at `http://localhost:5173` with `/api` requests auto-proxied to the backend.

### 5. Start Chatting

Open your browser at **http://localhost:5173** and start chatting with the AI assistant!

---

## 📡 API Reference

### Streaming Chat

```
GET /api/ai/chat?memoryId={id}&message={text}
```

| Parameter | Type | Description |
|-----------|------|-------------|
| `memoryId` | int | Session ID (isolates multi-turn conversation memory) |
| `message` | String | User message content |

**Response Format:** `text/event-stream` (SSE)

```
data: Hello
data: , I'm
data:  Code Forge AI
data: , happy
data:  to help!
```

---

## 🧩 Core Modules

### AI Service Assembly (`CodeForgeAiServiceFactory`)

Uses LangChain4j's `AiServices.builder()` to wire the following components into a unified service:

- **ChatModel** — Qwen chat model (with listener)
- **StreamingChatModel** — Streaming chat model
- **ChatMemoryProvider** — Per-`memoryId` isolated chat memory (sliding window of last 10 messages)
- **ContentRetriever (RAG)** — Retrieves relevant content from the local knowledge base
- **Tools** — Interview question search tool (Jsoup scraper)
- **ToolProvider (MCP)** — Zhipu web search

### RAG Knowledge Base (`RagConfig`)

- Loads Markdown documents from `src/main/resources/docs/`
- Splits by paragraph (1000 chars/chunk, 200 chars overlap)
- Embeds using DashScope text-embedding-v4
- Stores in an in-memory embedding store; retrieves the most relevant content at query time

### Input Safety Guardrail (`SafeInputGuardrail`)

- Performs sensitive word detection on user input
- Rejects the request with a warning when a sensitive word is matched

### MCP Web Search (`McpConfig`)

- Connects to Zhipu BigModel Web Search via HTTP SSE transport
- Gives the AI real-time internet search capabilities

---

## 🔑 Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `DASHSCOPE_API_KEY` | Alibaba DashScope API Key | ✅ |
| `BIGMODEL_API_KEY` | Zhipu BigModel API Key | ✅ |

You can also set these directly in `application-local.yml`, which takes precedence over environment variables.

---

## 🧪 Running Tests

```bash
./mvnw test
# On Windows:
mvnw.cmd test
```

---

## 📦 Production Build

### Backend

```bash
./mvnw clean package -DskipTests
java -jar target/code-forge-ai-0.0.1-SNAPSHOT.jar
```

### Frontend

```bash
cd frontend
npm run build
```

Build output will be in the `frontend/dist/` directory.

---

## 🗺️ Roadmap

- [ ] User authentication & multi-user support
- [ ] Chat history persistence (database storage)
- [ ] Additional AI model integrations (GPT, Claude, etc.)
- [ ] File upload & code analysis
- [ ] One-click Docker deployment
- [ ] Admin dashboard (knowledge base & session management)

---

## 📄 License

This project is for learning and educational purposes only.

---

<p align="center">
  Made with ❤️ by Code Forge AI Team
</p>
