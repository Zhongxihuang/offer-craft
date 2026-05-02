<div align="center">

# OfferCraft

**把岗位描述和简历转化为可执行的求职判断与面试准备计划。**

OfferCraft 面向正在评估具体岗位的求职者，帮助用户判断是否值得投递、识别最影响结果的关键差距，并生成短期准备动作。

[![Java 21](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5.9-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1.0-2B6CB0)](https://docs.langchain4j.dev/)
[![Vue 3](https://img.shields.io/badge/Vue.js-3.4-4FC08D?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

[English](README.md) | [演示](#演示) | [本地运行](#本地运行) | [接口](#接口)

</div>

---

## 项目简介

很多求职者在看到一个岗位时，真正需要解决的不是“生成一段泛化建议”，而是更具体的决策问题：这个岗位是否值得投入时间，自己的经历和岗位要求差在哪里，接下来几天应该如何准备。

OfferCraft 将岗位描述和候选人资料组织成一个固定分析流程。系统会先理解岗位要求，再分析候选人证据，随后排序关键差距，最后生成投递建议和面试准备计划。用户也可以基于已生成的结果继续追问或补充信息。

当前演示样例重点围绕 AI 产品、产品经理、AI 应用和技术相关岗位，因为这类岗位更依赖项目证据、技术理解、业务判断和面试表达。但产品本身不限制岗位类型，任何具体岗位描述和候选人资料都可以作为输入。

## 核心输出

- **投递判断**：建议直接投递、调整表达后投递、先准备，或转向更匹配的岗位。
- **岗位要求**：提取岗位方向、核心要求、关键词和面试关注点。
- **候选人证据**：识别已有优势、可迁移经历、缺失信号和潜在风险。
- **三项关键差距**：说明最影响匹配结果的差距、原因和准备动作。
- **可信度说明**：展示哪些判断有直接证据，哪些仍需要补充信息。
- **短期准备计划**：把差距转化为 3-7 天内可以执行的准备任务。

## 主要能力

### 1. 岗位理解

从岗位描述中提取角色定位、硬性要求、加分项、关键词和面试重点。

### 2. 候选人分析

从简历或候选人资料中提取能力证据、项目经历、可迁移优势和缺失信息。

### 3. 差距排序

根据招聘影响、证据强弱和面试风险，排序最重要的三项差距。

### 4. 面试准备

生成短期准备计划、简历表达建议、行为面试准备点和高风险问题方向。

### 5. 结果追问

用户可以基于已保存的分析结果继续追问、补充信息，并生成新的分析版本。

### 6. 上传与恢复

支持粘贴文本，也支持上传 PDF、TXT 和 Markdown 文件。分析结果会写入本地数据库，后端重启后仍可恢复。

## 使用流程

```text
1. 粘贴或上传岗位描述
2. 粘贴或上传简历 / 候选人资料
3. 补充目标岗位、级别、公司和关注点
4. 运行分析
5. 查看投递判断、关键差距、可信度和准备计划
6. 基于结果继续追问，或补充信息更新分析
```

## 设计思路

- **流程优先**：先形成结构化分析结果，再进入追问，避免产品退化成自由问答。
- **证据优先**：结果中展示候选人已有证据、缺失信息和判断置信度。
- **行动优先**：不仅指出差距，还把差距转成可执行的准备动作。
- **本地可运行**：默认演示模式不依赖外部模型密钥，便于快速体验和开发验证。
- **可扩展**：搜索、检索和工具调用作为增强能力，不阻断主流程。

<a id="演示"></a>

## 演示

仓库提供一组可复现的标准演示数据，用于快速体验完整流程。

**标准场景**：企业级生成式 AI 产品的高级产品经理岗位。

这个场景用于测试岗位要求提取、候选人证据分析、平台能力判断、治理意识和面试准备质量。候选人具备较强产品和数据背景，但在大模型平台深度、企业级治理经验和技术表达上存在可解释差距。

### 演示材料

| 文件 | 用途 |
|---|---|
| [job-description.md](docs/demo/ai-pm-canonical/job-description.md) | 标准岗位描述 |
| [candidate-resume.md](docs/demo/ai-pm-canonical/candidate-resume.md) | 标准候选人资料 |
| [workflow-request.json](docs/demo/ai-pm-canonical/workflow-request.json) | 接口请求样例 |
| [workflow-response.sample.json](docs/demo/ai-pm-canonical/workflow-response.sample.json) | 标准输出样例 |
| [support-chat-followups.md](docs/demo/ai-pm-canonical/support-chat-followups.md) | 结果追问示例 |
| [demo-script.md](docs/demo/ai-pm-canonical/demo-script.md) | 两分钟演示脚本 |

### 截图

- [输入页](docs/demo/ai-pm-canonical/screenshots/01-intake.png)
- [分析结果页](docs/demo/ai-pm-canonical/screenshots/02-workflow-result.png)
- [结果追问页](docs/demo/ai-pm-canonical/screenshots/03-support-chat.png)

当前版本以本地可运行演示为主，暂未提供公开在线地址。

<a id="本地运行"></a>

## 本地运行

### 环境要求

- JDK 21
- Maven 3.9+
- Node.js 18+
- npm

### 启动后端

默认使用本地演示模式，不需要外部模型密钥。

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

后端默认地址：

```text
http://localhost:8081/api
```

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

### 快速体验

1. 打开前端首页。
2. 使用演示材料中的岗位描述和候选人资料。
3. 点击分析按钮。
4. 查看投递判断、关键差距、可信度和准备计划。
5. 进入追问区域，继续生成准备计划、简历表达建议或模拟面试问题。

## 配置说明

### 本地演示模式

本地默认配置会启用演示模式。该模式不依赖外部模型密钥，可直接跑通主流程，适合快速体验和开发验证。

### 真实模型模式

如果要接入真实模型，可以设置：

| 变量 | 是否必填 | 说明 |
|---|---|---|
| `CAREER_AI_MODE` | 否 | `demo` 或 `provider` |
| `DASHSCOPE_API_KEY` | 真实模型模式必填 | DashScope / Qwen 模型密钥 |
| `BIGMODEL_API_KEY` | 否 | 网页搜索增强能力的可选密钥 |

Windows PowerShell:

```powershell
$env:CAREER_AI_MODE="provider"
$env:DASHSCOPE_API_KEY="your_dashscope_key"
$env:BIGMODEL_API_KEY="optional_search_key"
mvn.cmd spring-boot:run
```

### 运行与安全配置

| 变量 | 默认值 | 说明 |
|---|---|---|
| `CAREER_WEB_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | 允许访问后端的前端地址 |
| `CAREER_WORKFLOW_ACCESS_REQUIRE_TOKEN` | 本地为 `false`，基础配置为 `true` | 是否保护恢复、更新和版本接口 |
| `CAREER_WORKFLOW_READ_TOKEN` | 空 | 访问受保护分析结果时使用的令牌 |
| `CAREER_WORKFLOW_MAX_EXTRACTED_TEXT_CHARS` | `200000` | 单份文档可抽取的最大文本长度 |
| `SPRING_H2_CONSOLE_ENABLED` | `false` | 是否开启 H2 控制台 |

<a id="接口"></a>

## 接口

基础地址：

```text
http://localhost:8081/api
```

| 方法 | 路径 | 说明 |
|---|---|---|
| `POST` | `/career/workflow/analyze` | 分析粘贴文本形式的岗位描述和简历 |
| `POST` | `/career/workflow/analyze-upload` | 分析上传文件形式的岗位描述和简历 |
| `POST` | `/career/workflow/compare` | 对比 2-5 个目标岗位 |
| `GET` | `/career/workflow/{workflowId}` | 恢复已保存的分析结果 |
| `GET` | `/career/workflow/{workflowId}/versions` | 获取分析版本列表 |
| `POST` | `/career/workflow/{workflowId}/refine` | 基于补充信息更新分析 |
| `GET` | `/ai/chat` | 基于分析结果的流式追问 |

### 文本分析请求示例

```json
{
  "locale": "zh-CN",
  "targetRole": "AI 产品经理",
  "targetLevel": "实习生",
  "companyName": "美团",
  "jobDescription": "粘贴岗位描述...",
  "candidateProfile": "粘贴简历或候选人资料...",
  "focusAreas": ["面试策略", "技术理解"],
  "includeCompanyResearch": true
}
```

### 上传分析请求示例

```powershell
curl.exe -X POST "http://localhost:8081/api/career/workflow/analyze-upload" `
  -F "locale=zh-CN" `
  -F "targetRole=AI 产品经理" `
  -F "jobDescriptionText=在这里粘贴岗位描述" `
  -F "candidateProfileFile=@docs/demo/ai-pm-canonical/candidate-resume.md;type=text/markdown"
```

### 追问请求示例

```text
GET /api/ai/chat?memoryId=demo-1&workflowId={workflowId}&locale=zh-CN&message=把最重要的差距转成7天准备计划
```

### 错误响应格式

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

## 技术栈

| 层级 | 技术 |
|---|---|
| 前端 | Vue 3, Vite, vue-i18n, Axios, EventSource |
| 后端 | Java 21, Spring Boot 3.5, Spring MVC, Spring Data JPA |
| 人工智能编排 | LangChain4j typed AI services, staged workflow orchestration |
| 模型运行 | 本地确定性演示模式, DashScope / Qwen 真实模型模式 |
| 检索与工具 | 可选 RAG, MCP web search, tool calling |
| 存储 | H2 文件数据库, 本地上传文件存储 |
| 校验 | Jakarta Validation, 统一接口错误响应 |
| 测试 | JUnit 5, WebMvcTest, Spring Boot integration tests, demo outcome eval |

## 架构

```text
Vue 前端
  -> Spring Boot 接口服务
    -> 求职分析应用服务
      -> 岗位解析
      -> 候选人分析
      -> 差距分析
      -> 面试准备生成
      -> 可信度与证据标注
    -> H2 分析结果持久化
    -> 本地上传文件存储
    -> 可选检索 / 搜索 / 工具能力
```

分析流程被拆成多个阶段。每个阶段都有明确职责，因此结果比单次自由问答更容易检查、测试、更新和解释。

## 项目结构

```text
.
|-- frontend/
|   |-- src/api/                 # 前端接口调用
|   |-- src/components/          # 表单、结果页、追问面板
|   `-- src/i18n/                # 中文 / 英文界面文案
|-- src/main/java/com/workspace/codeforgeai/
|   |-- ai/                      # 聊天、检索、工具、模型配置
|   |-- career/api/              # 求职分析接口和请求对象
|   |-- career/demo/             # 本地演示模式实现
|   |-- career/jd/               # 岗位解析
|   |-- career/candidate/        # 候选人分析
|   |-- career/gap/              # 差距分析
|   |-- career/interview/        # 面试准备生成
|   |-- career/workflow/         # 编排、持久化、上传、可信度
|   `-- common/                  # 错误响应和国际化支持
|-- src/main/resources/
|   |-- application.yml
|   |-- application-local.yml
|   |-- messages_en.properties
|   `-- messages_zh_CN.properties
|-- docs/demo/ai-pm-canonical/   # 演示输入、样例输出和截图
|-- docs/product/                # 产品说明、评估样例和演示讲稿
|-- scripts/                     # 本地验证脚本
|-- pom.xml
`-- README.md
```

## 测试

后端：

```bash
mvn test
```

前端：

```bash
cd frontend
npm run build
```

后端测试覆盖控制器、本地演示模式集成测试、上传与持久化、安全边界，以及需要真实凭证时才运行的真实模型模式测试。

## 安全与限制

已实现的基础防护：

- 前端来源白名单。
- H2 控制台默认关闭。
- 上传文件限制为 PDF、TXT 和 Markdown。
- 上传路径归一化和路径穿越校验。
- 抽取文本长度限制。
- 恢复、更新和版本接口可选令牌保护。
- 默认不记录模型请求、响应、岗位描述、简历和密钥。

当前限制：

- 暂无账号系统和多租户权限模型。
- H2 和本地文件存储适合本地演示和单节点使用，不建议直接用于公网生产。
- 上传文件尚未接入病毒扫描、个人信息脱敏或自动保留周期。
- 追问历史尚未作为完整会话记录持久化。
- 搜索、检索和工具调用是增强能力，不可用时主流程会降级返回。

## 后续方向

- 候选人工作台：保存多个分析结果和岗位对比历史。
- 更完整的追问更新流程：用户补充经历后自动生成新版本并解释变化。
- 生产级存储：迁移到 PostgreSQL 和对象存储。
- 权限与隐私：增加登录、结果归属、文件保留周期和删除入口。
- 质量评估：扩展中英文评估集，覆盖更多岗位、弱简历和模糊岗位描述。

## 相关文档

- [中文 PRD](docs/product/chinese-prd.md)
- [Outcome eval cases](docs/product/eval-cases.md)
- [Demo talk track](docs/product/interview-talk-track.md)
- [Launch checklist](docs/product/launch-checklist.md)

## 许可证

OfferCraft 基于 [MIT License](LICENSE) 开源。
